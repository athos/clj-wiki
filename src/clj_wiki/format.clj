(ns clj-wiki.format
  (:use [hiccup.core :only [escape-html]]))

(defn format-content [content]
  (escape-html content))
