(ns clj-wiki.core
  (:require [appengine-magic.core :as ae]))


(defn clj-wiki-app-handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, world!"})


(ae/def-appengine-app clj-wiki-app #'clj-wiki-app-handler)