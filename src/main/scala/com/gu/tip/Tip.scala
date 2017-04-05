package com.gu.tip

import com.typesafe.scalalogging.LazyLogging
import okhttp3._
import net.liftweb.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

object Tip extends LazyLogging {

  private implicit val formats = DefaultFormats

  def verify(pathName: String) = {
    if (!_labelSet) {
      paths.verify(pathName)
      if (paths.allVerified) {
        _labelSet = true
        Future(setLabel(getLastMergedPullRequestNumber()))
          .andThen { case Failure(e) => logger.error(s"Tip failed because:", e) }
      }
    }
  }

  /*
    Latest commit message to master has the following form:
      "Merge pull request #118 from ${owner}/hackday-2017-tip-test-1"

    So we try to pick out pull request number #118
  */
  private def getLastMergedPullRequestNumber() = {
    val request = new Request.Builder()
      .url(s"${githubApiRoot}/repos/${owner}/${repo}/commits/master")
      .addHeader("Authorization", s"token ${personalAccessToken}")
      .build()
    val response = client.newCall(request).execute()
    val responseBody = response.body().string
    val message = (parse(responseBody) \ "commit" \ "message").extract[String] // e.g. #124
    val prNumberPattern = """#\d+""".r
    prNumberPattern.findFirstIn(message).get.tail // Using get() because currently we just swallow any exception in verify()
  }

  private def setLabel(prNumber: String) = {
    val request = new Request.Builder()
      .url(s"${githubApiRoot}/repos/${owner}/${repo}/issues/${prNumber}/labels")
      .addHeader("Authorization", s"token ${personalAccessToken}")
      .post(RequestBody.create(MediaType.parse("application/json"), s"""["${tipLabel}"]"""))
      .build()

    val response = client.newCall(request).execute()

    if (response.code() == 200) {
      logger.info("Verification label added to PR successfully")
    }
  }

  private var _labelSet = false

  private val client = new OkHttpClient()
  private val githubApiRoot = "https://api.github.com"

  private val owner: String = Configuration.tip.owner
  private val repo: String = Configuration.tip.repo
  private val personalAccessToken: String = Configuration.tip.personalAccessToken
  private val tipLabel: String = Configuration.tip.label
  private val paths: Paths = YamlReader("tip.yaml")
}

