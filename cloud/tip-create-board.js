const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

function registerBoard(sha, board) {
    return ddb.put(
        {
            TableName: 'tipcloud',
            Item: {
                sha: sha,
                board: board
            }
        }
    ).promise();
}

exports.handler = (event, context, callback) => {
    const body = JSON.parse(event.body);
    const board = body.board;
    const sha = body.sha;

    registerBoard(sha, board)
        .then(() => callback(null, {statusCode: 200, body: `{"field": "value"}`}));
};
