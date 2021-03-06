(ns bizet.utilities
  (:use saxon clojure.contrib.str-utils))

(def dot-xml
    (proxy [java.io.FilenameFilter] []
        (accept [dir name] (.endsWith name ".xml"))))

(def dot-xsl
    (proxy [java.io.FilenameFilter] []
        (accept [dir name] (.endsWith name ".xsl"))))

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

(defn cat-params
  "Concatenates items to string, if arg is vector; 
  otherwise returns argument. Takes optional separator 
  argument."
  ([prms kkey] (cat-params " " prms kkey))
  ([sep prms kkey]
    (let [vval  (prms kkey)
          f (if (vector? vval)
                (partial str-join sep)
                identity)]
      (f vval))))

(defn first-params
  "Returns first item in vector, if arg is vector; 
  otherwise returns argument."
  [prms kkey]
    (let [vval (prms kkey)
          f (if (vector? vval)
                  first
                  identity)]
      (f vval)))

(defn as-coll
  "If x is collection, return it; if singleton, make one-item vector. Special case: if nil,
  returns nil."
  [x]
  (when x
    (if (coll? x) x [x])))

(defn uuid
  "Returns randomly generated UUID (as string)."
  []
  (.toString (java.util.UUID/randomUUID)))

(defn as-abs-path
  {:tag String}
  [p]
  (if (instance? java.io.File p) (.getAbsolutePath #^java.io.File p) p))

(defn throw-fmt
  [fmt-str & args]
  (throw (Exception. (apply format fmt-str args))))

;; -- path utilities --

(defn trim-right
    "Removes s2 from right of s1."
    [#^String s1 #^String s2]
    (if (.endsWith s1 s2)
        (.substring s1 0 (- (count s1) (count s2)))
        s1))

(defn trim-left
    "Removes s2 from left of s1."
    [#^String s1 #^String s2]
    (if (.startsWith s1 s2)
        (.substring s1 (count s2))
        s1))

(defn basename
  "Like Unix basename: returns last portion of path."
  [path & suffix]
  (let [result (get (re-find #"(/|[^/]+)/?$" path) 1 "")]
    (if suffix (trim-right result (first suffix)) result)))

(defn dirname
  "Like Unix dirname: returns all but last portion of path."
  [path]
  (if (= path "/")
    path
    (let [path    (trim-right path "/")
          dirname (trim-right path (basename path))]
      (if (> (count dirname) 1)
        (trim-right dirname "/")
        dirname))))

(defn dirname-seq
  "Returns a lazy-seq of path and successive calls of dirname on path,
  e.g. /home/bin/text => (/home/bin/text /home/bin /home /)."
  [path]
  (when (not= path "")
    (lazy-seq (cons path (when (not= "/" path)
                            (dirname-seq (dirname path)))))))
(defn reduce-path-by
    "Trims path from the left with another path. E.g. 
    /home/dir/file.txt reduced by /home is dir/file.txt."
    [path #^String screen]
    (if (= path screen) ""
      (let [screen 
              (if (.endsWith screen "/")
                  screen
                  (str screen "/"))]
          (trim-left path screen))))


; XML

(defn compile-tei-q "Compile XQuery with TEI ns as default." 
  [q] (compile-xquery (with-default-ns "http://www.tei-c.org/ns/1.0" q)))

(defn tei-q "Run XQuery with TEI ns as default."
  [q nd] (query (with-default-ns "http://www.tei-c.org/ns/1.0" q) nd))


