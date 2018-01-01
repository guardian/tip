package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._
import net.liftweb.json.DefaultFormats

trait GitHubApiIf { this: HttpClientIf =>
  def getLastMergeCommitMessage: WriterT[IO, List[Log], String]
  def setLabel(prNumber: String): WriterT[IO, List[Log], String]

  val githubApiRoot = "https://api.github.com"
}

trait GitHubApi extends GitHubApiIf with LazyLogging { this: HttpClientIf =>

  import Configuration.tipConfig._

  private implicit val formats: DefaultFormats.type = DefaultFormats
  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
   */
  override def getLastMergeCommitMessage: WriterT[IO, List[Log], String] =
    get(s"$githubApiRoot/repos/$owner/$repo/commits/master", authHeader)
      .map(
        response => (parse(response) \ "commit" \ "message").extract[String]
      )
      .tell(
        List(Log("INFO",
                 s"Successfully retrieved commit message of last merged PR")))

  override def setLabel(prNumber: String): WriterT[IO, List[Log], String] =
    post(s"$githubApiRoot/repos/$owner/$repo/issues/$prNumber/labels",
         authHeader,
         s"""["$label"]""")
      .tell(
        List(Log("INFO", s"Successfully set label '$label' on PR $prNumber")))

  private lazy val authHeader = "Authorization" -> s"token $personalAccessToken"
}
