package com.gu.tip

import cats.effect.IO

import scala.concurrent.ExecutionContext.Implicits.global
import org.http4s.Status.InternalServerError
import org.http4s.client.UnexpectedStatus
import org.scalatest.{FlatSpec, MustMatchers}

class NotifierTest extends FlatSpec with MustMatchers {

//  implicit val strategy = Strategy.fromExecutionContext(global)

  val mockCommitMessageResponse =
    """
      |{
      |  "commit": {
      |    "message": "Merge pull request #118 from mario-galic/hackday-2017-tip-test-1"
      |  }
      |}
    """.stripMargin

  behavior of "unhappy Notifier"

  it should "return non-200 if cannot set the label" in {
    trait MockHttpClient extends HttpClient {
      override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) =
        IO(mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        IO.raiseError(UnexpectedStatus(InternalServerError))
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.run.attempt.map(_.fold(error => succeed, _ => fail)).unsafeRunSync()
  }

  behavior of "happy Notifier"

  it should "return 200 if successfully set the label on the latest merged pull request" in {
    trait MockHttpClient extends HttpClient {
      override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) =
        IO(mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        IO("")
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.run.attempt.map(_.fold(error => fail, _ => succeed)).unsafeRunSync()
  }
}
