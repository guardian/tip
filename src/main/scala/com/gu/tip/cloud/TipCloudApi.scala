package com.gu.tip.cloud

import cats.data.WriterT
import cats.effect.IO
import cats.implicits._
import com.gu.tip.{ConfigurationIf, HttpClientIf, Log}
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json._

trait TipCloudApiIf { this: HttpClientIf with ConfigurationIf =>
  def createBoard(sha: String, repo: String): WriterT[IO, List[Log], String]
  def verifyPath(sha: String, name: String): WriterT[IO, List[Log], String]
  def verifyHeadPath(owner: String,
                     repo: String,
                     name: String): WriterT[IO, List[Log], String]
  def getBoard(sha: String): WriterT[IO, List[Log], String]
}

trait TipCloudApi extends TipCloudApiIf with LazyLogging {
  this: HttpClientIf with ConfigurationIf =>

  val tipCloudApiRoot =
    "https://be9p0izsnc.execute-api.eu-west-1.amazonaws.com/PROD"

  override def createBoard(sha: String,
                           repo: String): WriterT[IO, List[Log], String] = {
    val paths = configuration.readPaths("tip.yaml")

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
         |   "sha": "${if (sha.nonEmpty) sha else repo}",
         |   "repo": "$repo",
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
    val body =
      s"""
         |{
         |  "sha": "$sha",
         |  "name": "$name"
         |}
       """.stripMargin

    post(s"$tipCloudApiRoot/board/path", auth, compactRender(parse(body)))
      .tell(
        List(Log("INFO", s"Successfully verified path $name on board $sha")))
  }

  override def verifyHeadPath(owner: String,
                              repo: String,
                              name: String): WriterT[IO, List[Log], String] = {
    val body =
      s"""
         |{
         |  "name": "$name"
         |}
       """.stripMargin

    patch(s"$tipCloudApiRoot/$owner/$repo/boards/head/paths",
          auth,
          compactRender(parse(body)))
      .tell(
        List(
          Log("INFO", s"Successfully verified path $name on head board $repo")))
  }

  override def getBoard(sha: String): WriterT[IO, List[Log], String] =
    get(s"$tipCloudApiRoot/board/$sha", auth)
      .tell(List(Log("INFO", s"Successfully retrieved board $sha")))

  private lazy val auth = "Authorization" -> "Hello world"
}
