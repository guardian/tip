package example

import scalaj.http.Http
import io.circe.generic.auto._
import io.circe.parser._
import scala.concurrent.{ExecutionContext, Future}

case class Pet(name: String)
case class PetCreateResponse(id: Long, name: String)
case class PetGetResponse(id: Long, name: String)

object PetService {
  def createPet(pet: Pet)(implicit executionContext: ExecutionContext): Future[Long] = Future {
    val response = Http("https://petstore.swagger.io/v2/pet")
      .header("Content-Type", "application/json")
      .header("accept", "application/json")
      .postData(
        s"""
           |{
           |  "name": "${pet.name}"
           |}
        """.stripMargin)
      .asString
      .body

    decode[PetCreateResponse](response).getOrElse(throw new RuntimeException).id
  }(executionContext)

  def getPetName(petId: Long)(implicit executionContext: ExecutionContext): Future[String] = Future {
    val response = Http(s"https://petstore.swagger.io/v2/pet/$petId")
      .header("accept", "application/json")
      .header("api_key", "special-key")
      .asString
      .body

    decode[PetGetResponse](response).getOrElse(throw new RuntimeException).name
  }(executionContext)

}
