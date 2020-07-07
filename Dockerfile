FROM clojure:openjdk-11-tools-deps-slim-buster as builder

WORKDIR /tmp

COPY deps.edn deps.edn
COPY src src

RUN clojure -Spom
RUN clojure -A:jar depstar-generated.jar -v

CMD clojure \
    -Sdeps '{:aliases {:clojars {:extra-deps {clojars-releaser {:local/root "/tmp/depstar-generated.jar"}}}}}' \
    -Aclojars -m entrypoint
