(ns clj-wiki.handlers
  (:use [hiccup.core :only [html]]
        [ring.util.response :only [redirect-after-post]]))

(defn- page->html [title content & {:keys [show-edit? show-all?]}]
  (html [:html
         [:head [:title title]
          [:body
           [:h1 title]
           [:div {:align "right"}
            [:a {:href "/"} "[Top]"]
            [:a {:href (str "/" title "/edit")} "[Edit]"]
            [:a {:href (str "/all")} "[All]"]]
           [:hr]
           content]]]))

(defn- render-view-page [pagename]
  (page->html pagename ""))

(defn- render-edit-page [pagename]
  (page->html
   pagename
   [:form {:method "POST" :action "/submit"}
    [:textarea {:name "content" :rows 25 :cols 60} "the content goes here"]
    [:input {:type "hidden" :name "wikiname" :value pagename}]
    [:input {:type "submit" :name "submit" :value "Submit"}]
    [:input {:type "reset" :name "reset" :value "Reset"}]]))

(defn- standard-page [content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body content})

(defn view-page [wikiname]
  (standard-page (render-view-page wikiname)))

(defn list-page []
  (standard-page "page list goes here"))

(defn edit-page [wikiname]
  (standard-page (render-edit-page wikiname)))

(defn commit-page [wikiname content]
  (redirect-after-post (str "/" wikiname)))

(defn not-found-page []
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "not found"})
