(ns bizet.queries
  (:use compojure saxon
     (bizet utilities web-utilities)))

(defn search-in-form 
  [entries]
  (form-to [:get (url "/search/in/")]
    [:p "Search in "
      (drop-down :tag
        (sort (set (mapcat :tags (vals entries)))))
        "with " (text-field :terms)
                (submit-button "Search")]))

(defn by-composition-date
  [entries]
  (sort-by #(:comp-date (meta %)) (comp - compare) (vals entries)))

(defn by-modified-date
  [entries]
  (sort-by #(:modified (meta %)) (comp - compare) (vals entries)))

(defn section-for-each
  "Returns seq of [entry section-html] pairs."
  [entries section]
  (let [div (compile-xpath (format "id('%s')" section))]
    (map (fn [[id entry]] 
            (if-let [sec (div (:doc entry))]
              [entry (htmlify sec)]))
        (vals entries))))

(defn search-in-tag
  "Returns nodes that contain search query, in map of :count & :restuls-map,
  where :results-map is a map from entry to seq of htmlized nodes, with 
  highlighting."
  [entries tag q]
  (let [srch  (compile-xpath (format "//%s[matches(.,\"%s\",'i')]" tag (h q)))
        results (for [e (vals entries)]
                  {:entry e :hits (srch (:doc e))})
        results (filter :hits results)]
    {:n   (count (mapcat :hits results))
     :results-map 
        (when results
          (into {}
            (map (fn [result]
                    [(:entry result) (map #(htmlify % {:search-terms q}) 
                                            (:hits result))])
              results)))}))
