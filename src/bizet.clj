(ns bizet
  (:use [bizet entries edit utilities web-utilities pages]
        [clojure.contrib [shell-out :only (sh)]]
        [compojure.control :only (decorate decorate-with)]
        [compojure.http.helpers :only (page-not-found)]
        compojure.http.routes))

;; Docs

;(def entries (ref {}))
;(def stylesheets (ref {}))
(dosync 
  (commute entries pull-entries-from-fs)
  (commute stylesheets pull-stylesheets-from-fs))

;; Servlet def, Route defs

(defroutes bizetapp
  "The Bizet Catalog."

  (GET "/" (home @entries))

  (GET "/works/" (get-entry (only-works @entries)))

  (GET "/works/:id" (get-entry @entries (params :id)))

  (GET "/transcripts/" (get-entry (only-transcripts @entries)))

  (GET "/transcripts/:id" (get-entry @entries (params :id)))

  (GET "/search/"
    (templ "Search"
        [:p "Search not yet implemented."]))

  (GET "/search/in/"
    (search-in @entries (first-params params :tag) (cat-params params :terms)))

  (GET "/abbrevs/lookup/"
    (abbrev-lookup (first-params params :q)))

  (GET "/abbrevs/test/"
    (abbrev-test @entries))

  (with-context "/edit" edit-routes)

  (GET "/stylesheets/"
    (run-stylesheets @stylesheets))

  (GET "/stylesheets/:sheet/"
    (run-stylesheets @entries @stylesheets (params :sheet)))

  (GET "/query/"
    (if-not (seq (.trim (params :q)))
      (run-query)
      (if (seq (params :entry))
        (run-query @entries (params :entry) (params :q))
        (run-query @entries (params :q)))))

  (GET "/sandiego"
    (san-diego))

  (GET "/timeline"
    (timeline @entries))

  (GET "/rrr" 
    (do
      (sh "svn" "up" "--username" "public" "--password" "bizet" *works-dir* *transcripts-dir *xsl-dir*)
      (templ "Reload"
          [:pre (map
                  (fn [[k,v]] 
                      (let [m (meta v)]
                          (format "%s\n\t%tc\n"
                              (:file m)
                              (:modified m)))) 
                  (dosync 
                    (commute stylesheets pull-stylesheets-from-fs)
                    (commute entries pull-entries-from-fs)))])))

  (GET "/static/*" 
       (trimming-serve-file (str *base-dir* "/xmlserver-static") (params :*)))
  (GET "*" (trimming-serve-file "public" (:uri request)))
  (ANY "*" (page-not-found)))

(decorate bizetapp 
          (restrict-to-ip #"(128\.252\..*)|(172\.(1[6-9]|2[0-9]|3[0-1])\..*)")
          add-trailing-slash)
