(ns {{namespace}}.ui.pages.home
  (:require [{{namespace}}.ui.layout :as layout]))

(defn render []
  (layout/page
   "{{title}}"
   [:main
    [:h1 "{{title}}"]
    [:p "Welcome to {{name}} — a clef-generated app."]
    [:p.muted "v0.0.1"]]))
