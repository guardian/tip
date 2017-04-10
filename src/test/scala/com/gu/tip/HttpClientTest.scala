package com.gu.tip

import org.scalatest.{FlatSpec, MustMatchers}

class HttpClientTest extends FlatSpec with MustMatchers {

  behavior of "HttpClient"

  it should "be able to make a GET request" in {
    object HttpClient extends HttpClient
    val (code, body) = HttpClient.get("http://example.com", ("Authorization","test"))
    code mustBe 200
  }

  it should "be able to make a POST request" in {
    object HttpClient extends HttpClient
    val (code, body) =
      HttpClient.post("http://petstore.swagger.io/v2/pet", ("Authorization","test"), "{\"name\": \"doggie\"}")
    code mustBe 200
  }

}
