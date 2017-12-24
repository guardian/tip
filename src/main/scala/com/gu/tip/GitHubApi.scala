package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._
import net.liftweb.json.DefaultFormats

trait GitHubApiIf { this: HttpClientIf =>
  def getLastMergeCommitMessage(): WriterT[IO, List[Log], String]
  def setLabel(prNumber: String): WriterT[IO, List[Log], String]

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
  override def getLastMergeCommitMessage(): WriterT[IO, List[Log], String] =
  WriterT {
    get(s"$githubApiRoot/repos/$owner/$repo/commits/master", authHeader).map { responseBody =>
      val commitMessage = (parse(responseBody) \ "commit" \ "message").extract[String]
      (
        List(Log("INFO", s"Successfully retrieved commit message of last merged PR: $commitMessage")),
        commitMessage
      )
    }
  }

  override def setLabel(prNumber: String): WriterT[IO, List[Log], String] =
  WriterT {
    post(s"$githubApiRoot/repos/$owner/$repo/issues/$prNumber/labels", authHeader, s"""["$label"]""").map {
      responseBody =>
        (
          List(Log("INFO", s"Successfully set label '$label' on PR $prNumber")),
          responseBody
        )
    }
  }

  private lazy val authHeader = "Authorization" -> s"token $personalAccessToken"
}
