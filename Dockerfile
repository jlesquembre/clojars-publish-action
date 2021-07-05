FROM clojure:openjdk-11-tools-deps-slim-buster as builder

WORKDIR /tmp

COPY deps.edn deps.edn
COPY src src

RUN clojure -Spom
RUN clojure -X:jar :jar depstar-generated.jar :verbose true

CMD clojure -Sforce \
    -Sdeps '{:aliases {:clojars {:extra-deps {clojars-releaser/clojars-releaser {:local/root "/tmp/depstar-generated.jar"}}}}}' \
    -Mclojars -m entrypoint
