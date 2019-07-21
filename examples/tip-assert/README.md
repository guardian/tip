# TipAssert example

Create a pet and then as a side effect perform assertion on mutated database by fetching the state 
and checking the correct name was written. 

Note how we run assertions on a separate thread pool `assertionExecutionContext` to be extra safe:

```sbtshell
TipAssert(
  PetService.getPetName(petId)(assertionExecutionContext), // Note how we are using dedicated thread pool here just to be extra safe
  (actualName: String) => actualName == expectedPet.name,
  s"Failed to create pet $petId with correct name! Expected: $expectedPet",
  max = 3,
  delay = Duration(250, TimeUnit.MILLISECONDS)
)
```

Run with `sbt run`.


