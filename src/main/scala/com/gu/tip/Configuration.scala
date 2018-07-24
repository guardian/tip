package com.gu.tip

import java.io.FileNotFoundException

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.jcazevedo.moultingyaml._

import scala.util.Try
import scala.io.Source.fromFile

// $COVERAGE-OFF$

case class TipConfig(owner: String,
                     repo: String,
                     personalAccessToken: String,
                     label: String,
                     boardSha: String = "")

class TipConfigurationException(
    msg: String = "Missing TiP config. Please refer to README.")
    extends RuntimeException(msg)
class MissingPathConfigurationFile(
    msg: String = "Missing tip.yaml. Please refer to README")
    extends RuntimeException(msg)
class PathConfigurationSyntaxError(
    msg: String = "Bad syntax in tip.yaml. Please refer to README")
    extends RuntimeException(msg)

class Configuration(config: TipConfig) {
  def this() {
    this(
      ConfigFactory
        .load()
        .as[Option[TipConfig]]("tip")
        .getOrElse(throw new TipConfigurationException)
    )
  }

  def this(typesafeConfig: Config) {
    this(
      typesafeConfig
        .as[Option[TipConfig]]("tip")
        .getOrElse(throw new TipConfigurationException)
    )
  }

  object TipYamlProtocol extends DefaultYamlProtocol {
    implicit val pathFormat: YamlFormat[Path] = yamlFormat2(Path)
  }

  import TipYamlProtocol._

  val tipConfig = config

  private def readFile(filename: String): String =
    Option(getClass.getClassLoader.getResource(filename))
      .map { path =>
        fromFile(path.getPath).mkString
      }
      .getOrElse(throw new FileNotFoundException(
        s"Path definition file not found on the classpath: $filename"))

  def readPaths(filename: String): List[Path] =
    Try(readFile(filename).parseYaml.convertTo[List[Path]]).recover {
      case e: FileNotFoundException => throw new MissingPathConfigurationFile
      case _                        => throw new PathConfigurationSyntaxError
    }.get

  def cloudEnabled: Boolean = tipConfig.boardSha.nonEmpty
}

trait ConfigurationIf {
  val configuration: Configuration
}

trait ConfigFromTypesafe extends ConfigurationIf {
  override val configuration: Configuration = new Configuration()
}

// $COVERAGE-ON$
