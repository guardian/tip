package com.gu.tip

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

trait NotifierIf { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): IO[String]
}

trait Notifier extends NotifierIf with LazyLogging { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): IO[String] =
    for {
      prNumber <- getLastMergedPullRequestNumber()
      responseBody <- setGitHubLabel(prNumber)
    } yield {
      responseBody
    }

  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
  */
  private def getLastMergedPullRequestNumber(): IO[String] = {
    getLastMergeCommitMessage().map { message =>
      val prNumberPattern = """#\d+""".r
      prNumberPattern.findFirstIn(message).get.tail // Using get() because currently we just swallow any exception in Tip.verify()
    }
  }

  private def setGitHubLabel(prNumber: String): IO[String] =
    setLabel(prNumber).map { response =>
      logger.info(s"Successfully set verification label on PR $prNumber")
      response
    }
}
