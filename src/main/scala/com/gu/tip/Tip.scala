package com.gu.tip

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import net.jcazevedo.moultingyaml.DeserializationException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait TipResponse

// OK
case object PathVerified                extends TipResponse
case object LabelAlreadySet             extends TipResponse
case object LabelSet                    extends TipResponse

// NOK
case object FailedToSetLabel            extends TipResponse
case object UnrecognizedPathName        extends TipResponse
case object PathDefinitionSyntaxError   extends TipResponse
case object PathDefinitionFileNotFound  extends TipResponse
case object UnclassifiedError           extends TipResponse

trait TipIf { this: NotifierIf =>
  def verify(pathName: String): Future[TipResponse]
  val pathConfigFilename = "tip.yaml"
}

trait Tip extends TipIf with LazyLogging { this: NotifierIf =>
  def verify(pathName: String): Future[TipResponse] = Future {
      if (!_labelSet) {
        paths.verify(pathName)
        if (paths.allVerified) {
          _labelSet = true
          if (setLabelOnLatestMergedPr() != 200)
            FailedToSetLabel
          else
            LabelSet
        }
        else
          PathVerified
      } else
        LabelAlreadySet
    } recover {
      case e: NoSuchElementException =>
        logger.error(s"Unrecognized path name. Available paths: ${paths.paths.map(_._1).mkString(",")}", e)
        UnrecognizedPathName

      case e: DeserializationException =>
        logger.error(s"Syntax problem with path config ${pathConfigFilename} file. Refer to README for the correct syntax: ", e)
        PathDefinitionSyntaxError

      case e: FileNotFoundException =>
        logger.error(s"Path config ${pathConfigFilename} file not found. Place ${pathConfigFilename} at project base directory level: ", e)
        PathDefinitionFileNotFound

      case e =>
        logger.error(s"Unclassified Tip error: ", e)
        UnclassifiedError
    }

  private lazy val paths = YamlPathConfigReader(pathConfigFilename)
  private var _labelSet = false
}

object Tip extends Tip with Notifier with GitHubApi with HttpClient

