const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

function registerBoard(sha, board, repo, commitMessage, deployTime) {
    return ddb.put(
        {
            TableName: 'tipcloud',
            Item: {
                sha: sha,
                board: board,
                repo: repo,
                commitMessage: commitMessage,
                deployTime: deployTime
            }
        }
    ).promise();
}

exports.handler = (event, context, callback) => {
    const body = JSON.parse(event.body);

    const board = body.board;
    const sha = body.sha;
    const repo = body.repo;
    const commitMessage = body.commitMessage;
    const deployTime = body.deployTime;

    registerBoard(sha, board, repo, commitMessage, deployTime)
        .then(() => callback(null, {statusCode: 200, body: `{"field": "value"}`}));
};
