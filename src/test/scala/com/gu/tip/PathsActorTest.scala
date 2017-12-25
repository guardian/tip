package com.gu.tip

import org.scalatest.{AsyncFlatSpec, Matchers}
import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

class PathsActorTest extends AsyncFlatSpec with Matchers {

  private implicit val system = ActorSystem("HelloWorld")
  implicit val ec             = system.dispatcher
  implicit val timeout        = Timeout(10.second)

  behavior of "PathsActor factory"

  it should "convert tip.yaml definition to Paths type" in {
    PathsActor("tip.yaml") shouldBe a[ActorRef]
  }

  it should "read correct number of paths from tip.yaml" in {
    PathsActor("tip.yaml") ? NumberOfPaths map {
      _ shouldBe NumberOfPathsAnswer(2)
    }
  }

  it should "throw exception if tip.yaml does not exist" in {
    assertThrows[MissingPathConfigurationFile](PathsActor("tipppp.yaml"))
  }

  it should "throw exception if tip.yaml has bad syntax" in {
    assertThrows[PathConfigurationSyntaxError](PathsActor("tip-bad.yaml"))
  }

  behavior of "PathsActor"

  it should "have no verified paths initially" in {
    PathsActor("tip.yaml") ? NumberOfVerifiedPaths map {
      _ shouldBe NumberOfVerifiedPathsAnswer(0)
    }
  }

  it should "verify an existing path" in {
    PathsActor("tip.yaml") ? Verify("Name A") map {
      _ shouldBe PathIsVerified("Name A")
    }
  }

  it should "verify an existing path only once" in {
    val paths = PathsActor("tip.yaml")

    val pathA1 = paths ? Verify("Name A")
    val pathA2 = paths ? Verify("Name A")

    for {
      _      <- pathA1
      result <- pathA2
    } yield { result shouldBe PathIsAlreadyVerified("Name A") }

    PathsActor("tip.yaml") ? Verify("Name A") map {
      _ shouldBe PathIsVerified("Name A")
    }
  }

  it should "indicate when all paths are verified" in {
    val paths = PathsActor("tip.yaml")

    val pathA = paths ? Verify("Name A")
    val pathB = paths ? Verify("Name B")

    for {
      _      <- pathA
      result <- pathB
    } yield { result shouldBe AllPathsVerified }
  }

  it should "respond with PathDoesNotExist message when it cannot find the path" in {
    PathsActor("tip.yaml") ? Verify("Misspelled Path Name") map {
      _ shouldBe PathDoesNotExist("Misspelled Path Name")
    }
  }
}
