# Testing in Production (TiP) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12) [![Build Status](https://travis-ci.org/guardian/tip.svg?branch=master)](https://travis-ci.org/guardian/tip) [![Coverage Status](https://coveralls.io/repos/github/guardian/tip/badge.svg?branch=master)](https://coveralls.io/github/guardian/tip?branch=master)

How to verify user journeys are not broken without writing a single test?

* First time a production user completes a path the corresponding square on the board lights up green:
![board_example](https://user-images.githubusercontent.com/13835317/43644305-342da90c-9726-11e8-8563-026403792153.png)

* Once all paths have been completed, label is set on the corresponding pull request:
![pr_label_example](https://user-images.githubusercontent.com/13835317/43644948-5ec1e7bc-9728-11e8-9b49-f4f095522811.png)
and a message is written to logs `All tests in production passed.`

## User Guide

### Tip.verify

#### [Minimal configuration](examples/tip-minimal/README.md)

1. Add [library](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12) to your application's dependencies:
    ```
    libraryDependencies += "com.gu" %% "tip" % "0.6.1"
    ```
1. List paths to be covered in `tip.yaml` file and make sure it is on the classpath:
    ```
    - name: Register
      description: User creates an account

    - name: Update User
      description: User changes account details
    ```
1. Instantiate `Tip` with `TipConfig`:  
    ```scala
    val tipConfig = TipConfig(repo = "guardian/identity", cloudEanbled = false)
    TipFactory.create(tipConfig)
    ```
1. Call `tip.verify("My Path Name"")` at the point where you consider path has been successfully completed.

#### Configuration with cloud enabled - single board

1. Instantiate `Tip` with `TipConfig` (which by default enables cloud):: 
    ```scala
    val tipConfig = TipConfig("guardian/identity")
    TipFactory.create(tipConfig)
    ```
1. Call `tip.verify("My Path Name"")` at the point where you consider path has been successfully completed.
1. Access board at `<tip cloud domain>/{owner}/{repo}/boards/head` to monitor verification in real-time.
    
#### Setting a label on PR
Optionally, if you want Tip to notify when all paths have been hit by setting a label on the corresponding merged PR, then  
1. [Create a GitHub label](https://help.github.com/articles/creating-and-editing-labels-for-issues-and-pull-requests/), for instance, a green label with name `Verified in PROD`:
![label_example](https://cloud.githubusercontent.com/assets/13835317/24609160/a1332296-1871-11e7-8bc7-e325c0be7b93.png)
1. [Create a GitHub personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) with at least `public_repo` scope. **Keep this secret!**
1. Set `personalAccessToken` in `TipConfig`:
    ```scala
    TipConfig(
      repo = "guardian/identity",
      personalAccessToken = some-secret-token
    )
    ```
    
#### Board by merge commit SHA
Optionally, if you want to have a separate board for each merged PR, then
1. Set `boardSha` in `TipConfig`:
    ```
    TipConfig(
      repo = "guardian/identity",
      boardSha = some-sha-value
    )
    ```
 1. Example Tip configuration which uses [`sbt-buildinfo`](https://github.com/sbt/sbt-buildinfo) to set `boardSha`:
     ```scala
     TipConfig(
       repo = "guardian/identity",
       personalAccessToken = config.Tip.personalAccessToken, // remove if you do not need GitHub label functionality
       label = "Verified in PROD", // remove if you do not need GitHub label functionality
       boardSha = BuildInfo.GitHeadSha // remove if you need only one board instead of board per sha
     )
     ```
     build.sbt:
     ```scala
       buildInfoKeys := Seq[BuildInfoKey](
         BuildInfoKey.constant("GitHeadSha", "git rev-parse HEAD".!!.trim)
       )
       buildInfoPackage := "com.gu.identity.api"
       buildInfoOptions += BuildInfoOption.ToMap
     )
     ```
 1. Access board at `<tip cloud domain>/board/{sha}`
 
### [TipAssert](examples/tip-assert/README.md)

`TipAssert` runs an assertion on a pass-by-name value and simply logs an error on failed
assertion. The idea is to have assertions run on production behaviour off the main thread in a **separate** execution context which should not 
affect main business logic, whilst being used in combination with crash monitoring software (for example, Sentry) 
which can alert on `log.error` statement. 

Users should use a separate `ExecutionContext` dedicated just to assertions to make sure 
assertions are not starving main business logic thread pool:

```scala
import com.gu.tip.assertion.ExecutionContext.assertionExecutionContext
Future(/* request we will assert on */)(assertionExecutionContext)
```

_(Note Because `ExecutionContext` of a `Future` [cannot be changed](https://medium.com/@sderosiaux/are-scala-futures-the-past-69bd62b9c001#a10c) 
after `Future` definition, TiP cannot take care of this for the user.)_

For example, say we have a scenario where we take payments from user and want to make sure we have not double
charged them. Given the following requests returning `Future`s

```scala
def chargeUser(implicit ec: ExecutionContext): Future[_]
def getNumberOfCharges(implicit ec: ExecutionContext): Future[Int]
```

then we could check if user has been double charged with
```scala
import com.gu.tip.assertion.TipAssert
import com.gu.tip.assertion.ExecutionContext.assertionExecutionContext

chargeUser(mainExecutionContext) andThen { case _ =>
  TipAssert(
    getNumberOfCharges(assertionExecutionContext),
    (num: Int) => num == 1,
    "User should be charged only once. Fix ASAP!"
  )
}
```
`TipAssert` can also handle eventually semantics via `max` and `delay` parameters for scenarios where
 database mutation is only eventually consistent. Here is the full signature:
 
 ```scala
def apply[T](
      f: => Future[T],
      p: T => Boolean,
      msg: String,
      max: Int = 1,
      delay: FiniteDuration = 0.seconds
  ): Future[AssertionResult] 
```

Note currently `TipAssert` is not related to `Tip.verify` functionality in any way. One major semantic 
difference between the two is that `TipAssert` checks failed paths whilst `Tip.verify` checks successful paths. 

## Releasing latest version of the library
See [How to make a release](docs/how-to-release.md)
