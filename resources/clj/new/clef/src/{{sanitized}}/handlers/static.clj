(ns {{namespace}}.handlers.static
  (:require [integrant.core :as ig]
            [reitit.ring :as ring]))

(defmethod ig/init-key :{{namespace}}.handlers/static [_ {:keys [root]}]
  (ring/create-resource-handler {:root (or root "public")}))
