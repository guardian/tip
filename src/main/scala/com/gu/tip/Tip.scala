package com.gu.tip

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout

sealed trait TipResponse

// OK
case object LabelSet                                  extends TipResponse
case object TipFinished                               extends TipResponse
case class PathsActorResponse(msg: PathsActorMessage) extends TipResponse

// NOK
case object FailedToSetLabel                          extends TipResponse
case class PathNotFound(name: String)                 extends TipResponse
case object UnclassifiedError                         extends TipResponse

trait TipIf { this: NotifierIf =>
  def verify(pathName: String): Future[TipResponse]

  val pathConfigFilename = "tip.yaml"

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(10.second)
  lazy val pathsActor: ActorRef = PathsActor(pathConfigFilename)
}

trait Tip extends TipIf with LazyLogging { this: NotifierIf =>
  system.registerOnTermination(logger.info("Successfully terminated actor system"))

  def verify(pathName: String): Future[TipResponse] = {
      pathsActor ? Verify(pathName) map {
        case AllPathsVerified =>
          if (setLabelOnLatestMergedPr() == 200) {
            logger.info("Successfully verified all paths!")
            pathsActor ? Stop
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

      } recover {
        case e: AskTimeoutException if (e.getMessage.contains("terminated")) => TipFinished

        case e =>
          logger.error(s"Unclassified Tip error: ", e)
          UnclassifiedError
      }
    }
}

object Tip extends Tip with Notifier with GitHubApi with HttpClient

