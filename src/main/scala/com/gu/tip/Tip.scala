package com.gu.tip

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import com.gu.tip.cloud.{TipCloudApi, TipCloudApiIf}
import scala.concurrent.ExecutionContextExecutor

sealed trait TipResponse

// OK
case object LabelSet                                  extends TipResponse
case object TipFinished                               extends TipResponse
case class PathsActorResponse(msg: PathsActorMessage) extends TipResponse
case object CloudPathVerified                         extends TipResponse

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

  private def inMemoryVerify(pathName: String): Future[TipResponse] = {
    pathsActor ? Verify(pathName) map {
      case AllPathsVerified =>
        setLabelOnLatestMergedPr.run.attempt
          .map({
            case Left(error) =>
              logger.error("Failed to set label on PR!", error)
              FailedToSetLabel

            case Right((logs, result)) =>
              logs.foreach(log => logger.info(log.toString))
              logger.info("Successfully verified all paths!")
              pathsActor ? Stop
              LabelSet
          })
          .unsafeRunSync()

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
        case PathsActorResponse(PathIsVerified(pathname))
            if configuration.cloudEnabled =>
          verifyPath(configuration.tipConfig.boardSha, pathname).run.attempt
            .map({
              case Left(error) =>
                logger.error(s"Failed to cloud verify path $pathname", error)
                FailedToVerifyCloudPath

              case Right((logs, result)) =>
                logs.foreach(log => logger.info(log.toString))
                logger.info(s"Successfully verified cloud path $pathname!")
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

  if (configuration.cloudEnabled) {
    createBoard(configuration.tipConfig.boardSha).run.attempt.unsafeRunSync()
  }
}

object TipFactory {
  def apply(config: TipConfig): Tip =
    new Tip with Notifier with GitHubApi with TipCloudApi with HttpClient
    with ConfigurationIf {

      override val configuration: Configuration = new Configuration(config)

      if (configuration.cloudEnabled) {
        createBoard(configuration.tipConfig.boardSha).run.attempt
          .unsafeRunSync()
      }
    }
}
