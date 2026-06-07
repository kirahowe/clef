(ns {{namespace}}.concerns.http-kit
  (:require [integrant.core :as ig]
            [org.httpkit.server :as http]
            [taoensso.telemere :as tel]))

(defmethod ig/init-key :{{namespace}}.concerns/http-kit [_ {:keys [handler opts]}]
  (tel/event! ::starting {:level :info :data {:opts opts}})
  (let [server (http/run-server handler (assoc opts :legacy-return-value? false))]
    (tel/event! ::started {:level :info :data {:port (http/server-port server)}})
    server))

(defmethod ig/halt-key! :{{namespace}}.concerns/http-kit [_ server]
  (tel/event! ::stopping {:level :info})
  (http/server-stop! server))
