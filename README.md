# Testing in Production (TiP) [![Maven Central](https://img.shields.io/maven-central/v/com.gu/tip_2.12.svg?label=latest%20release%20for%202.12)](https://maven-badges.herokuapp.com/maven-central/com.gu/tip_2.12) [![Build Status](https://travis-ci.org/guardian/tip.svg?branch=master)](https://travis-ci.org/guardian/tip)

How to verify the most important user journeys are not broken without writing tests?

After a fresh deploy to production, TiP verifies regression has not been introduced into _Mission Critical Paths (MCP)_ by setting a label on the latest
deployed pull request the first time production users successfully complete all MCPs.

![pr_label_example](https://cloud.githubusercontent.com/assets/13835317/24607798/534dbcfe-186b-11e7-836b-4d9a7dcae7d3.png)

MCP is the answer to: _**"What user journey should never fail?"**_

## User Guide

1. Add TiP library to your application's dependencies:
```
libraryDependencies += "com.gu" %% "tip" % "0.2.0"
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

5. Define your MCPs by creating a `tip.yaml` file, at the project base directory level, with the following format:
```
- name: Buy Subscription
  description: User completes subscription purchase journey

- name: Register Account
  description: User completes account registration journey
```

Note that the names of MCPs must be unique.

5. Place `Tip.verify("My_unique_MCP_name")` calls at the points in the source code where each MCP is successfully completed. 

For example, given the above `tip.yaml` you could place `Tip.verify("Buy Subscription")` at _Payment Thank You_ page, and `Tip.verify("Register Account")` at _Registration Confirmation_ page.

## How it Works?

When users successfully complete all the defined MCPs, then `Tip.verify()` call sends two HTTP requests to [GitHub API](https://developer.github.com/v3/):

  1. [GET request](https://developer.github.com/v3/repos/commits/#get-a-single-commit) to find out the identification number of the latest merged pull request,
  1. [POST request](https://developer.github.com/v3/issues/labels/#add-labels-to-an-issue) to actually add the label to the pull request
  
TiP will do this only once - the first time users complete all MCPs. 

TiP does not care how many users it took to complete all the MCPs, that is, they could all be completed by a single user or multiple users could complete different subsets of MCPs at different points in time.

You can also use TiP if your web app is load balanced across multiple instances. In this case TiP will 
trigger once per instance, thus if you have 3 instances, then GitHub API will be hit six times, however 
once a label is set on the PR, trying to set it again has no ill effect.

TiP is designed to mitigate risk in a continuous deployment workflow, by employing production users to provide fast verification feedback on production code.

![tip_workflow_diagram](https://cloud.githubusercontent.com/assets/13835317/24617884/2a5eee18-188d-11e7-94d9-bc6ff694ff91.jpg)
    


