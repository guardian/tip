package com.gu.tip

import cats.data.WriterT
import cats.effect.IO
import com.gu.tip.cloud.TipCloudApi
import org.http4s.Status.InternalServerError
import org.http4s.client.UnexpectedStatus
import org.scalatest.{AsyncFlatSpec, MustMatchers}

class TipTest extends AsyncFlatSpec with MustMatchers {

  private val mockOkResponse: WriterT[IO, List[Log], String] =
    WriterT(
      IO(
        (
          List(Log("", "")),
          ""
        )
      )
    )

  trait MockHttpClient extends HttpClient {
    override def get(endpoint: String = "",
                     authHeader: (String, String) = ("", ""))
      : WriterT[IO, List[Log], String] = mockOkResponse

    override def post(endpoint: String = "",
                      authHeader: (String, String) = ("", ""),
                      jsonBody: String = ""): WriterT[IO, List[Log], String] =
      mockOkResponse
  }

  object Tip
      extends Tip
      with Notifier
      with GitHubApi
      with TipCloudApi
      with MockHttpClient
      with ConfigFromTypesafe

  behavior of "unhappy Tip"

  it should "throw an exception on missing TiP configuration"

  it should "throw an exception on bad syntax in path definition file" in {
    object Tip
        extends Tip
        with Notifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe {
      override val pathConfigFilename: String = "tip-bad.yaml"
    }

    assertThrows[Throwable](Tip.verify(""))
  }

  it should "throw an exception on missing path definition file" in {
    object Tip
        extends Tip
        with Notifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe {
      override val pathConfigFilename: String = "tip-not-found.yaml"
    }
    assertThrows[MissingPathConfigurationFile](Tip.verify(""))
  }

  it should "handle bad path name" in {
    Tip.verify("badName").map(_ mustBe PathNotFound("badName"))
  }

  it should "handle exceptions thrown from Notifier" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr: WriterT[IO, List[Log], String] =
        WriterT(
          IO.raiseError(throw new RuntimeException)
            .map(_ => (List(Log("", "")), "")))
    }

    object Tip
        extends Tip
        with MockNotifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe

    for {
      _      <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe UnclassifiedError
  }

  it should "not affect verification result when setting of PR label fails" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr: WriterT[IO, List[Log], String] =
        WriterT(
          IO.raiseError(UnexpectedStatus(InternalServerError))
            .map(_ => (List(Log("", "")), "")))
    }

    object Tip
        extends Tip
        with MockNotifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe

    for {
      _      <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe AllTestsInProductionPassed
  }

  behavior of "happy Tip"

  it should "verify a path" in {
    Tip
      .verify("Name A")
      .map(_ mustBe PathsActorResponse(PathIsVerified("Name A")))
  }

  it should "return AllTestsInProductionPassed when all paths have been verified" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): WriterT[IO, List[Log], String] =
        mockOkResponse
    }

    object Tip
        extends Tip
        with MockNotifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe

    for {
      _      <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe AllTestsInProductionPassed
  }

  it should "not return AllTestsInProductionPassed if the same path is verified multiple times concurrently (no race conditions)" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr: WriterT[IO, List[Log], String] =
        mockOkResponse
    }

    object Tip
        extends Tip
        with MockNotifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe

    val path1 = Tip.verify("Name A") // execute in parallel
    val path2 = Tip.verify("Name A")
    val path3 = Tip.verify("Name A")
    val path4 = Tip.verify("Name A")

    for {
      _      <- path1
      _      <- path2
      _      <- path3
      result <- path4
    } yield result must not be PathsActorResponse(AllPathsAlreadyVerified)
  }

  it should "shutdown its actor system after all paths have been verified" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr: WriterT[IO, List[Log], String] =
        mockOkResponse
    }

    object Tip
        extends Tip
        with MockNotifier
        with GitHubApi
        with TipCloudApi
        with MockHttpClient
        with ConfigFromTypesafe

    val path1 = Tip.verify("Name A") // execute these in parallel
    val path2 = Tip.verify("Name B")
    val path3 = Tip.verify("Name A")
    val path4 = Tip.verify("Name B")
    val path5 = Tip.verify("Name A")
    val path6 = Tip.verify("Name B")

    for {
      _      <- Tip.verify("Name A") // execute these in sequence
      _      <- Tip.verify("Name B")
      _      <- Tip.verify("Name A")
      _      <- Tip.verify("Name B")
      _      <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe TipFinished
  }
}
