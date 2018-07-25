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
        const pathsWithStatusColour =
            data.Item.board
                .map(path => {
                    let colour;
                    if (path.verified)
                        colour = `style="background-color:green"`;
                    else
                        colour = `style="background-color:grey"`;

                    return `<span ${colour}>${path.name}</span>`;
                })
                .toString()
                .replace (/,/g, "");

        const sha = data.Item.sha;
        const repo = data.Item.repo;
        const commitMessage = data.Item.commitMessage.replace(/\n/g, '<br />');
        const numberOfVerifiedPaths = data.Item.board.filter( path => path.verified == true).length;
        const coverage = (100 * numberOfVerifiedPaths) / data.Item.board.length;
        const deployTime = data.Item.deployTime
        const elapsedTimeSinceDeploy = Date.now() - Date.parse(deployTime);

        const linkToCommit = `https://github.com/${repo}/commit/${sha}`

        const html = `
            <!DOCTYPE html>
            <html>
            <head>
                       
            
            <style>
            
                body {
                  background-color: whitesmoke;
                  color:lightgrey;
                  font-family: "Courier New", Courier, monospace
                }
                
                span {
                    display: inline-block;
                    border: 1px solid;
                    margin: 4px;
                    height: 70px;
                    width: 155px;
                    text-align: center;
                    vertical-align: middle;
                    font-weight: bold;
                    font-family: "Courier New", Courier, monospace;
                }
                
                #myProgress {
                    width: 100%;
                    background-color: grey;
                }
                
                #myBar {
                    width: 79%;
                    height: 30px;
                    background-color: green;
                }
                
                .barcontainer {
                  width: 100%;
                  background-color: #ddd;
                }
                
                .progressbar {
                  text-align: right;
                  padding-right: 20px;
                  line-height: 30px;
                  color: white;
                }
                
                .coverageprogress {width: ${coverage}%; background-color: #4CAF50;}
                
                a {
                  color: lightgrey;
                }
                
                #container{
                  max-width: 990px;
                  margin: auto;
                  background: #282828;
                  padding: 10px;
                }
                
            
            </style>
            </head>
            
            <body>
            
            <div id="container">
                <h3>
                <a href="${linkToCommit}">${repo} ${sha}</a>
                
                </h3>
                <p>
                ${commitMessage}
                </p> 
                
                <p>
                Elapsed time since deploy: <time>${msToTime(elapsedTimeSinceDeploy)}</time> 
                </p>
                
                <div class="barcontainer">
                  <div class="progressbar coverageprogress">${coverage}%</div>
                </div>
                
                <br>
                
                ${pathsWithStatusColour}
            </div>
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

function msToTime(s) {
    var ms = s % 1000;
    s = (s - ms) / 1000;
    var secs = s % 60;
    s = (s - secs) / 60;
    var mins = s % 60;
    var hrs = (s - mins) / 60;

    return hrs + ':' + mins + ':' + secs;
}

exports.handler = (event, context, callback) => {
    const sha = event.pathParameters.sha;

    getBoard(sha)
        .then(data => renderBoard(data))
        .then(boardAsHtml => callback(null, boardAsHtml));
};
