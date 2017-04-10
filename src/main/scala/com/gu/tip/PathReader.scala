package com.gu.tip

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import net.jcazevedo.moultingyaml._
import net.jcazevedo.moultingyaml.DefaultYamlProtocol

import scala.io.Source._
import scala.util.Try
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

// $COVERAGE-OFF$
case class Path(name: String, description: String)

object TipYamlProtocol extends DefaultYamlProtocol {
  implicit val pathFormat = yamlFormat2(Path)
}
// $COVERAGE-ON$

class EnrichedPath(path: Path) extends LazyLogging {
  def verify() = {
    logger.info(s"${path.name} is now verified")
    _verified = true
  }

  def verified(): Boolean = _verified

  private var _verified = false
}

sealed trait PathsActorMessage

// OK
case class Verify(pathName: String)                 extends PathsActorMessage
case class PathIsVerified(pathName: String)         extends PathsActorMessage
case class PathIsUnverified(pathName: String)       extends PathsActorMessage
case class PathIsAlreadyVerified(pathName: String)  extends PathsActorMessage
case object AllPathsVerified                        extends PathsActorMessage
case object AllPathsAlreadyVerified                 extends PathsActorMessage
case object NumberOfPaths                           extends PathsActorMessage
case class NumberOfPathsAnswer(value: Int)          extends PathsActorMessage
case object NumberOfVerifiedPaths                   extends PathsActorMessage
case class NumberOfVerifiedPathsAnswer(value: Int)  extends PathsActorMessage

// NOK
case class PathDoesNotExist(pathName: String)       extends PathsActorMessage

class PathsActor(val paths: Map[String, EnrichedPath]) extends Actor with LazyLogging{
  private def allVerified: Boolean = _unverifiedPathCount == 0

  private var _unverifiedPathCount = paths.size

  def receive = {
    case Verify(pathname) =>
      logger.trace("Paths got Verify message")

      Try(paths(pathname)).map { path =>
        if (allVerified) {
          logger.trace(s"All paths are already verified")
          sender() ! AllPathsAlreadyVerified
        }
        else
        if (path.verified()) {
          logger.trace(s"Path ${pathname} is already verified")
          sender() ! PathIsAlreadyVerified(pathname)
        }
        else {
          path.verify()
          _unverifiedPathCount = _unverifiedPathCount - 1
          if (allVerified) {
            logger.trace("All paths are verified!")
            sender() ! AllPathsVerified
          }
          else {
            logger.trace(s"Path ${pathname} is verified!")
            sender() ! PathIsVerified(pathname)
          }
        }
      } recover { case e: NoSuchElementException =>
        logger.error(s"Unrecognized path name. Available paths: ${paths.map(_._1).mkString(",")}")
        sender() ! PathDoesNotExist(pathname)
      }

    case NumberOfPaths => sender() ! NumberOfPathsAnswer(paths.size)

    case NumberOfVerifiedPaths => sender() ! NumberOfVerifiedPathsAnswer(paths.size - _unverifiedPathCount)
  }
}

trait PathReaderIf {
  def readPaths(filename: String)(implicit system: ActorSystem): ActorRef
}

/**
  * Reader for path config file with the following syntax:
  *
  *     - name: Buy Subscription
  *       description: User completes subscription purchase journey
  *
  *     - name: Register Account
  *       description: User completes account registration journey
  */
trait PathReader extends PathReaderIf with LazyLogging {
  import TipYamlProtocol._

  private def readFile(filename: String): String =
    Option(getClass.getClassLoader.getResource(filename)).map {
      path => fromFile(path.getPath).mkString
    }.getOrElse(throw new FileNotFoundException(s"Path definition file not found on the classpath: $filename"))

  def readPaths(filename: String)(implicit system: ActorSystem): ActorRef = {
    val pathList = readFile(filename).parseYaml.convertTo[List[Path]]
    val paths: Map[String, EnrichedPath] = pathList.map(path => path.name -> new EnrichedPath(path)).toMap
    system.actorOf(Props[PathsActor](new PathsActor(paths)))
  }
}

