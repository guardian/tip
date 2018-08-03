# How it works?

## How setting the GitHub label works?

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
