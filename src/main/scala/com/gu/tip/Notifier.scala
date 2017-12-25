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
      prNumber     <- getLastMergedPullRequestNumber()
      responseBody <- setGitHubLabel(prNumber)
    } yield {
      responseBody
    }

  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
   */
  private def getLastMergedPullRequestNumber(): WriterT[IO, List[Log], String] =
    getLastMergeCommitMessage
      .map { commitMessage =>
        val prNumberPattern = """#\d+""".r
        val prNumber        = prNumberPattern.findFirstIn(commitMessage).get.tail // Using get() because currently we just swallow any exception in Tip.verify()
        prNumber
      }
      .tell(List(Log(
        "INFO",
        s"Successfully extracted PR number from the commit message of the last merged PR")))

  private def setGitHubLabel(prNumber: String): WriterT[IO, List[Log], String] =
    setLabel(prNumber).tell(
      List(Log("INFO", s"Successfully set verification label on PR $prNumber")))
}
