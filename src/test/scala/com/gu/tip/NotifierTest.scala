package com.gu.tip

import org.scalatest.{FlatSpec, MustMatchers}

class NotifierTest extends FlatSpec with MustMatchers {

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
        (200, mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        (500, "")
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr() must not be 200
  }

  behavior of "happy Notifier"

  it should "return 200 if successfully set the label on the latest merged pull request" in {
    trait MockHttpClient extends HttpClient {
      override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) =
        (200, mockCommitMessageResponse)

      override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") =
        (200, "")
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr() mustBe 200
  }
}
