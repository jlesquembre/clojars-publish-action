# Clojars release action

A Github action to release Clojure tools.deps projects to clojars.

It creates a new release on clojars, where the version is the name of your tag.
It can also create snapshot releases on every push, in this case, the version
will be `${BRANCH_NAME}-SNAPSHOT`.

A minimal `pom.xml` is expected in the project root, e.g.:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${GROUP_ID}</groupId>
  <artifactId>${ARTIFACT_ID}</artifactId>
  <version>LATEST</version>
  <name>${PROJECT_NAME}</name>
  <description>A description</description>
  <url>https://github.com/$USER/$PROJECT</url>

  <licenses>
    <license>
      <name>Eclipse Public License</name>
      <url>http://www.eclipse.org/legal/epl-v10.html</url>
    </license>
  </licenses>

  <developers>
    <developer>
      <name>Your name</name>
    </developer>
  </developers>

  <scm>
    <url>https://github.com/$USER/$PROJECT</url>
    <connection>scm:git:git://github.com/$USER/$PROJECT.git</connection>
    <developerConnection>scm:git:ssh://git@github.com/$USER/$PROJECT.git</developerConnection>
    <tag>HEAD</tag>
  </scm>

</project>
```

Notice that your `pom.xml` is used as a template, you don't need to define your
dependencies nor your source directory, since that data will be generated from
your `deps.edn` (via `clojure -Spom`).

Also, `version` and `smc/tag` values don't matter, since they will be replaced
with the data from git (tag or branch name).

## Environment variables

### `CLOJARS_USERNAME`

**Required** Your clojars username

### `CLOJARS_PASSWORD`

**Required** Your clojars token. As of 2020-06-27, Clojars will no longer accept
your Clojars password, you have to use a token instead. Read more about this
here:
[Clojars wiki: Deploy Tokens](https://github.com/clojars/clojars-web/wiki/Deploy-Tokens)

To learn about about GitHub secrets, see
[Creating and storing encrypted secrets on GitHub help](https://help.github.com/en/actions/configuring-and-managing-workflows/creating-and-storing-encrypted-secrets)

## Usage

`.github/workflows/release.yaml`:

```yaml
on:
  push:
    branches:
      - master
    tags:
      - "*"

jobs:
  clojars:
    runs-on: ubuntu-latest

    steps:
      # This step checks out a copy of your repository.
      - uses: actions/checkout@v2

      - uses: jlesquembre/clojars-publish-action@master
        env:
          CLOJARS_USERNAME: ${{ secrets.CLOJARS_USERNAME }}
          CLOJARS_PASSWORD: ${{ secrets.CLOJARS_PASSWORD }}
```
