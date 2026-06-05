(ns clef.main-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [clef.main :as main]
            [clef.version :as version]))

(defn- capture-out [f]
  (let [sw (java.io.StringWriter.)]
    (binding [*out* sw]
      (let [exit (f)]
        {:out (str sw) :exit exit}))))

(deftest version-command
  (testing "--version prints the version and exits 0"
    (let [{:keys [out exit]} (capture-out #(main/dispatch ["--version"]))]
      (is (= 0 exit))
      (is (str/includes? out version/version))))

  (testing "-v is an alias for --version"
    (let [{:keys [out exit]} (capture-out #(main/dispatch ["-v"]))]
      (is (= 0 exit))
      (is (str/includes? out version/version)))))

(deftest unknown-command-shows-usage
  (let [{:keys [out exit]} (capture-out #(main/dispatch ["nope"]))]
    (is (= 1 exit))
    (is (str/includes? out "Usage"))))

(deftest no-args-shows-usage
  (let [{:keys [out exit]} (capture-out #(main/dispatch []))]
    (is (= 1 exit))
    (is (str/includes? out "Usage"))))
