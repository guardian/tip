const AWS = require('aws-sdk');
AWS.config.update({region: 'eu-west-1'});
const ddb = new AWS.DynamoDB.DocumentClient();

const getBoard = (repo) => {
    return ddb.scan(
        {
            TableName: 'TipCloud-PROD',
            FilterExpression : 'repo = :repo',
            ExpressionAttributeValues : {':repo' : `${repo}`}
        }
    ).promise();
}

const getLatestBoard = (boards) =>
    boards
        .Items
        .sort((a,b) => new Date(b.deployTime) - new Date(a.deployTime))[0];

const renderBoard = (item) => {
    return new Promise((resolve, reject) => {
        const pathsWithStatusColour =
            item.board
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

        const sha = item.sha;
        const repo = item.repo;
        const numberOfVerifiedPaths = item.board.filter( path => path.verified == true).length;
        const coverage = Math.round((100 * numberOfVerifiedPaths) / item.board.length);
        const deployTime = item.deployTime;
        const elapsedTimeSinceDeploy = Date.now() - Date.parse(deployTime);

        const linkToCommit = `https://github.com/${repo}/commit/${sha}`;

        const html = `
            <!DOCTYPE html>
            <html>
            <head>
            <meta http-equiv="refresh" content="5" >
            
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
                
                <hr>
                
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

const msToTime = (s) => {
    var ms = s % 1000;
    s = (s - ms) / 1000;
    var secs = s % 60;
    s = (s - secs) / 60;
    var mins = s % 60;
    var hrs = (s - mins) / 60;

    return hrs + ':' + mins + ':' + secs;
}

exports.handler = (event, context, callback) => {
    const owner = event.pathParameters.owner;
    const repo = event.pathParameters.repo;
    const slug = `${owner}/${repo}`;

    getBoard(slug)
        .then(boards => getLatestBoard(boards))
        .then(latestBoard => renderBoard(latestBoard))
        .then(boardAsHtml => callback(null, boardAsHtml));
};
