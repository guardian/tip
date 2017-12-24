package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import cats.implicits._

trait NotifierIf { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): WriterT[IO, String, String]
}

trait Notifier extends NotifierIf with LazyLogging { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): WriterT[IO, String, String] =
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
  private def getLastMergedPullRequestNumber(): WriterT[IO, String, String] = {
    WriterT {
      getLastMergeCommitMessage().map { message =>
        val prNumberPattern = """#\d+""".r
        val prNumber = prNumberPattern.findFirstIn(message).get.tail // Using get() because currently we just swallow any exception in Tip.verify()
        (
          s"Successfully retrieved PR number of the last merged PR: $prNumber",
          prNumber
        )
      }

    }
  }

  private def setGitHubLabel(prNumber: String): WriterT[IO, String, String] =
    WriterT {
      setLabel(prNumber).map { response =>
        (
          s"Successfully set verification label on PR $prNumber",
          response
        )
      }
    }
}
