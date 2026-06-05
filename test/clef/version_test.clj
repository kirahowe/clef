(ns clef.version-test
  (:require [clojure.test :refer [deftest is]]
            [clef.version :as version]))

(deftest version-is-a-non-blank-semver-ish-string
  (is (string? version/version))
  (is (seq version/version))
  (is (re-matches #"\d+\.\d+\.\d+(?:-\S+)?" version/version)
      "Version should match semver-ish pattern"))
