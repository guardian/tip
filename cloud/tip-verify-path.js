const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

function updateBoard(dbItem) {
    ddb.put(
        {
            TableName: 'tipcloud',
            Item: dbItem
        }
    ).promise();
}

function getBoard(sha) {
    return ddb.get(
        {
            TableName : 'tipcloud',
            Key: {
                sha: sha
            }
        }
    ).promise();
}

function verifyPath(data, path) {
    return new Promise((resolve, reject) => {
        const index = data.Item.board.findIndex(element => element.name === path);
        data.Item.board[index] = { name: path, verified: true };
        resolve(data.Item);
    });
}

exports.handler = (event, context, callback) => {
    const body = JSON.parse(event.body);
    const sha = body.sha;
    const name = body.name;

    getBoard(sha)
        .then(data => verifyPath(data, name))
        .then(dbItem => updateBoard(dbItem))
        .then(() => callback(null, {statusCode: 200, body: null}));
};
