package com.gu.tip

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import com.gu.tip.cloud.{TipCloudApi, TipCloudApiIf}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContextExecutor

sealed trait TipResponse

// OK
case object LabelSet                                  extends TipResponse
case object TipFinished                               extends TipResponse
case class PathsActorResponse(msg: PathsActorMessage) extends TipResponse
case object CloudPathVerified                         extends TipResponse
case object AllTestsInProductionPassed                extends TipResponse

// NOK
case object FailedToSetLabel          extends TipResponse
case class PathNotFound(name: String) extends TipResponse
case object UnclassifiedError         extends TipResponse
case object FailedToVerifyCloudPath   extends TipResponse

trait TipIf { this: NotifierIf with TipCloudApiIf with ConfigurationIf =>
  def verify(pathName: String): Future[TipResponse]

  val pathConfigFilename = "tip.yaml"

  implicit val system: ActorSystem          = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout             = Timeout(10.second)
  lazy val pathsActor: ActorRef             = PathsActor(pathConfigFilename, configuration)
}

trait Tip extends TipIf with LazyLogging {
  this: NotifierIf with TipCloudApiIf with ConfigurationIf =>
  system.registerOnTermination(
    logger.info("Successfully terminated actor system"))

  private def fireAndForgetSetLabelOnLatestMergedPr() =
    if (configuration.tipConfig.personalAccessToken.nonEmpty) {
      setLabelOnLatestMergedPr().run.attempt
        .map({
          case Left(error) =>
            logger.error("Failed to set label on PR!", error)
            FailedToSetLabel

          case Right((logs, result)) =>
            logs.foreach(log => logger.info(log.toString))
            LabelSet
        })
        .unsafeRunSync()
    }

  private def inMemoryVerify(pathName: String): Future[TipResponse] = {
    pathsActor ? Verify(pathName) map {
      case AllPathsVerified =>
        fireAndForgetSetLabelOnLatestMergedPr()
        logger.info("All tests in production passed.")
        pathsActor ? Stop
        AllTestsInProductionPassed

      case PathDoesNotExist(pathname) =>
        logger.error(s"Unrecognized path name: $pathname")
        PathNotFound(pathname)

      case response: PathsActorMessage =>
        logger.trace(s"Tip received response from PathsActor: $response")
        PathsActorResponse(response)

    } recover {
      case e: AskTimeoutException if (e.getMessage.contains("terminated")) =>
        TipFinished

      case e =>
        logger.error(s"Unclassified Tip error: ", e)
        UnclassifiedError
    }
  }

  private def cloudVerify(pathname: String,
                          inMemoryResult: TipResponse): Future[TipResponse] =
    Future {
      inMemoryResult match {
        case PathsActorResponse(PathIsVerified(_)) | AllTestsInProductionPassed
            if configuration.tipConfig.cloudEnabled =>
          val action = if (configuration.tipConfig.boardSha.nonEmpty) {
            verifyPath(configuration.tipConfig.boardSha, pathname)
          } else {
            verifyHeadPath(configuration.tipConfig.owner,
                           configuration.tipConfig.repo,
                           pathname)
          }

          action.run.attempt
            .map({
              case Left(error) =>
                logger.error(s"Failed to cloud verify path $pathname", error)
                FailedToVerifyCloudPath

              case Right((logs, result)) =>
                logs.foreach(log => logger.info(log.toString))
                logger.info(
                  s"Successfully verified cloud path $pathname on board ${configuration.tipConfig.boardSha}!")
                CloudPathVerified
            })
            .unsafeRunSync()

        case _ => inMemoryResult
      }
    }

  def verify(pathname: String): Future[TipResponse] = {
    for {
      inMemoryResult <- inMemoryVerify(pathname)
      result         <- cloudVerify(pathname, inMemoryResult)
    } yield {
      result
    }
  }
}

object Tip
    extends Tip
    with Notifier
    with GitHubApi
    with TipCloudApi
    with HttpClient
    with ConfigFromTypesafe {

  if (configuration.tipConfig.cloudEnabled) {
    val sha = configuration.tipConfig.boardSha
    val repo =
      s"${configuration.tipConfig.owner}/${configuration.tipConfig.repo}"
    createBoard(sha, repo).run.attempt
      .unsafeRunSync()
  }
}

object TipFactory {
  def create(tipConfig: TipConfig): Tip =
    new Tip with Notifier with GitHubApi with TipCloudApi with HttpClient
    with ConfigurationIf {
      override val configuration: Configuration = new Configuration(tipConfig)

      if (configuration.tipConfig.cloudEnabled) {
        val sha = configuration.tipConfig.boardSha
        val repo =
          s"${configuration.tipConfig.owner}/${configuration.tipConfig.repo}"
        createBoard(sha, repo).run.attempt
          .unsafeRunSync()
      }
    }

  def create(typesafeConfig: Config): Tip =
    new Tip with Notifier with GitHubApi with TipCloudApi with HttpClient
    with ConfigurationIf {
      override val configuration: Configuration = new Configuration(
        typesafeConfig)

      if (configuration.tipConfig.cloudEnabled) {
        val sha = configuration.tipConfig.boardSha
        val repo =
          s"${configuration.tipConfig.owner}/${configuration.tipConfig.repo}"
        createBoard(sha, repo).run.attempt
          .unsafeRunSync()
      }
    }

  def createDummy(config: TipConfig): Tip = {
    new Tip with Notifier with GitHubApi with TipCloudApi with HttpClient
    with ConfigurationIf {
      override val configuration: Configuration = new Configuration(config)
      override def verify(pathname: String): Future[TipResponse] =
        Future.successful(LabelSet)
    }
  }
}
