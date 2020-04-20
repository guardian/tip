# How to make a release
This repo is configured to release an artefact to the central Maven repository, following the semantic versioning policy.

## Prerequisites
To make a release you will need:
1. To be in the master branch of the repo
1. A pair of pgp keys set up locally  
See https://www.scala-sbt.org/release/docs/Using-Sonatype.html#step+1%3A+PGP+Signatures
1. Sonatype credentials stored in your local sbt configuration.  
See https://www.scala-sbt.org/release/docs/Using-Sonatype.html#step+3%3A+Credentials for details.

## Releasing library
Command is `sbt release`  
Follow the interactive prompts to set the version number of the release.

If successful, the release will eventually be found here: 
https://repo.maven.apache.org/maven2/com/gu/tip_2.12/  
It can take several hours before the new release is generally available from the central maven repo.

For reference:
https://stackoverflow.com/questions/45963559/how-to-release-a-scala-library-to-maven-central-using-sbt

## Troubleshooting

### Reverting failed release

 1. Login to https://oss.sonatype.org/#stagingRepositories
 1. Drop staging repository
 1. Remove version bump commit from master: `git reset --hard sha`
 1. Remove tag: eg. `git tag -d v0.6.2`
 
### Problems with signing

#### Expired keys
 - https://github.com/sbt/sbt-pgp/issues/158
 - `gpg --list-secret-keys`
 - `gpg --list-keys`

#### `gpg: signing failed: Inappropriate ioctl for device`
 - https://github.com/keybase/keybase-issues/issues/2798
 - `export GPG_TTY=$(tty)`
 