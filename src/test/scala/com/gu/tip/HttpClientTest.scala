package com.gu.tip

import org.scalatest.{FlatSpec, MustMatchers}

import scala.concurrent.ExecutionContext

class HttpClientTest extends FlatSpec with MustMatchers {

  object HttpClient extends HttpClient {
    override implicit val ec: ExecutionContext =
      scala.concurrent.ExecutionContext.global
  }

  behavior of "HttpClient"

  it should "be able to make a GET request" in {
    HttpClient
      .get("https://duckduckgo.com", ("Authorization", "test"))
      .run
      .attempt
      .map(_.fold(error => fail, _ => succeed))
      .unsafeRunSync()
  }

  it should "be able to make a POST request" in {
    HttpClient
      .post(
        "https://duckduckgo.com",
        ("", ""),
        "test body"
      )
      .run
      .attempt
      .map(_.fold(error => fail, _ => succeed))
      .unsafeRunSync()
  }

}
