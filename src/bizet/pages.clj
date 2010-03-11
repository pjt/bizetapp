(ns bizet.pages
  (:use 
    [bizet queries utilities web-utilities abbrevs]
    [clojure.contrib.json.write :only (json-str)]
    [compojure.html gen page-helpers]))

(defn home [entries]
  (templ "Bizet Entries" 
    [:h2 "Search by Tag"]
    (search-in-form entries)))

(defn- entry-link
  ([entry] (entry-link entry :title))
  ([entry display-fn]
    (ctx-link-to
      (format "/entry/%s" (:id entry)) (display-fn entry))))

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

(defn edit-entry
  [entries id]
  (templ "Bizet Entry"
    {:js "/js/tei-and-html.js"}
    (htmlify (:doc (entries id)))))

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
             
