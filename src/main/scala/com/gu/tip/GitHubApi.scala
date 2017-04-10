package com.gu.tip

import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._
import net.liftweb.json.DefaultFormats

trait GitHubApiIf { this: HttpClientIf =>
  def getLastMergeCommitMessage(): String
  def setLabel(prNumber: String): Int

  val githubApiRoot = "https://api.github.com"
}

trait GitHubApi extends GitHubApiIf with LazyLogging { this: HttpClientIf =>

  private implicit val formats = DefaultFormats
  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
  */
  override def getLastMergeCommitMessage(): String = {
    val (code, responseBody) = get(s"${githubApiRoot}/repos/${owner}/${repo}/commits/master", authHeader)
    (parse(responseBody) \ "commit" \ "message").extract[String]
  }

  override def setLabel(prNumber: String): Int = {
    val (code, responseBody) = post(s"${githubApiRoot}/repos/${owner}/${repo}/issues/${prNumber}/labels", authHeader, s"""["${tipLabel}"]""")
    code
  }

  private val owner: String = Configuration.tip.owner
  private val repo: String = Configuration.tip.repo
  private val personalAccessToken: String = Configuration.tip.personalAccessToken
  private val tipLabel: String = Configuration.tip.label

  private val authHeader = "Authorization" -> s"token ${personalAccessToken}"
}
