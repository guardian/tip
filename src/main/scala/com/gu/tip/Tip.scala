package com.gu.tip

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import net.jcazevedo.moultingyaml.DeserializationException

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

sealed trait TipResponse

// OK
case object LabelSet                                  extends TipResponse
case class PathsActorResponse(msg: PathsActorMessage) extends TipResponse

// NOK
case object FailedToSetLabel                          extends TipResponse
case object PathDefinitionSyntaxError                 extends TipResponse
case object PathDefinitionFileNotFound                extends TipResponse
case class PathNotFound(name: String)                 extends TipResponse
case object UnclassifiedError                         extends TipResponse

trait TipIf { this: PathReaderIf with NotifierIf =>
  def verify(pathName: String): Future[TipResponse]

  val pathConfigFilename = "tip.yaml"

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(10.second)
  lazy val pathsActorTry: Try[ActorRef] = Try(readPaths(pathConfigFilename))
}

trait Tip extends TipIf with LazyLogging { this: PathReaderIf with NotifierIf =>
  def verify(pathName: String): Future[TipResponse] = {
    (pathsActorTry.map { pathsActor =>
      pathsActor ? Verify(pathName) map {
        case AllPathsVerified =>
          if (setLabelOnLatestMergedPr() == 200) {
            logger.info("Successfully verified all paths!")
            LabelSet
          }
          else
            FailedToSetLabel

        case PathDoesNotExist(pathname)  =>
          logger.error(s"Unrecognized path name: $pathname")
          PathNotFound(pathname)

        case response: PathsActorMessage =>
          logger.trace(s"Tip received response from PathsActor: $response")
          PathsActorResponse(response)

      } recover { // exceptions thrown from Notifier
        case e =>
          logger.error(s"Unclassified Tip error: ", e)
          UnclassifiedError
      }
    } recover { // exceptions thrown from PathReader
        case e: DeserializationException =>
          logger.error(s"Syntax problem with path config ${pathConfigFilename} file. Refer to README for the correct syntax: ", e)
          Future(PathDefinitionSyntaxError)

        case e: FileNotFoundException =>
          logger.error(s"Path config ${pathConfigFilename} file not found. Place ${pathConfigFilename} at project base directory level: ", e)
          Future(PathDefinitionFileNotFound)

        case e =>
          logger.error(s"Unclassified Tip error: ", e)
          Future(UnclassifiedError)
    }).get
  }
}

object Tip extends Tip with PathReader with Notifier with GitHubApi with HttpClient

