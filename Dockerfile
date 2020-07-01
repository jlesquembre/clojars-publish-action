FROM clojars-releaser-tmp:latest

# Download deps
RUN cd /tmp/maven \
    && mvn versions:help deploy:help \
    && mvn dependency:go-offline \
    && rm -rf /tmp/maven
