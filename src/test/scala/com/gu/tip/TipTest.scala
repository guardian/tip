package com.gu.tip

import org.scalatest.{AsyncFlatSpec, MustMatchers}

class TipTest extends AsyncFlatSpec with MustMatchers {
  trait MockHttpClient extends HttpClient {
    override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) = (200, "")
    override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") = (200, "")
  }

  object Tip extends Tip with Notifier with GitHubApi with MockHttpClient

  behavior of "unhappy Tip"

  it should "throw an exception on missing TiP configuration"

  it should "throw an exception on bad syntax in path definition file" in {
    object Tip extends Tip with Notifier with GitHubApi with MockHttpClient  {
      override val pathConfigFilename: String = "tip-bad.yaml"
    }

    assertThrows[Throwable](Tip.verify(""))
  }

  it should "throw an exception on missing path definition file" in {
    object Tip extends Tip with Notifier with GitHubApi with MockHttpClient  {
      override val pathConfigFilename: String = "tip-not-found.yaml"
    }
    assertThrows[MissingPathConfigurationFile](Tip.verify(""))
  }

  it should "handle bad path name" in {
    Tip.verify("badName").map(_ mustBe PathNotFound("badName"))
  }

  it should "handle exceptions thrown from Notifier" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = throw new RuntimeException
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe UnclassifiedError
  }

  it should "handle failure to set the label" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 500
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
     _ <- Tip.verify("Name A")
     result <- Tip.verify("Name B")
    } yield result mustBe FailedToSetLabel
  }

  behavior of "happy Tip"

  it should "verify a path" in {
    Tip.verify("Name A").map(_ mustBe PathsActorResponse(PathIsVerified("Name A")))
  }

  it should "set the label when all paths are verified" in {
    trait MockNotifier extends NotifierIf {this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 200
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe LabelSet
  }

  it should "not set the label if the same path is verified multiple times concurrently (no race conditions)" in {
    trait MockNotifier extends NotifierIf {this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 200
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    val path1 = Tip.verify("Name A") // execute in parallel
    val path2 = Tip.verify("Name A")
    val path3 = Tip.verify("Name A")
    val path4 = Tip.verify("Name A")

    for {
      _ <- path1
      _ <- path2
      _ <- path3
      result <- path4
    } yield result must not be PathsActorResponse(AllPathsAlreadyVerified)
  }

  it should "should shutdown its actor system after all paths have been verified" in {
    trait MockNotifier extends NotifierIf {this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 200
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    val path1 = Tip.verify("Name A") // execute these in parallel
    val path2 = Tip.verify("Name B")
    val path3 = Tip.verify("Name A")
    val path4 = Tip.verify("Name B")
    val path5 = Tip.verify("Name A")
    val path6 = Tip.verify("Name B")

    for {
      _ <- Tip.verify("Name A") // execute these in sequence
      _ <- Tip.verify("Name B")
      _ <- Tip.verify("Name A")
      _ <- Tip.verify("Name B")
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe TipFinished
  }
}