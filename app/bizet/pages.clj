(ns bizet.pages
  (:use (bizet queries utilities web-utilities)
        compojure))

(defn home [entries]
  (templ "Bizet Entries" 
    [:h2 "Search by Tag"]
    (search-in-form entries)
    [:h2 "Divisions"]
    (unordered-list 
      (map 
        #(ctx-link-to (str "/section/" (key %)) 
          (format "%s (%s)" (key %) (val %)))
        (set-count (mapcat :sections (vals entries)))))))

(defn get-entry
  ([entries]
   (templ "Entries"
      [:h2 "Catalog Entries"]
       (ordered-list 
         (map #(ctx-link-to (str "/entry/" (:id %)) 
                 (format "%s" (:title %)) 
                 (and (:comp-date %) 
                      (format " (%s)" (:comp-date %))))
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
            (let [to-entry (ctx-link-to 
                             (format "/entry/%s" (:id entry)) (:title entry))]
              (map #(html
                      [:div.return-chunk %]
                      [:div.to-entry to-entry])
                results)))
          results-map)
        [:p [:em "No results found."]]))))

