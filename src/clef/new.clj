(ns clef.new
  "The `clef new` command: render the embedded project templates into a
  fresh directory. Template content and paths are rendered with Selmer
  using Leiningen/clj-new-style variables, so the same templates under
  resources/clj/new/clef render identically there."
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clef.templates :as templates]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]))

;; Stencil/mustache treat {{x}} as raw text; match that so the same
;; templates work under clj-new.
(selmer-util/turn-off-escaping!)

(defn- capitalize-first [s]
  (if (str/blank? s)
    s
    (str (str/upper-case (subs s 0 1)) (subs s 1))))

(defn ->vars
  "Derive template variables from a raw project name, mirroring the
  Leiningen/clj-new naming convention:
    :name      raw name as given          (myapp / my-app)
    :namespace munged namespace           (my-app)
    :sanitized namespace as a path        (my_app)
    :title     display title              (My-app)"
  [raw-name]
  (let [ns-name   (-> raw-name str/trim (str/replace "_" "-"))
        sanitized (-> ns-name (str/replace "." "/") (str/replace "-" "_"))]
    {:name      raw-name
     :namespace ns-name
     :sanitized sanitized
     :title     (capitalize-first ns-name)}))

(def ^:private name-pattern
  #"[a-zA-Z][a-zA-Z0-9_-]*(\.[a-zA-Z][a-zA-Z0-9_-]*)*")

(defn- validate-name!
  "Throws ExceptionInfo unless `name` munges to a legal Clojure namespace
  — letters, digits, - and ., starting with a letter. This also rejects
  path separators and .., so the name is safe to use as a directory."
  [name]
  (when-not (re-matches name-pattern name)
    (throw (ex-info (str "invalid project name: " (pr-str name)
                         " — use letters, digits, - and ., starting with a letter")
                    {:name name}))))

(defn new!
  "Generate the project into ./<name> (or `:target`). Returns the target
  as a babashka.fs path. Throws ExceptionInfo if the name is not a legal
  project name, or if the target exists and is non-empty."
  [raw-name {:keys [target]}]
  (let [raw-name (str/trim raw-name)
        _        (validate-name! raw-name)
        vars     (->vars raw-name)
        dest     (fs/file (or target raw-name))]
    (when (and (fs/exists? dest) (seq (fs/list-dir dest)))
      (throw (ex-info (str "target already exists and is not empty: " dest)
                      {:target (str dest)})))
    (doseq [[path content] (sort-by key templates/files)]
      (let [file (fs/file dest (selmer/render path vars))]
        (fs/create-dirs (fs/parent file))
        (spit file (selmer/render content vars))))
    dest))

(defn run
  "CLI entry for `clef new <name>`. Returns a process exit code."
  [args]
  (let [raw-name (first args)]
    (if (str/blank? raw-name)
      (do (println "Usage: clef new <name>")
          1)
      (try
        (let [dest (new! raw-name {})]
          (println (str "Created " dest))
          (println)
          (println "Next steps:")
          (println (str "  cd " dest))
          (println "  clj -M:dev      # start a REPL, then (dev) (go)")
          (println "  clj -M:test     # run the tests")
          0)
        (catch clojure.lang.ExceptionInfo e
          (println (str "Error: " (ex-message e)))
          1)))))
