(ns clef.gen-script
  (:require [clojure.string :as str]))

(defn gen-script
  "Concatenate clef's source namespaces into a single bb-runnable script at ./clef.
   Order matters: version.clj must precede main.clj because main requires version."
  []
  (let [prelude     (slurp "prelude")
        version-src (slurp "src/clef/version.clj")
        main-src    (slurp "src/clef/main.clj")
        invocation  "(apply clef.main/-main *command-line-args*)"
        parts       [prelude version-src main-src invocation]
        script      (str/join "\n\n" parts)]
    (spit "clef" script)
    (.setExecutable (java.io.File. "clef") true)
    (println "Wrote ./clef")))
