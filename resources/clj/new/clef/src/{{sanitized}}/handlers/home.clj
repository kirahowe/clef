(ns {{namespace}}.handlers.home
  (:require [integrant.core :as ig]
            [{{namespace}}.ui.pages.home :as home]))

(defmethod ig/init-key :{{namespace}}.handlers/home [_ _]
  (fn [_req]
    {:status  200
     :headers {"content-type" "text/html; charset=utf-8"}
     :body    (home/render)}))
