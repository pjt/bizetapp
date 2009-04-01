(ns bizet.utilities
  (:use clojure.contrib.str-utils))

(def dot-xml
    (proxy [java.io.FilenameFilter] []
        (accept [dir name] (.endsWith name ".xml"))))

(defmacro transform-keyval
  "Transforms key-value pairs according to passed form.
  Passed form will have the vars 'k' & 'v' available to
  it, & will be evaulated for each key-value pair. Results
  of evaluation should be a vector of new [key val]."
  [hashmap form] 
  `(into {} (map (fn ~'[[k,v]] ~form) ~hashmap)))


(defn set-count
  "Reduces collection to a hash whose keys are the unique items
  in the collection & whose values are the number of times the
  item appears. Hash is sorted by number, descending."
  [coll]
  (sort-by val                        ; sort by val of hash item
    (fn [x y] (- (compare x y)))    ; descending order
    (reduce (fn [accum item]
              (assoc accum item (inc (accum item 0))))
      {} coll)))

;; Misc

(defn upcase
  "Makes string uppercase."
  [#^String s]
  (.toUpperCase s))

(defn downcase
  "Makes string lowercase."
  [#^String s]
  (.toLowerCase s))

(defn first-upcase
  "Makes first character of string uppercase."
  [#^String s]
  (apply str (Character/toUpperCase (first s)) (rest s)))

(defn cat-if-vec
  "Concatenates vector items to string, if arg is vector; 
  otherwise returns argument. Takes optional separator 
  argument."
  ([arg] (cat-if-vec " " arg))
  ([sep arg]
    (let [f (if (vector? arg)
                (partial str-join sep)
                identity)]
      (f arg))))

(defn first-if-vec
  "Returns first item in vector, if arg is vector; 
  otherwise returns argument."
  [arg]
    (let [f (if (vector? arg)
                  first
                  identity)]
      (f arg)))

