(ns clj-wiki.core
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [compojure.handler :only [site]]
        [clj-wiki.handlers :only [view-page list-page edit-page commit-page not-found-page]]
        [clj-wiki.util :only [url-decode]]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.multipart-params :only [wrap-multipart-params]]
        [somnium.congomongo :only [make-connection set-connection! authenticate connection?]]
        [somnium.congomongo.config :only [*mongo-config*]]))

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

(def *db-info* (atom nil))

(defn wrap-reload [handler reloadables]
  (fn [req]
    (doseq [reloadable reloadables]
      (require reloadable :reload))
    (handler req)))

(defn wrap-mongo-setup [handler default]
  (fn [req]
    (when-not (connection? *mongo-config*)
      (let [{:keys [user pass host port db]} (or @*db-info* default)
            conn (make-connection db :host host :port port)]
        (set-connection! conn)
        (when (and user pass)
          (authenticate conn user pass))))
    (handler req)))

(def clj-wiki-app
  (-> (site routes)
      (wrap-mongo-setup {:db "mydb" :host "127.0.0.1" :port 27017})
      (wrap-multipart-params)
      (wrap-reload '[clj-wiki.handlers])
      ))

(defn db-info [url]
  (and url
       (if-let [[_ user pass host port db]
                (re-matches #"mongodb://(.+?):(.+?)@(.+?):(\d+?)/(.+)" url)]
         {:user user :pass pass :host host :port (Integer/parseInt port) :db db})))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))
        db-url (System/getenv "MONGOHQ_URL")]
    (reset! *db-info* (db-info db-url))
    (run-jetty clj-wiki-app {:port port})))
