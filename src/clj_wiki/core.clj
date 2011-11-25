(ns clj-wiki.core
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [compojure.handler :only [site]]
        [clj-wiki.handlers :only [view-page list-page edit-page commit-page not-found-page]]
        [clj-wiki.util :only [url-decode]]
        [clj-wiki.ring-middleware :only [wrap-reload wrap-mongo-connect set-db-info!]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.multipart-params :only [wrap-multipart-params]]))

(defroutes routes
  (GET "/" req
       (view-page "TopPage"))
  (GET "/all" req
       (list-page))
  (GET "/:wikiname" [wikiname]
       (view-page wikiname))
  (GET "/:wikiname/edit" [wikiname]
       (edit-page wikiname))
  (POST "/:wikiname/submit" [wikiname content]
        (commit-page wikiname content))
  (ANY "*" _
       (not-found-page)))

(def clj-wiki-app
  (-> (site routes)
      (wrap-mongo-connect {:db "mydb" :host "127.0.0.1" :port 27017})
      (wrap-multipart-params)
      #_(wrap-reload '[clj-wiki.handlers])
      ))

(defn db-info [url]
  (and url
       (if-let [[_ user pass host port db]
                (re-matches #"mongodb://(.+?):(.+?)@(.+?):(\d+?)/(.+)" url)]
         {:user user :pass pass :host host :port (Integer/parseInt port) :db db})))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))
        db-url (System/getenv "MONGOHQ_URL")]
    (set-db-info! (db-info db-url))
    (run-jetty clj-wiki-app {:port port})))
