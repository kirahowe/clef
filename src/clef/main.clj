(ns clef.main
  (:require [clef.new :as new]
            [clef.version :as version]))

(defn- print-version [_args]
  (println version/version)
  0)

(defn- print-usage [_args]
  (println "Usage: clef <command> [args]")
  (println)
  (println "Commands:")
  (println "  new <name>      Generate a new clef app in ./<name>")
  (println "  --version, -v   Print version")
  1)

(def commands
  {"new"       new/run
   "--version" print-version
   "-v"        print-version})

(defn dispatch [argv]
  (let [[cmd & rest] argv
        handler (get commands cmd print-usage)]
    (handler rest)))

(defn -main [& argv]
  (System/exit (dispatch (vec argv))))
