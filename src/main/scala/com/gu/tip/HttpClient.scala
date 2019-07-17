package com.gu.tip

import cats.data.WriterT
import cats.effect._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

trait HttpClientIf {
  def get(endpoint: String,
          authHeader: (String, String)): WriterT[IO, List[Log], String]
  def post(endpoint: String,
           authHeader: (String, String),
           jsonBody: String): WriterT[IO, List[Log], String]
  def patch(endpoint: String,
            authHeader: (String, String),
            jsonBody: String): WriterT[IO, List[Log], String]
}

trait HttpClient extends HttpClientIf with Http4sClientDsl[IO] {
  override def get(
      endpoint: String,
      authHeader: (String, String)): WriterT[IO, List[Log], String] = {
    val request = Request[IO](
      uri = Uri.unsafeFromString(endpoint),
      headers = Headers.of(Header(authHeader._1, authHeader._2)),
      method = Method.GET
    )

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
    val request = POST.apply(jsonBody,
                             Uri.unsafeFromString(endpoint),
                             Header(authHeader._1, authHeader._2))

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

  override def patch(endpoint: String,
                     authHeader: (String, String),
                     jsonBody: String): WriterT[IO, List[Log], String] = {
    val request = PATCH(jsonBody,
                        Uri.unsafeFromString(endpoint),
                        Header(authHeader._1, authHeader._2))

    WriterT(
      client.expect[String](request).map { response =>
        (
          List(
            Log("INFO",
                s"Successfully executed HTTP PATCH request to $endpoint")),
          response
        )
      }
    )
  }

  implicit def ec: ExecutionContext

  // Lazy val to avoid a null pointer exception from using the execution context before its defined.
  implicit lazy val cs: ContextShift[IO] = IO.contextShift(ec)

  // Lazy val to avoid a null pointer exception from using the execution context before its defined.
  private lazy val client = {
    // Right-hand value is used to close the client, which we are not concerned about.
    val (_client, _) =
      BlazeClientBuilder[IO](ec).resource.allocated.unsafeRunSync()
    _client
  }
}
