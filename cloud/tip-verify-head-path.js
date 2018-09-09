const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

function updateBoard(dbItem) {
    ddb.put(
        {
            TableName: 'TipCloud-PROD',
            Item: dbItem
        }
    ).promise();
}

const getBoards = (repo) => {
    return ddb.scan(
        {
            TableName: 'TipCloud-PROD',
            FilterExpression : 'repo = :repo',
            ExpressionAttributeValues : {':repo' : `${repo}`}
        }
    ).promise();
}

function verifyPath(boards, path) {
    return new Promise((resolve, reject) => {
        const sortedBoards = boards.Items.sort((a,b) => new Date(b.deployTime) - new Date(a.deployTime));
        const headBoard = sortedBoards[0];
        const index = headBoard.board.findIndex(element => element.name === path);
        headBoard.board[index] = { name: path, verified: true };
        resolve(headBoard);
    });
}

exports.handler = (event, context, callback) => {
    const owner = event.pathParameters.owner;
    const repo = event.pathParameters.repo;
    const slug = `${owner}/${repo}`;

    const body = JSON.parse(event.body);
    const name = body.name;

    getBoards(slug)
        .then(data => verifyPath(data, name))
        .then(dbItem => updateBoard(dbItem))
        .then(() => callback(null, {statusCode: 200, body: null}));
};