# Testing in Production (TiP)

How to verify the most important user journey is not broken without writing a test?

After deploying a new feature to production, TiP verifies regression has not been introduced into Business Critical Path (BCP) by setting a label on the latest
deployed pull request the first time a production user successfully completes the BCP.

![pr_label_example](https://cloud.githubusercontent.com/assets/13835317/24607798/534dbcfe-186b-11e7-836b-4d9a7dcae7d3.png)

BCP is the answer to: _**"What is the single most important user journey in my product?"**_

## User Guide

1. Add TiP library to your application's dependencies:
```
libraryDependencies += "com.gu" %% "tip" % "0.1.1"
```
    
2. [Create a GitHub label](https://help.github.com/articles/creating-and-editing-labels-for-issues-and-pull-requests/), for instance, a green label with name `Verified in PROD`:
![label_example](https://cloud.githubusercontent.com/assets/13835317/24609160/a1332296-1871-11e7-8bc7-e325c0be7b93.png)
    
3. [Create a GitHub personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) with at least `public_repo` scope. **Keep this secret!**
    
4. Add the following configuration to your application's secret `*.conf` file:
```
tip {
  owner = "mario-galic"
  repo = "sandbox"
  personalAccessToken = "************"
  label = "Verified in PROD"
}
``` 
5. Place `Tip.verify()` call at the point in the source code where BCP is successfully completed, 
for instance, at "Thank You" page, or "Confirmation" page.

Here is an example real world PR that sets up TiP: https://github.com/guardian/identity-admin/pull/153

## How it Works?

`Tip.verify()` call will send two HTTP requests to GitHub API:

  1. one GET request to find out the number of the latest merged pull request,
  1. and another POST request to actually set the label on the pull request
  
TiP will do this only once - the first time user completes BCP. 

You can also use TiP if your web app is load balanced across multiple instances. In this case TiP will 
trigger once per instance, thus if you have 3 instances, then GitHub API will be hit six times, however 
once a label is set on the PR, trying to set it again has no ill effect.
    


