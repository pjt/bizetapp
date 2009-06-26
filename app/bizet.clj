(ns bizet
  (:use (bizet entries queries utilities web-utilities)
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
      [:h2 "Search by Tag"]
      (search-in-form @entries)
      [:h2 "Divisions"]
      (unordered-list 
        (map 
          #(ctx-link-to (str "/section/" (key %)) 
            (format "%s (%s)" (key %) (val %)))
          (set-count (mapcat :sections (vals @entries)))))))
  (GET "/entries/"
    (templ "Entries"
       [:h2 "Catalog Entries"]
        (ordered-list 
            (map #(ctx-link-to (str "/entry/" (:id %)) 
                    (format "%s" (:title %)) 
                    (and (:comp-date %) 
                         (format " (%s)" (:comp-date %))))
                (by-composition-date @entries)))))
  (GET "/entry/:id" 
    (templ "Bizet Entry" 
        (htmlify (:doc (@entries (params :id))))))
  (GET "/section/:name"
    (let [section   (params :name)
          sections  (section-for-each @entries section)
          title     (format "%s Sections" (first-upcase section))]
        (templ title
            [:h1 title]
            (map (fn [[entry sec]] 
                  [[:h2 (ctx-link-to 
                          (format "/entry/%s" (:id entry) (:title entry)))] 
                   sec]))
                sections)))
  (GET "/search/"
    (templ "Search"
        [:p "Search not yet implemented."]))
  (GET "/search/in/"
    (let [tag   (first-if-vec (params :tag))
          terms (cat-if-vec (params :terms))
          {:keys (n results-map)}
                (search-in-tag @entries tag terms)]
      (templ "Search Results"
        [:h1 (format "Results for \"%s\" in &lt;%s&gt;: %d" terms tag n)]
        (if results-map
          (mapcat
            (fn [[entry results]]
              (let [to-entry (ctx-link-to 
                               (format "/entry/%s" (:id entry)) (:title entry))]
                (map #(html
                        [:div.return-chunk %]
                        [:div.to-entry to-entry])
                  results)))
            results-map)
          [:p [:em "No results found."]]))))

  (GET "/rrr" 
    (do
      (sh "svn" "up" *data-dir*)
      (templ "Reload"
          [:pre (map
                  (fn [[k,v]] 
                      (let [m (meta v)]
                          (format "%s\n\t%tc\n"
                              (:file m)
                              (:modified m)))) 
                  (dosync (commute entries pull-entries-from-fs)))])))
  (GET "*" (trimming-serve-file "public" (:uri request)))
  (ANY "*" (page-not-found)))
