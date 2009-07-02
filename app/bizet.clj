(ns bizet
  (:use (bizet entries queries utilities web-utilities pages)
        (clojure.contrib [shell-out :only (sh)])
        compojure))

;; Docs

(def entries (ref {}))
(dosync 
  (commute entries pull-entries-from-fs))

;; Servlet def, Route defs

(defroutes bizetapp
  "The Bizet Catalog."

  (GET "/" (home @entries))

  (GET "/entries/" (get-entry @entries))

  (GET "/entry/:id" (get-entry @entries (params :id)))

  ;(GET "/section/:name"
  ;  (let [section   (params :name)
  ;        sections  (section-for-each @entries section)
  ;        title     (format "%s Sections" (first-upcase section))]
  ;      (templ title
  ;          [:h1 title]
  ;          (map (fn [[entry sec]] 
  ;                [[:h2 (ctx-link-to 
  ;                        (format "/entry/%s" (:id entry) (:title entry)))] 
  ;                 sec]))
  ;              sections)))

  (GET "/search/"
    (templ "Search"
        [:p "Search not yet implemented."]))

  (GET "/search/in/"
    (search-in @entries (first-if-vec (params :tag)) (cat-if-vec (params :terms))))

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
