# Testing in Production (TiP) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12) [![Build Status](https://travis-ci.org/guardian/tip.svg?branch=master)](https://travis-ci.org/guardian/tip) [![Coverage Status](https://coveralls.io/repos/github/guardian/tip/badge.svg?branch=master)](https://coveralls.io/github/guardian/tip?branch=master)

How to verify the most important user journeys are not broken without writing a single test?

* First time a production user hits a critical path the corresponding square on the board lights up green:
![board_example](https://user-images.githubusercontent.com/13835317/43644305-342da90c-9726-11e8-8563-026403792153.png)


* Once all paths have been hit, label is set on the corresponding pull request:
![pr_label_example](https://user-images.githubusercontent.com/13835317/43644948-5ec1e7bc-9728-11e8-9b49-f4f095522811.png)

## User Guide

1. Add [library](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12) to your application's dependencies:
    ```
    libraryDependencies += "com.gu" %% "tip" % "0.4.5"
    ```
1. List your critical paths in `tip.yaml` file and make sure it is on the classpath:
    ```
    - name: Buy Subscription
      description: User completes subscription purchase journey

    - name: Register Account
      description: User completes account registration journey
    ```
1. Put `tip.verify("My Path Name"")` statement at the point on the path where you consider path has been successfully completed.
1. Visit `https://<tip cloud domain>/{owner}/{repo}/boards/head` to monitor verification in real-time.
1. Example Tip configuration, which uses [`sbt-buildinfo`](https://github.com/sbt/sbt-buildinfo) to set `boardSha`:
    ```scala
      @Provides
      @Singleton
      def getTip(config: Config): Tip = {
        val tipConfig = TipConfig(
          owner = "guardian",
          repo = "identity",
          personalAccessToken = config.Tip.personalAccessToken, // set to empty string "" if you do not need GitHub label functionality
          label = "Verified in PROD",
          boardSha = BuildInfo.GitHeadSha
        )
    
        if (config.App.stage == "PROD")
          TipFactory.create(tipConfig)
        else
          TipFactory.createDummy(tipConfig)
      }
    ```
    ```scala
    lazy val buildInfoSettings = Seq(
      buildInfoKeys := Seq[BuildInfoKey](
        BuildInfoKey.constant("GitHeadSha", "git rev-parse HEAD".!!.trim)
      ),
      buildInfoPackage := "com.gu.identity.api",
      buildInfoOptions += BuildInfoOption.ToMap
    )

    ```
If you want Tip to notify when all paths have been hit by setting a label on the corresponding merged PR, then  
1. [Create a GitHub label](https://help.github.com/articles/creating-and-editing-labels-for-issues-and-pull-requests/), for instance, a green label with name `Verified in PROD`:
![label_example](https://cloud.githubusercontent.com/assets/13835317/24609160/a1332296-1871-11e7-8bc7-e325c0be7b93.png)
1. [Create a GitHub personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) with at least `public_repo` scope. **Keep this secret!**
1. Set `personalAccessToken` in `TipConfig`
1. Otherwise, if you do not need the label functionality, set  `personalAccessToken` to empty string `""`
