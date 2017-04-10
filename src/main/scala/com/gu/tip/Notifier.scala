package com.gu.tip

import com.typesafe.scalalogging.LazyLogging

trait NotifierIf { this: GitHubApiIf =>
  def setLabelOnLatestMergedPr(): Int
}

trait Notifier extends NotifierIf with LazyLogging { this: GitHubApiIf =>

  def setLabelOnLatestMergedPr(): Int = setGitHubLabel(getLastMergedPullRequestNumber())

  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
  */
  private def getLastMergedPullRequestNumber(): String = {
    val message = getLastMergeCommitMessage()
    val prNumberPattern = """#\d+""".r
    prNumberPattern.findFirstIn(message).get.tail // Using get() because currently we just swallow any exception in Tip.verify()
  }

  private def setGitHubLabel(prNumber: String): Int = {
    val responseCode = setLabel(prNumber)
    if (responseCode == 200) {
      logger.info("Verification label added to PR successfully")
    }
    responseCode
  }


}
