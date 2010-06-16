(ns bizet.queries
  (:use 
     compojure.html.form-helpers 
     [compojure.html.gen :only (h)]
     [saxon :as sax :only ()]
     [clojure.contrib.seq-utils :as su :only ()]
     [bizet.utilities :only (as-coll compile-tei-q)]
     [bizet abbrevs web-utilities]))

(defn search-in-form 
  "Display HTML form for searching in tag."
  [entries]
  (form-to [:get (url "/search/in/")]
    [:p "Search in "
      (drop-down :tag
        (sort (set (mapcat :tags (vals entries)))))
        " with " (text-field :terms)
                (submit-button "Search")]))

(defn by-composition-date
  [entries]
  (sort-by #(:comp-date %) (vals entries)))

(defn by-modified-date
  [entries]
  (sort-by #(:modified (meta %)) (comp - compare) (vals entries)))

(defn section-for-each
  "Returns seq of [entry section-html] pairs."
  [entries section]
  (let [div (sax/compile-xpath (format "id('%s')" section))]
    (map (fn [[id entry]] 
            (if-let [sec (div (:doc entry))]
              [entry (htmlify sec)]))
        (vals entries))))

(defn search-in-tag
  "Returns nodes that contain search query, in map of :count & :restuls-map,
  where :results-map is a map from entry to seq of htmlized nodes, with 
  highlighting."
  [entries tag q]
  (let [srch    (compile-tei-q (format "//%s[matches(.,\"%s\",'i')]" tag (h q)))
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

(defn test-abbrevs
  [entries]
  (let [get-abbrs (compile-tei-q "distinct-values(//abbr/string())")
        abbrevs (apply merge-with (partial apply conj)
                    (map (fn [entry] 
                           (zipmap (as-coll (get-abbrs (:doc entry))) 
                                      (repeat [entry])))
                      (vals entries)))
        abbrevs (remove (comp nil? key) abbrevs)]
    (su/group-by #(count (lookup (key %))) abbrevs)))
    ; returns map of count => vector of key-value pairs, where key is
    ; the abbrev, value is a vector of entries in which abbrev appears,
    ; e.g. 0 => ["us-nope" [entry1 entry8 ...]]

(defn apply-stylesheet
  "Apply stylesheet to a document or all documents. Returns [entry result-doc], or seq of these."
  ([entries entry-q sheets sheet-q]
    (let [entry (entries entry-q)]
      [entry ((:fn (sheets sheet-q)) (:doc entry))]))
  ([entries sheets sheet-q]
    (filter #(seq (str (second %)))
      (map #(apply-stylesheet entries % sheets sheet-q) (keys entries)))))

(defn apply-query
  "Apply XQuery to a document or all documents. If all documents, return distinct seq of return
  values."
  ([entries entry q]
    ((compile-tei-q q) (:doc (entries entry))))
  ([entries q]
    (distinct (mapcat #(apply-query entries % q) (keys entries)))))
