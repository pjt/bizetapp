(ns bizet
  (:use [bizet entries utilities web-utilities pages]
        [clojure.contrib [shell-out :only (sh)]]
        [compojure.control :only (decorate-with)]
        [compojure.http.helpers :only (page-not-found)]
        compojure.http.routes))

;; Docs

(def entries (ref {}))
(def stylesheets (ref {}))
(dosync 
  (commute entries pull-entries-from-fs)
  (commute stylesheets pull-stylesheets-from-fs))

;; Servlet def, Route defs

(defroutes bizetapp
  "The Bizet Catalog."

  (GET "/" (home @entries))

  (GET "/works/" (get-entry @entries))

  (GET "/works/:id" (get-entry @entries (params :id)))

  (GET "/search/"
    (templ "Search"
        [:p "Search not yet implemented."]))

  (GET "/search/in/"
    (search-in @entries (first-params params :tag) (cat-params params :terms)))

  (GET "/abbrevs/lookup/"
    (abbrev-lookup (first-params params :q)))

  (GET "/abbrevs/test/"
    (abbrev-test @entries))

  (GET "/stylesheets/"
    (run-stylesheets @stylesheets))

  (GET "/stylesheets/:sheet/"
    (run-stylesheets @entries @stylesheets (params :sheet)))

  (GET "/sandiego"
    (san-diego))

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
                  (dosync 
                    (commute stylesheets pull-stylesheets-from-fs)
                    (commute entries pull-entries-from-fs)))])))

  (GET "*" (trimming-serve-file "public" (:uri request)))
  (ANY "*" (page-not-found)))

(decorate-with add-trailing-slash bizetapp)
