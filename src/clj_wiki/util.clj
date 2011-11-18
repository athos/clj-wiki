(ns clj-wiki.util
  (:import [java.net URLEncoder URLDecoder]))

(defn url-encode [^String url & [more]]
  (reduce #(.replace ^String %1 (str %2) (format "%%%2X" (int %2)))
          (URLEncoder/encode url)
          more))

(defn url-decode [^String url]
  (URLDecoder/decode url))
