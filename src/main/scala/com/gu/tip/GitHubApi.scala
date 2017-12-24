package com.gu.tip

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._
import net.liftweb.json.DefaultFormats

trait GitHubApiIf { this: HttpClientIf =>
  def getLastMergeCommitMessage(): IO[String]
  def setLabel(prNumber: String): IO[String]

  val githubApiRoot = "https://api.github.com"
}

trait GitHubApi extends GitHubApiIf with LazyLogging { this: HttpClientIf =>

  import Configuration.tipConfig._

  private implicit val formats = DefaultFormats
  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
  */
  override def getLastMergeCommitMessage(): IO[String] = {
    get(s"$githubApiRoot/repos/$owner/$repo/commits/master", authHeader).map { responseBody =>
      (parse(responseBody) \ "commit" \ "message").extract[String]
    }
  }

  override def setLabel(prNumber: String): IO[String] = {
    post(s"$githubApiRoot/repos/$owner/$repo/issues/$prNumber/labels", authHeader, s"""["$label"]""")
  }

  private lazy val authHeader = "Authorization" -> s"token $personalAccessToken"
}
