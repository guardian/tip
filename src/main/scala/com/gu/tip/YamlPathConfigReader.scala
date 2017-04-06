package com.gu.tip

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import net.jcazevedo.moultingyaml._
import net.jcazevedo.moultingyaml.DefaultYamlProtocol

import scala.io.Source._
import scala.util.{Success, Try}

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

class Paths(val paths: Map[String, EnrichedPath]) {
  def verify(pathName: String) = {
    val path = paths(pathName)
    if (!path.verified()) {
      path.verify()
      _unverifiedPathCount = _unverifiedPathCount - 1
    }
  }

  def allVerified: Boolean = _unverifiedPathCount == 0

  private var _unverifiedPathCount = paths.size
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
object YamlPathConfigReader extends LazyLogging {
  import TipYamlProtocol._


  private def readFile(filename: String): Option[String] = {
    val pathsToTry = List(filename, s"./conf/${filename}")
    pathsToTry map { file => Try(fromFile(file).mkString) } collectFirst { case Success(str) => str }
  }

  def apply(filename: String): Try[Paths] = Try {
    readFile(filename).map { source =>
      val pathList = source.parseYaml.convertTo[List[Path]]
      val paths: Map[String, EnrichedPath] = pathList.map(path => path.name -> new EnrichedPath(path)).toMap
      new Paths(paths)
    }.getOrElse(throw new FileNotFoundException(s"Cannot find ${filename}"))
  }
}

