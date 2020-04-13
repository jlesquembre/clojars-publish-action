#!/usr/bin/env bash

# Get last element in path
version=${GITHUB_REF##*/}

# If not a tag, create a SNAPSHOT
if [[ "${GITHUB_REF}" != "refs/tags/"* ]]; then
  version=${version}-SNAPSHOT
fi

project_name=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
jar_name="${project_name}-${version}.jar"

clojure -Spom

mvn versions:set versions:set-scm-tag -DnewVersion="${version}" \
  -DnewTag="${version}" -DgenerateBackupPoms=false

clojure -Sdeps '{:deps {seancorfield/depstar {:mvn/version "1.0.94"}}}' \
  -m hf.depstar.jar target/"${jar_name}" -v

mvn deploy:deploy-file -Dfile="target/${jar_name}" -DpomFile=pom.xml \
  -DrepositoryId=clojars -Durl=https://clojars.org/repo/ \
  -Dclojars.username="${CLOJARS_USERNAME}" \
  -Dclojars.password="${CLOJARS_PASSWORD}"
