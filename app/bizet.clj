(ns bizet
    (:use (bizet entries utilities)
          (clojure.contrib str-utils)
          (clojure.contrib [shell-out :only (sh)])
          compojure
          saxon))

;; HTML funcs

(defn css-path
    "Concatenate file with path prefix for CSS files. File
    should be given as string or keyword, without .css suffix."
    [file]
    (let    [prefix "/css/"
             file (str* file)]
        (str prefix file ".css")))

(defmacro nav
    "Navigation section."
    [heading & items]
    `(html
        [:div {:class "nav"}
            [:strong ~heading]
            [:ul
                ~@(for [[path label] items] 
                    [:li (link-to path label)])]]))

(defn templ
    "HTML template."
    [title & body]
    [{"Content-Type" "text/html;charset=UTF-8"}
     (html
        (doctype :xhtml-strict)
        [:html
          [:head
            [:link {:rel "stylesheet" :type "text/css" :href (css-path :main)}]
            [:title title]]
          [:body
            body]])])

;; Docs

(def htmlstyle (compile-xslt (java.io.File. "public/bizet.xsl")))

(dosync (commute entries pull-entries-from-fs))

;; Servlet def, Route defs

(defservlet bizetapp
  "The Bizet Catalog."
  (GET "/"
    (templ "Bizet Entries" 
        [:h2 "Entries"]
        (unordered-list 
            (map 
                #(link-to (str "/entry/" (:id %)) 
                    (format "%s" (:title %)) 
                    (and (:comp-date %) 
                         (format " (%s)" (:comp-date %))))
                (vals @entries)))
        [:h2 "Divisions"]
        (unordered-list 
            (map 
                #(link-to (str "/section/" (key %)) 
                    (format "%s (%s)" (key %) (val %)))
                (set-count (mapcat :divids (vals @entries)))))
        [:h2 "Search by Tag"]
        (form-to [GET "/search/in/"]
            [:p "Search in "
                (drop-down :tag
                    (sort (set (mapcat :tags (vals @entries)))))
                "with " (text-field :terms)
                (submit-button "Search")])))
  (GET "/entry/:id" 
    (templ "Bizet Entry" 
        (htmlstyle (:doc (@entries (route :id))))))
  (GET "/section/:name"
    (let [section (route :name)
          div   (compile-xpath (format "id('%s')" section))
          section (first-upcase section)]
        (templ (format "%s Sections" section)
            [:h1 (format "%s Sections" section)]
            (map; str
                (fn [entry] 
                    (let [[id e] entry]
                        (if-let [sec (div (:doc e))]
                            (html
                                [:h2 (link-to (format "/entry/%s" id) (:title e))]
                                (htmlstyle sec)))))
                @entries))))
  (GET "/search/"
    (templ "Search"
        [:p "Search not yet implemented."]))
  (GET "/search/in/"
    (let [tag   (first-if-vec (params :tag))
          terms (cat-if-vec (params :terms))
          srch  (compile-xpath (format "//%s[matches(.,\"%s\",'i')]" tag (h terms)))
          results (for [e (vals @entries)]
                    {:entry e :hits (srch (:doc e))})
          results (filter :hits results)]
        (templ "Search Results"
            [:h1 (format "Results for \"%s\" in &lt;%s&gt;: %d" 
                    terms 
                    tag 
                    (count (mapcat :hits results)))]
            (if results
                (mapcat; str
                    (fn [result]
                        (let [e (:entry result)
                              to-entry 
                                (link-to (format "/entry/%s" (:id e)) (:title e))]
                            (map; str
                                #(html
                                    [:div.return-chunk 
                                        (htmlstyle % {:search-terms terms})]
                                    [:div.to-entry to-entry])
                                (:hits result))))
                    results)
                [:p [:em "No results found."]]))))

  (GET "/rrr" 
    (do
      (sh "svn" "up" *data-dir*)
      (templ "Reload"
          [:pre
              (map; str
                  (fn [[k,v]] 
                      (let [m (meta v)]
                          (format "%-30s: %tc\n"
                              (:file m)
                              (:modified m)))) 
                  (dosync (commute entries pull-entries-from-fs)))])))
  (GET "/*" (or (serve-file "public" path) :next))
  (ANY "/*" (page-not-found)))
