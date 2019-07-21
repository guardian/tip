package example

import com.gu.tip.assertion.TipAssert
import com.gu.tip.assertion.ExecutionContext.assertionExecutionContext
import java.util.concurrent.TimeUnit
import com.gu.tip.assertion.AssertionResult
import scala.concurrent.duration.Duration
import scala.concurrent.Future

object validatePetCreation {
  def apply(petId: Long, expectedPet: Pet): Future[AssertionResult] = {
    TipAssert(
      PetService.getPetName(petId)(assertionExecutionContext), // Note how we are using dedicated thread pool here just to be extra safe
      (actualName: String) => actualName == expectedPet.name,
      //      (actualName: String) => "badName" == expectedPet.name, // uncomment this to see what happens if assertion fails
      s"Failed to create pet $petId with correct name! Expected: $expectedPet",
      max = 3,
      delay = Duration(250, TimeUnit.MILLISECONDS)
    )
  }
}
