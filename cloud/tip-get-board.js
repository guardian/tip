const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

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

function renderBoard(data) {
    return new Promise((resolve, reject) => {
        const rows =
            data.Item.board.map(path => {
                let colour;
                if (path.verified)
                    colour = `style="background-color:green"`;
                else
                    colour = `style="background-color:grey"`;

                return `<tr ${colour}><td>${path.name}</td><td>${path.verified}</td></tr>`;
            });

        const html = `
            <!DOCTYPE html>
            <html>
            <head>
            <style>
            table {
                font-family: arial, sans-serif;
                border-collapse: collapse;
                width: 100%;
            }
            
            td, th {
                border: 1px solid #dddddd;
                text-align: left;
                padding: 8px;
            }
            
            tr:nth-child(even) {
                background-color: #dddddd;
            }
            </style>
            </head>
            <body>
            
            <h2>HTML Table</h2>
            
            <table>
              <tr>
                <th>Path</th>
                <th>Verified</th>
              </tr>
              ${rows}
            </table>
            
            </body>
            </html>
          `;

        const response = {
            statusCode: 200,
            headers: {
                'Content-Type': 'text/html',
            },
            body: html,
        };

        resolve(response);
    });
}

exports.handler = (event, context, callback) => {
    const sha = event.pathParameters.sha;

    getBoard(sha)
        .then(data => renderBoard(data))
        .then(boardAsHtml => callback(null, boardAsHtml));
};
