(ns clj-wiki.core
  (:use [compojure.core :only [defroutes GET POST ANY]]
        [compojure.handler :only [site]]
        [clj-wiki.handlers :only [view-page list-page edit-page commit-page not-found-page]]
        [ring.adapter.jetty :only [run-jetty]]
        [somnium.congomongo :only [make-connection with-mongo authenticate]]))

(defroutes routes
  (GET "/" req
       (view-page "TopPage"))
  (GET "/all" req
       (list-page))
  (POST "/submit" {params :params}
        (commit-page (:wikiname params) (:content params)))
  (GET "/:wikiname" [wikiname]
       (view-page wikiname))
  (GET "/:wikiname/edit" [wikiname]
       (edit-page wikiname))
  (ANY "*" _
       (not-found-page)))

(def clj-wiki-app (site routes))

(defn db-info [url]
  (if-let [[_ user pass host port db]
           (re-matches #"mongodb://(.+?):(.+?)@(.+?):(\d+?)/(.+)" url)]
    {:user user :pass pass :host host :port (Integer/parseInt port) :db db}))

(defn with-db [db-info proc]
  (let [{:keys [user pass host port db]} db-info
        conn (make-connection db :host host :port port)]
    (with-mongo conn
      (when (and user pass)
        (authenticate conn user pass))
      (proc))))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))
        db-url (System/getenv "MONGOHQ_URL")
        db-info (or (and db-url (db-info db-url))
                    {:db "mydb", :host "127.0.0.1", :port 27017})]
    (with-db db-info
      #(run-jetty clj-wiki-app {:port port}))))
