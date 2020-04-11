FROM nixery.dev/shell/maven/clojure

ENV HOME /builder
ENV _JAVA_OPTIONS -Duser.home=/builder

# Download deps
RUN mkdir -p /tmp/maven \
    && cd /tmp/maven \
    && clojure -Sdeps '{:deps {seancorfield/depstar {:mvn/version "1.0.94"}}}' -Spom \
    && mvn dependency:go-offline

COPY entrypoint.sh /entrypoint.sh
COPY settings.xml /builder/.m2/settings.xml

ENTRYPOINT ["bash", "/entrypoint.sh"]