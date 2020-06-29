{ pkgs ? import (fetchTarball https://github.com/NixOS/nixpkgs/archive/22a81aa5fc15b2d41b12f7160a71cd4a9f3c3fa1.tar.gz) { }
}:
let
  maven = pkgs.maven.override { jdk = pkgs.jdk11_headless; };
  clojure = pkgs.clojure.override { jdk11 = pkgs.jdk11_headless; };
  entrypoint = ./entrypoint.sh;
  mvn-settings = ./settings.xml;
  home-dir = "/home/builder";
in
pkgs.dockerTools.buildImage {
  name = "clojars-releaser";
  tag = "latest";
  contents = [
    pkgs.coreutils
    pkgs.bash
    maven
    clojure
  ];
  config.Env = [
    "HOME=${home-dir}"
    "_JAVA_OPTIONS=-Duser.home=${home-dir}"
  ];
  runAsRoot = ''
    mkdir -p ${home-dir}/.m2
    cp ${mvn-settings} ${home-dir}/.m2/settings.xml
  '';
  config = {
    Cmd = [ "${pkgs.bash}/bin/bash" ];
  };
}
