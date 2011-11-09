(ns clj-wiki.core
  (:use compojure.core
        hiccup.core)
  (:require [appengine-magic.core :as ae]))

(defn page->html [title content & {:keys [show-edit? show-all?]}]
  (html [:html
         [:head [:title title]
          [:body
           [:h1 title]
           [:div {:align "right"}
            [:a {:href "/"} "[トップ]"]
            [:a {:href (str "/" title "/edit")} "[編集]"]
            [:a {:href (str "/all")} "[一覧]"]]
           [:hr]
           content]]]))

(defn view-page [pagename]
  (page->html pagename ""))

(defn edit-page [pagename]
  (page->html
   pagename
   [:form {:method "POST" :action (str "/" pagename "/submit")}
    [:textarea {:name "content" :rows 25 :cols 60} "the content goes here"]
    [:input {:type "submit" :name "submit" :value "Submit"}]
    [:input {:type "reset" :name "reset" :value "Reset"}]]))

(defroutes clj-wiki-app-handler
  (GET "/" req
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "Hello, world!"})
  (GET "/all" req
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "page list goes here"})
  (GET "/:wikiname" [wikiname]
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (view-page wikiname)})
  (GET "/:wikiname/edit" [wikiname]
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (edit-page wikiname)})
  (POST "/:wikiname/submit" [wikiname]
        {:status 200
         :headers {"Content-Type" "text/plain"}
         :body (format "Submit Page for %s" wikiname)})
  (ANY "*" _
       {:status 404
        :headers {"Content-Type" "text/plain"}
        :body "not found"}))

(ae/def-appengine-app clj-wiki-app #'clj-wiki-app-handler)
