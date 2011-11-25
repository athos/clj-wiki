(ns clj-wiki.util
  (:use [somnium.congomongo :only [make-connection set-connection! authenticate connection?]]
        [somnium.congomongo.config :only [*mongo-config*]])
  (:import [java.net URLEncoder URLDecoder]))

(defn url-encode [^String url & [more]]
  (reduce #(.replace ^String %1 (str %2) (format "%%%2X" (int %2)))
          (URLEncoder/encode url "UTF-8")
          more))

(defn url-decode [^String url]
  (URLDecoder/decode url "UTF-8"))

(defn with-db [db proc]
  (let [{:keys [user pass host port db]} db
        conn (make-connection db :host host :port port)]
    (when-not (connection? *mongo-config*)
      (set-connection! conn)
      (when (and user pass)
        (authenticate conn user pass)))
    (proc)))
