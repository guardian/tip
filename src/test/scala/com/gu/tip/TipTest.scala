package com.gu.tip

import org.scalatest.{AsyncFlatSpec, MustMatchers}

class TipTest extends AsyncFlatSpec with MustMatchers {
  trait MockHttpClient extends HttpClient {
    override def get(endpoint: String = "", authHeader: (String, String) = ("", "")) = (200, "")
    override def post(endpoint: String = "", authHeader: (String, String) = ("", ""), jsonBody: String = "") = (200, "")
  }

  object Tip extends Tip with Notifier with GitHubApi with MockHttpClient

  behavior of "unhappy Tip"

  it should "error on bad path name" in {
    Tip.verify("badName").map(_ mustBe UnrecognizedPathName)
  }

  it should "error on bad syntax path definition" in {
    object Tip extends Tip with Notifier with GitHubApi with MockHttpClient {
      override val pathConfigFilename: String = "tip-bad.yaml"
    }

    Tip.verify("").map( _ mustBe PathDefinitionSyntaxError)
  }

  it should "error on missing path definition file" in {
    object Tip extends Tip with Notifier with GitHubApi with MockHttpClient {
      override val pathConfigFilename: String = "tip-not-found.yaml"
    }
    Tip.verify("").map(_ mustBe PathDefinitionFileNotFound)
  }

  it should "error on un-classifed error" in {
    trait MockNotifier extends NotifierIf { this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = throw new RuntimeException
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe UnclassifiedError
  }

  it should "error on FailedToSetLabel" in {
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
    Tip.verify("Name A").map(_ mustBe PathVerified)
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

  it should "set the label only once" in {
    trait MockNotifier extends NotifierIf {this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 200
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
      _ <- Tip.verify("Name A")
      _ <- Tip.verify("Name B")
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name B")
    } yield result mustBe LabelAlreadySet
  }

  it should "not set the label if the same path is verified multiple times" in {
    trait MockNotifier extends NotifierIf {this: GitHubApiIf =>
      override def setLabelOnLatestMergedPr(): Int = 200
    }

    object Tip extends Tip with MockNotifier with GitHubApi with MockHttpClient

    for {
      _ <- Tip.verify("Name A")
      _ <- Tip.verify("Name A")
      result <- Tip.verify("Name A")
    } yield result must not be LabelAlreadySet
  }
}