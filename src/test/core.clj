(ns test.core
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as ring-jetty]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))
(def users (atom {}))
(defn string-handler [_]
  {:status 200
   :body "fuck oh no"})

(defn create-user [{user :body-params}]
  (let [id (str (java.util.UUID/randomUUID))
   users (->> (assoc user :id id)
         (swap! users assoc id))]
   {
    :status 200
    :body users
    } 
  ))

(defn get-users [_]
  {:status 200
   :body @users})

(defn get-user [{{:keys [id]} :path-params}]
  {:status 200
   :body (get @users id)})

(def app
  (ring/ring-handler
   (ring/router
    ["/"
     ["" string-handler]
     ["users" get-users]
     ["user/:id" get-user]
     ["user" {:post create-user }]]

    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})))

(def reloadable-app (wrap-reload #'app))

(defn start []
  (ring-jetty/run-jetty reloadable-app {:port 3000 :join? false}))

(defn -main [& args] (start))

