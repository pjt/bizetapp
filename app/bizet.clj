(ns bizet
    (:use (bizet entries utilities web-utilities)
          (clojure.contrib str-utils)
          (clojure.contrib [shell-out :only (sh)])
          compojure
          saxon))

;; Docs

(def entries (ref {}))
(dosync 
  (commute entries pull-entries-from-fs))

;; Servlet def, Route defs

(defroutes bizetapp
  "The Bizet Catalog."

  (GET "/"
    (templ "Bizet Entries" 
        #_(unordered-list 
            (map 
                #(link-to (str "/entry/" (:id %)) 
                    (format "%s" (:title %)) 
                    (and (:comp-date %) 
                         (format " (%s)" (:comp-date %))))
                (vals @entries)))
        [:h2 "Search by Tag"]
        (form-to [GET "/search/in/"]
            [:p "Search in "
                (drop-down :tag
                    (sort (set (mapcat :tags (vals @entries)))))
                "with " (text-field :terms)
                (submit-button "Search")])
        [:h2 "Divisions"]
        (unordered-list 
            (map 
                #(link-to (str "/section/" (key %)) 
                    (format "%s (%s)" (key %) (val %)))
                (set-count (mapcat :divids (vals @entries)))))))
  (GET "/entries/"
    (templ "Entries"
       [:h2 "Catalog Entries"]
        (unordered-list 
            (map 
                #(link-to (str "/entry/" (:id %)) 
                    (format "%s" (:title %)) 
                    (and (:comp-date %) 
                         (format " (%s)" (:comp-date %))))
                (sort-by #(:modified (meta %)) (comp - compare)
                  (vals @entries))))))
  (GET "/entry/:id" 
    (templ "Bizet Entry" 
        (htmlify (:doc (@entries ((:route-params request) :id))))))
  (GET "/section/:name"
    (let [section ((:route-params request) :name)
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
                                (htmlify sec)))))
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
                (mapcat
                    (fn [result]
                        (let [e (:entry result)
                              to-entry 
                                (link-to (format "/entry/%s" (:id e)) (:title e))]
                            (map
                                #(html
                                    [:div.return-chunk 
                                        (htmlify % {:search-terms terms})]
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
  (GET "/*" (trimming-serve-file "public" (:uri request)))
  (ANY "/*" (page-not-found)))
