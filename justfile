IMAGE_NAME := "jlle/clojars"
VERSION := "dev-01"
PWD := `pwd`


build:
  nix-build container.nix
  docker load -i result
  docker build -t {{IMAGE_NAME}}:{{VERSION}} .


push:
  docker push {{IMAGE_NAME}}:{{VERSION}}


bash:
   docker run -it --rm --entrypoint bash -e 'GITHUB_REF=refs/tags/0.1.0' {{IMAGE_NAME}}:{{VERSION}}

run:
   docker run -it --rm -e 'GITHUB_REF=refs/tags/1.0.0' \
   --mount type=bind,src={{PWD}}/files/pom.xml,dst=/home/builder/pom.xml \
   {{IMAGE_NAME}}:{{VERSION}}
