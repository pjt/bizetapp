(ns bizet.pages
  (:use 
    [bizet queries utilities web-utilities abbrevs]
    [clojure.contrib.json.write :only (json-str)]
    [compojure.html gen page-helpers]))

(defn home [entries]
  (templ "Bizet Entries" 
    [:h2 "Search by Tag"]
    (search-in-form entries)
    [:h2 "Run Stylesheets"]
    (ctx-link-to "/stylesheets/" "Run stylesheets on all works")))

(defn- entry-link
  ([entry] (entry-link entry :title))
  ([entry display-fn]
    (ctx-link-to
      (format "/works/%s" (:id entry)) (display-fn entry))))

(defn get-entry
  ([entries]
   (templ "Entries"
      [:h2 "Catalog Entries"]
       (ordered-list 
         (map (fn [entry] 
                (entry-link entry
                 #(str (format "%s" (:title %)) 
                     (and (:comp-date %) 
                          (format " (%s)" (:comp-date %))))))
             (by-composition-date entries)))))
  ([entries id]
   (templ "Bizet Entry" 
      (htmlify (:doc (entries id))))))

(defn search-in
  [entries tag terms]
  (let [{:keys (n results-map)}
            (search-in-tag entries tag terms)]
    (templ "Search Results"
      [:h1 (format "Results for \"%s\" in &lt;%s&gt;: %d" terms tag n)]
      (if results-map
        (mapcat
          (fn [[entry results]]
            (let [to-entry (entry-link entry)]
              (map #(html
                      [:div.return-chunk %]
                      [:div.to-entry to-entry])
                results)))
          results-map)
        [:p [:em "No results found."]]))))

(defn abbrev-lookup
  [abbr]
  (json-str (lookup abbr)))

(defn abbrev-test
  [entries]
  (let [title "Abbreviation Test Results"
        results (test-abbrevs entries)]
    (templ title {:js "/js/abbrevtest.js"}
      [:h1 title]
      (for [fnd (keys results)]
        [:div.abbrev-result
         [:h2 (format "Abbrevs for which %d match%s found" 
                        fnd (if (= 1 fnd) " was" "es were"))]
         (ordered-list
           (for [abbr (results fnd)]
             (html (key abbr)
                (unordered-list (map entry-link (val abbr))))))]))))
             
(defn run-stylesheets
  "Run stylesheets on all documents."
  ([sheets]
    (templ "Available stylesheets"
      [:h2 "Stylesheets"]
      [:p "Choose stylesheet to run over all documents."]
      (ordered-list
        (map #(ctx-link-to (format "/stylesheets/%s/" (:filename %)) (:filename %)) (vals sheets))))) 
  ([docs sheets sheet]
    (let [niller    (constantly nil)
          sheet-fn  (get (sheets sheet) :fn niller)
          results   (map (fn [doc] [doc (sheet-fn (:doc doc))]) (vals docs))
          results   (filter #(not= "" (str (second %))) results)] 
      (templ "Stylesheet Results"
        (cond 
          (= niller sheet-fn)
            (html [:h2 "Error"] [:p (format "Stylesheet `%s` was not found 
                                             in the list of sheets." sheet)])
          (not (seq results))
            (html [:h2 "No results"] [:p (format "The application of `%s` 
                                                  produced no non-emtpy results." sheet)])
          :else
            (ordered-list
              (for [[doc result] results]
                [:div [:h3 "Results for " (ctx-link-to (format "/works/%s" (:id doc)) (:title doc))]
                 [:pre (h result)]])))))))


(defn san-diego []
  (templ "MLA Links"
    [:h2 "MLA Links"]
    [:p "Two test documents:"
     (unordered-list 
       [(ctx-link-to "/works/lachansondufou" "Short: Chanson du fou")
        (ctx-link-to "/works/lespecheursdeperles" "Long: Pêcheurs de perles")])]))

