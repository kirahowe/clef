(ns clef.new-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clef.new :as new]))

(deftest vars-derivation
  (testing "simple name"
    (is (= {:name "myapp" :namespace "myapp" :sanitized "myapp" :title "Myapp"}
           (new/->vars "myapp"))))
  (testing "hyphenated name keeps dashes in ns, underscores in path"
    (let [{:keys [namespace sanitized title]} (new/->vars "my-app")]
      (is (= "my-app" namespace))
      (is (= "my_app" sanitized))
      (is (= "My-app" title))))
  (testing "underscores normalize to dashes in the namespace"
    (is (= "my-app" (:namespace (new/->vars "my_app"))))))

(deftest generates-rendered-tree
  (let [dir    (fs/create-temp-dir {:prefix "clef-new-test"})
        target (fs/file dir "myapp")]
    (try
      (new/new! "myapp" {:target target})
      (testing "expected files exist at rendered paths"
        (doseq [p ["deps.edn"
                   "src/myapp/main.clj"
                   "src/myapp/handlers/health.clj"
                   "test/myapp/http_test.clj"
                   "resources/base-system.edn"
                   "resources/public/css/tokens.css"]]
          (is (fs/exists? (fs/file target p)) (str "missing " p))))
      (testing "content is rendered with no leftover template tags"
        (let [main (slurp (fs/file target "src/myapp/main.clj"))]
          (is (str/includes? main "(ns myapp.main"))
          (is (not (str/includes? main "{{"))))
        (let [sys (slurp (fs/file target "resources/base-system.edn"))]
          (is (str/includes? sys ":myapp.concerns/http-kit"))
          (is (not (str/includes? sys "{{")))))
      (finally (fs/delete-tree dir)))))

(deftest rejects-invalid-names
  (let [dir (fs/create-temp-dir {:prefix "clef-new-test"})]
    (try
      (testing "names that don't munge to a legal namespace are rejected"
        (doseq [bad ["123app" "my app" "../evil" "/tmp/x" "foo/bar" "foo." ""]]
          (is (thrown? clojure.lang.ExceptionInfo
                       (new/new! bad {:target (fs/file dir "proj")}))
              (str "should reject " (pr-str bad)))))
      (finally (fs/delete-tree dir)))))

(deftest refuses-nonempty-target
  (let [dir (fs/create-temp-dir {:prefix "clef-new-test"})]
    (try
      (spit (fs/file dir "occupied") "x")
      (is (thrown? clojure.lang.ExceptionInfo (new/new! "whatever" {:target dir})))
      (finally (fs/delete-tree dir)))))
