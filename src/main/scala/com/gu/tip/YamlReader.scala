package com.gu.tip

import com.typesafe.scalalogging.LazyLogging
import net.jcazevedo.moultingyaml._
import net.jcazevedo.moultingyaml.DefaultYamlProtocol

case class Path(name: String, description: String)

class EnrichedPath(path: Path) extends LazyLogging {

  private var _verified = false

  def verify() = {
    logger.info(s"${path.name} is now verified")
    _verified = true
  }

  def verified(): Boolean = _verified

}


class Paths(paths: Map[String, EnrichedPath]) {

  private var _unverifiedPathCount = paths.size

  def verify(pathName: String) = {
    val path = paths(pathName)
    if (!path.verified()) {
      path.verify()
      _unverifiedPathCount = _unverifiedPathCount - 1
    }
  }

  def allVerified: Boolean = _unverifiedPathCount == 0

}

object YamlReader extends LazyLogging {

  object TipYamlProtocol extends DefaultYamlProtocol {
    implicit val pathFormat = yamlFormat2(Path)
  }

  import TipYamlProtocol._

  def apply(configFile: String): Paths = {
    val source = scala.io.Source.fromFile("tip.yaml").mkString
    val pathList = source.parseYaml.convertTo[List[Path]]
    val paths: Map[String, EnrichedPath] = pathList.map(path => path.name -> new EnrichedPath(path)).toMap
    new Paths(paths)
  }

}

