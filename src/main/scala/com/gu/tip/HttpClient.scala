package com.gu.tip

import fs2.Task
import org.http4s.client._
import org.http4s.client.blaze._
import org.http4s._
import org.http4s.dsl._

trait HttpClientIf {
  def get(endpoint: String, authHeader: (String, String)): Task[String]
  def post(endpoint: String, authHeader: (String, String), jsonBody: String): Task[String]
}

trait HttpClient extends HttpClientIf {
  override def get(endpoint: String, authHeader: (String, String)): Task[String] = {
    val request = Request(
      uri = Uri.unsafeFromString(endpoint),
      headers = Headers(Header(authHeader._1, authHeader._2)),
      method = Method.GET
    )
    client.expect[String](request)
  }

  override def post(endpoint: String, authHeader: (String, String), jsonBody: String): Task[String] = {
    val request = POST(Uri.unsafeFromString(endpoint), jsonBody).putHeaders(Header(authHeader._1, authHeader._2))
    client.expect[String](request)
  }

  private val client = PooledHttp1Client()
}
