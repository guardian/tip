package com.gu.tip

import org.scalatest.{FlatSpec, MustMatchers}

class HttpClientTest extends FlatSpec with MustMatchers {

  object HttpClient extends HttpClient

  behavior of "HttpClient"

  it should "be able to make a GET request" in {
    HttpClient.get("http://example.com", ("Authorization","test"))
      .attempt.map(_.fold(error => fail, _ => succeed)).unsafeRunSync()
  }

  it should "be able to make a POST request" in {
    HttpClient.post(
      "https://duckduckgo.com",
      ("",""),
      "test body"
    ).attempt.map(_.fold(error => fail, _ => succeed)).unsafeRunSync()
  }

}
