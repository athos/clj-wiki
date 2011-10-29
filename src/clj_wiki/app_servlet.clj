(ns clj-wiki.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use clj-wiki.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method clj-wiki-app) this request response))
