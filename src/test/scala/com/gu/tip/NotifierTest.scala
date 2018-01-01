package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import org.http4s.Status.InternalServerError
import org.http4s.client.UnexpectedStatus
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
      override def get(endpoint: String = "",
                       authHeader: (String, String) = ("", ""))
        : WriterT[IO, List[Log], String] =
        WriterT.putT(IO(mockCommitMessageResponse))(List(Log("", "")))

      override def post(endpoint: String = "",
                        authHeader: (String, String) = ("", ""),
                        jsonBody: String = ""): WriterT[IO, List[Log], String] =
        WriterT(
          IO.raiseError(UnexpectedStatus(InternalServerError))
            .map(_ => (List(Log("", "")), "")))
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.run.attempt
      .map(_.fold(error => succeed, _ => fail))
      .unsafeRunSync()
  }

  behavior of "happy Notifier"

  it should "return 200 if successfully set the label on the latest merged pull request" in {
    trait MockHttpClient extends HttpClient {
      override def get(endpoint: String = "",
                       authHeader: (String, String) = ("", ""))
        : WriterT[IO, List[Log], String] =
        WriterT.putT(IO(mockCommitMessageResponse))(List(Log("", "")))

      override def post(endpoint: String = "",
                        authHeader: (String, String) = ("", ""),
                        jsonBody: String = ""): WriterT[IO, List[Log], String] =
        WriterT.putT(IO(""))(List(Log("", "")))
    }

    object Notifier extends Notifier with GitHubApi with MockHttpClient

    Notifier.setLabelOnLatestMergedPr.run.attempt
      .map(_.fold(error => fail, _ => succeed))
      .unsafeRunSync()
  }
}
