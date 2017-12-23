package com.gu.tip

import scala.concurrent.ExecutionContext.Implicits.global
import fs2.{Strategy, Task}
import org.http4s.Status.InternalServerError
import org.http4s.client.UnexpectedStatus
import org.scalatest.{FlatSpec, MustMatchers}

class NotifierTest extends FlatSpec with MustMatchers {

  implicit val strategy = Strategy.fromExecutionContext(global)

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
        Task(mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        Task.fail(UnexpectedStatus(InternalServerError))
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.attemptFold(error => succeed, _ => fail).unsafeRun()
  }

  behavior of "happy Notifier"

  it should "return 200 if successfully set the label on the latest merged pull request" in {
    trait MockHttpClient extends HttpClient {
      override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) =
        Task(mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        Task("")
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.attemptFold(error => fail, _ => succeed).unsafeRun()
  }
}
