(ns entrypoint
  (:require
    [clojure.data.xml :as xml]
    [clojure.tools.deps.alpha.script.generate-manifest2 :as gen-manifest]
    [clojure.zip :as zip]
    [clojure.data.zip.xml :as zip-xml]
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
    [hf.depstar.uberjar :refer [uber-main]]))
    ; [clojure.java.shell :refer [sh]])
  ; (:import
  ;   (org.apache.maven.shared.invoker DefaultInvoker DefaultInvocationRequest)
  ;   (java.util Collections)))

(def github-ref (or (System/getenv "GITHUB_REF") "refs/UNKNOWN"))
(def version (cond-> (last (str/split github-ref #"/"))
               (not (str/starts-with? github-ref "refs/tags/"))
               (str "-SNAPSHOT")))

; (comment
;   (meta (edn/read-string (slurp "./deps.edn"))))

(defn nav-xml
  [xml path]
  (let [f #(apply zip-xml/xml1-> % path)]
    (some-> xml
      (xml/parse-str :namespace-aware false)
      (zip/xml-zip)
      f)))
      ; (zip-xml/text))))
      ; (zip-xml/xml1-> :project :name))))

(defn get-content
  [xml & path]
  (some-> (nav-xml xml path)
      (zip-xml/text)))

(defn update-content
  [xml value & path]
  (some-> (nav-xml xml path)
    (zip/edit #(assoc % :content value))
    (zip/root)
    (xml/emit-str)))
    ; (xml/indent-str)))

(defn file->str
  [& parts]
  (let [path (apply str parts)
        f (io/file (apply str path))]
    (when (.exists f)
      (slurp f))))

(comment
  (def cwd "/home/jlle/projects/dummy-clj"))

(defn -main
  [& args]
  (let [; cwd "."
        deps (-> (file->str cwd "/deps.edn") edn/read-string)
        ; xml (some-> (file->str cwd "/pom.xml") xml/parse-str)
        xml (file->str cwd "/pom.xml")
        project-name (get-content xml :project :name)
        jar-name (str project-name "-" version ".jar")
        new-pom (-> xml
                    (update-content version :project :version)
                    (update-content version :project :scm :tag))]

   ; new-pom
    ; (indent-xml new-pom)
    (spit (str cwd "/pom.xml") new-pom)

    ; https://github.com/clojure/brew-install/blob/2ee355398e655e1d1b57e4f5ee658d087ccaea7f/src/main/resources/clojure#L342
    ; (print (:out (sh "clojure" "-Spom")))
    (gen-manifest/-main "--config-project" (str cwd "/deps.edn") "--gen" "pom")))

    ; (uber-main {:dest (str cwd "/target/" jar-name) :jar :thin} "-v")))

    ; clojure -Sdeps '{:deps {seancorfield/depstar {:mvn/version "1.0.94"}}}' \
    ;   -m hf.depstar.jar target/"${jar_name}" -v
    ; mvn deploy:deploy-file -Dfile="target/${jar_name}" -DpomFile=pom.xml \
    ;   -DrepositoryId=clojars -Durl=https://clojars.org/repo/ \
    ;   -Dclojars.username="${CLOJARS_USERNAME}" \
    ;   -Dclojars.password="${CLOJARS_PASSWORD}"


(comment


  (System/getProperty "user.dir")
  (System/getProperties)
  (System/setProperty "user.dir" "/home/jlle/projects/dummy-clj"))

(comment
  (defn tag-name? [tag tname]
    (some-> tag :tag name #{tname}))

  (defn tag-content-str [tag]
    (when tag
      (->> tag :content (filter string?) (str/join ""))))

  (defn project-name
    ([element-name] (project-name element-name "pom.xml"))
    ([element-name path]
     (->>
      (slurp path)
      (xml/parse-str)
      (xml-seq)
      (filter #(tag-name? % element-name))
      first
      tag-content-str))))

; (if-let [arg (first *command-line-args*)]
;   (pom-version arg)
;   (pom-version))

; (System/getenv "USERNAME")

; (comment

;   (let [mvn-home "/nix/store/473r38zk0xf7hn45ws10qnhgpyfzr63f-apache-maven-3.6.3"
;         goal "help"
;         f (io/file "/home/jlle/projects/clojars-release-action/files/pom.xml")
;         req (doto (DefaultInvocationRequest.)
;                   (.setPomFile f)
;                   (.setGoals (Collections/singletonList goal)))
;         inv (doto (DefaultInvoker.)
;                   (.setMavenHome (io/file mvn-home)))
;         result (.execute inv req)]
;     (str "Exit code for " goal ": " (.getExitCode result)))




;   (str (project-name "name") "-" version)

;   (require '[clojure.data.zip.xml :as zipxml])

;   (project-name "id" "/home/jlle/projects/clojars-release-action/files/pom.xml")

;   ; (some-> (slurp "/home/jlle/projects/clojars-release-action/files/pom.xml"))

;   (get-content (slurp "./files/pom.xml") :project :name)

;   (some-> (slurp "./files/pom.xml")
;     (xml/parse-str :namespace-aware false)
;     (zip/xml-zip)
;     (zip-xml/xml1-> :project :name)
;     (zip-xml/text))

;   ; (def r
;   (some-> (slurp "/home/jlle/projects/clojars-release-action/files/pom.xml")
;    (xml/parse-str :namespace-aware false)
;    (zip/xml-zip)
;    (zip-xml/xml1-> :project :name)
;    (zip/edit #(assoc % :content "a new name"))
;    (zip/root)
;    (xml/indent-str))
;     ; (zipxml/text))

;   ; (with-open [f (clojure.java.io/writer  "/tmp/books_with_prices.xml")]
;   ;   (xml/emit r f))

;   (-> (slurp "/home/jlle/projects/clojars-release-action/files/pom.xml")
;    (xml/parse-str :coalescing false)
;    (xml-seq)))
;    ; :content))


;     ; InvocationRequest request = new DefaultInvocationRequest();
;     ; request.setPomFile( new File( "/path/to/pom.xml"));
;     ; request.setGoals( Collections.singletonList( "install"));

;     ; Invoker invoker = new DefaultInvoker();
;     ; invoker.execute( request)));
