(ns clj-wiki.handlers
  (:use [clj-wiki.util :only [url-encode]]
        [clj-wiki.format :only [format-content]]
        [hiccup.page-helpers :only [html4]]
        [hiccup.core :only [html escape-html]]
        [ring.util.response :only [redirect-after-post]]
        [somnium.congomongo :only [insert! update! fetch-one fetch]]))

(defn- page->html [title content & {:keys [show-edit? show-all?]
                                    :or {show-edit? true, show-all? true}}]
  (html4
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
    [:title (escape-html title)]
    ]
   [:body
    [:h1 (escape-html title)]
    [:div {:align "right"}
     `([:a {:href "/"} "[Top]"]
        ~@(if show-edit?
            [[:a {:href (str "/" (url-encode title "/") "/edit")} "[Edit]"]]
            [])
        ~@(if show-all?
            [[:a {:href (str "/all")} "[All]"]]
            []))]
    [:hr]
    content]))

(defn- render-view-page [pagename content]
  (page->html pagename content))

(defn- render-edit-page [pagename content]
  (page->html
   pagename
   [:form {:method "POST"
           :action (str "/" (url-encode pagename "/") "/submit")
           :enctype "multipart/form-data"}
    [:textarea {:name "content" :rows 25 :cols 60} content]
    [:input {:type "submit" :name "submit" :value "Submit"}]
    [:input {:type "reset" :name "reset" :value "Reset"}]]
   :show-edit? false))

(defn- render-list-page []
  (page->html
   "List"
   [:ul
    (for [page (fetch :pages :only [:name] :sort {:name 1})]
      [:li [:a {:href (url-encode (:name page) "/")}
            (escape-html (:name page))]])]
   :show-edit? false
   :show-all? false))

(defn- standard-page [content]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body content})

(defn not-found-page []
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "not found"})

(defn view-page [wikiname]
  (if-let [page (fetch-one :pages :where {:name wikiname})]
    (standard-page (render-view-page wikiname (:content page)))
    (not-found-page)))

(defn list-page []
  (standard-page (render-list-page)))

(defn edit-page [wikiname]
  (let [page (fetch-one :pages :where {:name wikiname})]
    (standard-page (render-edit-page wikiname (:content page)))))

(defn commit-page [wikiname content]
  (let [formatted (format-content content)]
    (if-let [page (fetch-one :pages :where {:name wikiname})]
      (update! :pages page (merge page {:content formatted}))
      (insert! :pages {:name wikiname :content formatted})))
  (redirect-after-post (str "/" (url-encode wikiname "/"))))
