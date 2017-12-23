package com.gu.tip

import org.scalatest.{FlatSpec, MustMatchers}

class HttpClientTest extends FlatSpec with MustMatchers {

  object HttpClient extends HttpClient

  behavior of "HttpClient"

  it should "be able to make a GET request" in {
    HttpClient.get("http://example.com", ("Authorization","test")).unsafeRun().map(_ => succeed)
  }

  it should "be able to make a POST request" in {
    HttpClient.post(
      "http://petstore.swagger.io/v2/pet",
      ("Authorization","test"),
      "{\"name\": \"doggie\"}"
    ).map(_ => succeed)
  }

}
