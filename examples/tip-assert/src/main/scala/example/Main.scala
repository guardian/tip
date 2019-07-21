package example

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object Main extends App {
  val pet = Pet("Scary Dino")
  PetService.createPet(pet)
    .andThen { case Success(petId) => validatePetCreation(petId, pet) }

  Thread.sleep(10000)
}

