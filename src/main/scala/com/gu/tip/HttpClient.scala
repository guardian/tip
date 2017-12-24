package com.gu.tip

import cats.effect._
import org.http4s._
import org.http4s.client.blaze.Http1Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

trait HttpClientIf {
  def get(endpoint: String, authHeader: (String, String)): IO[String]
  def post(endpoint: String, authHeader: (String, String), jsonBody: String): IO[String]
}

trait HttpClient extends HttpClientIf with Http4sClientDsl[IO] {
  override def get(endpoint: String, authHeader: (String, String)): IO[String] = {
    val request = Request[IO](
      uri = Uri.unsafeFromString(endpoint),
      headers = Headers(Header(authHeader._1, authHeader._2)),
      method = Method.GET
    )
    client.expect[String](request)
  }

  override def post(endpoint: String, authHeader: (String, String), jsonBody: String): IO[String] = {
    val request = POST(Uri.unsafeFromString(endpoint), jsonBody).putHeaders(Header(authHeader._1, authHeader._2))
    client.expect[String](request)
  }

  private val client = Http1Client[IO]().unsafeRunSync
}
