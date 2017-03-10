# Testing in Production (TiP)

How to verify the most important user journey is not broken without writing a test?

After deploying a new feature to production, verifies regression has not been introduced into Business Critical Path (BCP) by setting a label on the latest
deployed pull request the first time a production user successfully completes the BCP.

BCP is the answer to: _**"What is the single most important user journey in my product?"**_

## User Guide

Import the library into your web app:

    libraryDependencies += "com.gu" %% "tip" % "0.1.0"
    
Create a GitHub label, for instance, a green label with name `Pass-in-PROD`.
    
Create a GitHub personal access token with at least `public_repo` scope. **Keep this secret!**
    
Add the following configuration to your secret `application.conf`:

    tip {
      owner = "mario-galic"
      repo = "sandbox"
      personalAccessToken = "somesecret"
      label = "Pass-in-PROD"
    }
    
Place `Tip.verify()` call at the point in the source code where BCP is successfully completed, 
for instance, at "Thank You" page, or "Confirmation" page.

## How it Works?

`Tip.verify()` call will send two HTTP requests to GitHub API:

  1. one GET request to find out the number of the latest merged pull request,
  1. and another POST request to actually set the label on the pull request
  
TiP will do this only once - the first time user completes BCP. 

You can also use TiP if your web app is load balanced across multiple instances. In this case TiP will 
trigger once per instance, thus if you have 3 instances, then GitHub API will be hit six times, however 
once a label is set on the PR, trying to set it again has no ill effect.
    


