package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging

trait NotifierIf { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): WriterT[IO, List[Log], String]
}

trait Notifier extends NotifierIf with LazyLogging { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): WriterT[IO, List[Log], String] =
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
  private def getLastMergedPullRequestNumber(): WriterT[IO, List[Log], String] = {
    WriterT {
      getLastMergeCommitMessage().map { message =>
        val prNumberPattern = """#\d+""".r
        val prNumber = prNumberPattern.findFirstIn(message).get.tail // Using get() because currently we just swallow any exception in Tip.verify()
        (
          List(Log("INFO", s"Successfully retrieved PR number of the last merged PR: $prNumber")),
          prNumber
        )
      }

    }
  }

  private def setGitHubLabel(prNumber: String): WriterT[IO, List[Log], String] =
    WriterT {
      setLabel(prNumber).map { response =>
        (
          List(Log("INFO", s"Successfully set verification label on PR $prNumber")),
          response
        )
      }
    }
}
