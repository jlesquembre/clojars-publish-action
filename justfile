IMAGE_NAME := "jlle/clojars"
VERSION := "0.4"
PWD := `pwd`

build:
  podman build -t {{IMAGE_NAME}}:{{VERSION}} .

push:
  podman push {{IMAGE_NAME}}:{{VERSION}}

bash:
   podman run -it --rm --entrypoint bash -e 'GITHUB_REF=refs/tags/0.1.0' {{IMAGE_NAME}}:{{VERSION}}

run:
   podman run -it --rm -e 'GITHUB_REF=refs/tags/1.0.0' \
   {{IMAGE_NAME}}:{{VERSION}}
