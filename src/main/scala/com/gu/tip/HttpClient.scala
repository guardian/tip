package com.gu.tip

import cats.data.WriterT
import cats.effect._
import org.http4s._
import org.http4s.client.blaze.Http1Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

trait HttpClientIf {
  def get(endpoint: String,
          authHeader: (String, String)): WriterT[IO, List[Log], String]
  def post(endpoint: String,
           authHeader: (String, String),
           jsonBody: String): WriterT[IO, List[Log], String]
}

trait HttpClient extends HttpClientIf with Http4sClientDsl[IO] {
  override def get(
      endpoint: String,
      authHeader: (String, String)): WriterT[IO, List[Log], String] = {
    println(endpoint)
    val request = Request[IO](
      uri = Uri.unsafeFromString(endpoint),
      headers = Headers(Header(authHeader._1, authHeader._2)),
      method = Method.GET
    )
    println(request.uri)
    println(request.body)
    println(request.headers)

    WriterT(
      client.expect[String](request).map { response =>
        (
          List(
            Log("INFO",
                s"Successfully executed HTTP GET request to $endpoint")),
          response
        )
      }
    )
  }

  override def post(endpoint: String,
                    authHeader: (String, String),
                    jsonBody: String): WriterT[IO, List[Log], String] = {
    println(endpoint)
    val request = POST(Uri.unsafeFromString(endpoint), jsonBody)
      .putHeaders(Header(authHeader._1, authHeader._2))

    request.map { req =>
      println(req.uri)
      println(req.body)
      println(req.headers)
    }
    WriterT(
      client.expect[String](request).map { response =>
        (
          List(
            Log("INFO",
                s"Successfully executed HTTP POST request to $endpoint")),
          response
        )
      }
    )
  }

  private val client = Http1Client[IO]().unsafeRunSync
}
