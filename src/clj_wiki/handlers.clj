(ns clj-wiki.handlers
  (:use [hiccup.core :only [html]]
        [ring.util.response :only [redirect-after-post]]
        [somnium.congomongo :only [insert! fetch-one]]))

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

(defn- render-view-page [pagename content]
  (page->html pagename content))

(defn- render-edit-page [pagename content]
  (page->html
   pagename
   [:form {:method "POST" :action "/submit"}
    [:textarea {:name "content" :rows 25 :cols 60} content]
    [:input {:type "hidden" :name "wikiname" :value pagename}]
    [:input {:type "submit" :name "submit" :value "Submit"}]
    [:input {:type "reset" :name "reset" :value "Reset"}]]))

(defn- standard-page [content]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body content})

(defn not-found-page []
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "not found"})

(defn view-page [wikiname]
  (if-let [page {:content "hogehoge"} #_(fetch-one :pages :where {:name wikiname})]
    (standard-page (render-view-page wikiname (:content page)))
    (not-found-page)))

(defn list-page []
  (standard-page "page list goes here"))

(defn edit-page [wikiname]
  (let [page {:content "hogehoge"} #_(fetch-one :pages :where {:name wikiname})]
    (standard-page (render-edit-page wikiname (:content page)))))

(defn commit-page [wikiname content]
  (redirect-after-post (str "/" wikiname)))