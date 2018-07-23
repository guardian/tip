package com.gu.tip.cloud

import cats.data.WriterT
import cats.effect.IO
import cats.implicits._
import com.gu.tip.{Configuration, HttpClientIf, Log}
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._
import net.liftweb.json.DefaultFormats

trait TipCloudApiIf { this: HttpClientIf =>
  def createBoard(sha: String): WriterT[IO, List[Log], String]
  def verifyPath(sha: String, name: String): WriterT[IO, List[Log], String]
  def getBoard(sha: String): WriterT[IO, List[Log], String]

  val tipCloudApiRoot =
    "https://1g3v0a5b5h.execute-api.eu-west-1.amazonaws.com/PROD"
}

trait TipCloudApi extends TipCloudApiIf with LazyLogging { this: HttpClientIf =>

  if (Configuration.tipConfig.cloudEnabled) {
    createBoard("firstprodtest").run.attempt.unsafeRunSync()
  }

  override def createBoard(sha: String): WriterT[IO, List[Log], String] = {
    val paths = Configuration.readPaths("tip.yaml")

    val board = paths.map { path =>
      s"""
         | {
         |   "name": "${path.name}",
         |   "verified": false
         | }
      """.stripMargin
    }

    val body =
      s"""
         |{
         |   "sha": "${sha}",
         |   "board": [
         |     ${board.mkString(",")}
         |   ]
         | }
        """.stripMargin

    post(s"$tipCloudApiRoot/board", auth, compactRender(parse(body)))
      .tell(List(Log("INFO", s"Successfully created board $sha")))
  }

  override def verifyPath(sha: String,
                          name: String): WriterT[IO, List[Log], String] = {
    val queryString = s"sha=$sha&name=$name"

    post(s"$tipCloudApiRoot/board/path?$queryString", auth, "")
      .tell(
        List(Log("INFO", s"Successfully verified path $name on board $sha")))
  }

  override def getBoard(sha: String): WriterT[IO, List[Log], String] =
    get(s"$tipCloudApiRoot/board/$sha", auth)
      .tell(List(Log("INFO", s"Successfully retrieved board $sha")))

  private lazy val auth = "Authorization" -> "Hello world"
}
