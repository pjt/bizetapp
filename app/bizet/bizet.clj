(ns bizet
    (:use (compojure html http str-utils) saxon))

(defn set-count
    "Reduces collection to a hash whose keys are the unique items
    in the collection & whose values are the number of times the
    item appears. Hash is sorted by number, descending."
    [coll]
    (sort-by
        #(val %)                        ; grab val of hash item
        (fn [x y] (- (compare x y)))    ; descending order
        (reduce
            (fn [accum item]
                (merge-with 
                    +
                    accum
                    {item 1}))
            {} 
            coll)))


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

;; Misc

(defn upcase
    "Makes character uppercase."
    [#^Character char]
    (Character/toUpperCase char))

(defn downcase
    "Makes character lowercase."
    [#^Character char]
    (Character/toLowerCase char))

(defn first-upcase
    "Makes first character of string uppercase."
    [#^String s]
    (apply str (upcase (first s)) (rest s)))

;; Docs

(load "entries.clj")
(def htmlstyle (compile-xslt "public/bizet.xsl"))

(let [replace-fn (compile-xslt "public/text-replace.xsl")]
    (defn txt-replace
        "Replaces instances of pat (string-representation of
        regex) with repl (replacement string, with $N for 
        matched groups, governed by any flags ('i' for case-
        insensitive, 'm' for multi-line, etc.)."
        [xml pat repl & flags]
        (replace-fn xml 
            {:target-pattern pat
             :replace-pattern repl
             :flags (or (and flags (first flags)) "")})))

(dosync (commute entries pull-entries-from-fs))

;(def divids 
;    (set-count (mapcat :divids (vals @entries))))
;
;;(def divids 
;;    ((comp set-count mapcat :divids vals) @entries))
;
;(def all-tags
;    (sort (set (mapcat :tags (vals @entries)))))
                
    

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
                    [:select {:name "tag"} 
                        (select-options 
                            (sort (set (mapcat :tags (vals @entries)))))]
                    "with " (text-field :terms)
                    (submit-button "Search")])))
    (GET "/entry/:id" 
        (templ "Bizet Entry" 
            (htmlstyle (:doc (@entries (route :id))))))
    (GET "/section/:name"
        (let [section (route :name)
              div   (compile-xpath (format "//div[@xml:id='%s']" section))
              section (first-upcase section)]
            (templ (format "%s Sections" section)
                [:h1 (format "%s Sections" section)]
                (str-map
                    (fn [entry] 
                        (let [[id e] entry]
                            (if-let sec (div (:doc e))
                                (html
                                    [:h2 (link-to (format "/entry/%s" id) (:title e))]
                                    (htmlstyle sec)))))
                    @entries))))
    (GET "/search/"
        (templ "Search"
            [:p "Search not yet implemented."]))
    (GET "/search/in/"
        (let [tag   (param :tag)
              terms (param :terms)
              srch  (compile-xpath (format "//%s[matches(.,'%s','i')]" tag (h terms)))
              results (for [e (vals @entries)]
                        {:entry e :hits (srch (:doc e))})
              results (filter :hits results)]
            (templ "Search Results"
                [:h1 (format "Results for \"%s\" in &lt;%s&gt;: %d" 
                        terms 
                        tag 
                        (count (mapcat :hits results)))]
                (if results
                    (str-map
                        (fn [result]
                            (let [e (:entry result)
                                  to-entry 
                                    (link-to (format "/entry/%s" (:id e)) (:title e))]
                                (str-map
                                    #(html
                                        [:div.return-chunk 
                                            (htmlstyle % {:search-terms terms})]
                                        [:div.to-entry to-entry])
                                    (:hits result))))
                        results)
                    [:p [:em "No results found."]]))))

    (GET "/rrr" 
        (templ "Reload"
            [:pre
                (str-map
                    (fn [[k,v]] 
                        (let [m (meta v)]
                            (format "%-30s: %tc\n"
                                (:file m)
                                (:modified m)))) 
                    (dosync (commute entries pull-entries-from-fs)))]))
    (GET "/*" (or (serve-file "public" full-path) :next))
    (ANY "/*" (page-not-found)))
