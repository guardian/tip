#!/usr/bin/env bash

S3_BUCKET=identity-lambda
S3_KEY=tip-cloud.zip

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

echo "Cleaning previous package..."
rm $S3_KEY

echo "Zipping code..."
zip -r $S3_KEY *

echo "Copying to S3 bucket..."
aws s3 cp $S3_KEY "s3://${S3_BUCKET}/"

echo "Updating Lambda code"
aws lambda update-function-code --function-name tip-create-board --s3-bucket $S3_BUCKET --s3-key $S3_KEY
aws lambda update-function-code --function-name tip-get-board --s3-bucket $S3_BUCKET --s3-key $S3_KEY
aws lambda update-function-code --function-name tip-get-head-board --s3-bucket $S3_BUCKET --s3-key $S3_KEY
aws lambda update-function-code --function-name tip-verify-path --s3-bucket $S3_BUCKET --s3-key $S3_KEY
aws lambda update-function-code --function-name tip-verify-head-path --s3-bucket $S3_BUCKET --s3-key $S3_KEY

echo "Done."