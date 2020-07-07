{ pkgs ? import (fetchTarball https://github.com/NixOS/nixpkgs/archive/22a81aa5fc15b2d41b12f7160a71cd4a9f3c3fa1.tar.gz) { } }:
let
  maven = pkgs.maven.override { jdk = pkgs.jdk11_headless; };
  clojure = pkgs.clojure.override { jdk11 = pkgs.jdk11_headless; };
  entrypoint = ./files/entrypoint.sh;
  mvn-settings = ./files/settings.xml;
  pom = ./files/pom.xml;
  home-dir = "home/builder";

  babashka =
    let
      version = "0.1.3";
    in
    self.stdenv.mkDerivation {
      inherit version;
      pname = "babashka-bin";
      dontBuild = true;
      installPhase = ''
        mkdir -p $out/bin
        cp bb $out/bin
      '';
      src = self.fetchzip {
        url = "https://github.com/borkdude/babashka/releases/download/v${version}/babashka-${version}-linux-static-amd64.zip";
        sha256 = "0jxxryx5a0jv405i3ch9n08di4ryv9wyfb3ibh7s20ccijlfj35p";
      };
    };

  deps-img = pkgs.dockerTools.buildImage {
    name = "clojure-maven";
    tag = "latest";
    contents = [
      pkgs.coreutils
      pkgs.bash
      maven
      clojure
      babashka
    ];
  };
in
pkgs.dockerTools.buildImage {
  name = "clojars-releaser-tmp";
  tag = "latest";

  fromImage = deps-img;

  config.Env = [
    "HOME=/${home-dir}"
    "_JAVA_OPTIONS=-Duser.home=/${home-dir}"
  ];

  # runAsRoot = ''
  extraCommands = ''
    mkdir -p ${home-dir}/.m2
    cp ${mvn-settings} ${home-dir}/.m2/settings.xml

    mkdir -p tmp/maven
    cp ${pom} tmp/maven/pom.xml
  '';

  config.Cmd = [ "${pkgs.bash}/bin/bash" entrypoint ];
  config.WorkingDir = "/${home-dir}";
}
