(ns entrypoint
  (:require
    [clojure.data.xml :as xml]
    [clojure.tools.deps.alpha.script.generate-manifest2 :as gen-manifest]
    [clojure.zip :as zip]
    [clojure.data.zip.xml :as zip-xml]
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [hf.depstar.uberjar :refer [build-jar]]
    [deps-deploy.deps-deploy :as deploy]))

(xml/alias-uri 'pom "http://maven.apache.org/POM/4.0.0")

(def github-ref (or (System/getenv "GITHUB_REF") "refs/UNKNOWN"))
(def use-git-ref (case (some-> "USE_GIT_REF" System/getenv str/lower-case)
                   "false" nil
                   true))

(defn nav-xml
  [xml path]
  (let [f #(apply zip-xml/xml1-> % path)]
    (some-> xml
      (xml/parse-str)
      (zip/xml-zip)
      f)))

(defn get-content
  [xml & path]
  (some-> (nav-xml xml path)
      (zip-xml/text)))

(defn update-content
  [xml value & path]
  (or (some-> (nav-xml xml path)
       (zip/edit #(assoc % :content value))
       (zip/root)
       (xml/emit-str))
      xml))

(defn get-version
  [xml]
  (if use-git-ref
    (cond-> (last (str/split github-ref #"/"))
      (not (str/starts-with? github-ref "refs/tags/"))
      (str "-SNAPSHOT"))
    (get-content xml ::pom/project ::pom/version)))

(defn file->str
  [& parts]
  (let [path (apply str parts)
        f (io/file (apply str path))]
    (when (.exists f)
      (slurp f))))

(defn update-pom-version!
  [pom version]
  (let [new-pom (-> pom
                    (update-content version ::pom/project ::pom/version)
                    (update-content version ::pom/project ::pom/scm ::pom/tag))]
    (spit "./pom.xml" new-pom)))


(defn -main
  [& args]
  (let [deps (-> (file->str "./deps.edn") edn/read-string)
        pom (file->str "./pom.xml")
        version (get-version pom)
        project-name (get-content pom ::pom/project ::pom/name)
        jar-name (str project-name "-" version ".jar")
        jar-path (str "./target/" jar-name)]

    (when use-git-ref
      (update-pom-version! pom version))

    ; https://github.com/clojure/brew-install/blob/2ee355398e655e1d1b57e4f5ee658d087ccaea7f/src/main/resources/clojure#L342
    ; (print (:out (sh "clojure" "-Spom")))
    (gen-manifest/-main "--config-project" "./deps.edn" "--gen" "pom")

    (build-jar {:jar jar-path :jar-type :thin :verbose true})

    ; mvn deploy:deploy-file -Dfile="target/${jar_name}" -DpomFile=pom.xml \
    ;   -DrepositoryId=clojars -Durl=https://clojars.org/repo/ \
    ;   -Dclojars.username="${CLOJARS_USERNAME}" \
    ;   -Dclojars.password="${CLOJARS_PASSWORD}"

    (deploy/-main "deploy" jar-path)))
