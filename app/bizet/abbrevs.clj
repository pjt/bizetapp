(ns bizet.abbrevs
    (:use clojure.contrib.duck-streams
          (clojure.contrib.json read write)))

(def *abbrevs* 
  (read-json 
    (java.io.PushbackReader. (reader "public/abbrevs.oxford-music-online.json"))))

(def lookups
  [[(partial re-find #"([A-Z]+)-(.*)"),
    (fn [[_ country siglum]] 
      (let [libs (-> *abbrevs* (get "Library Sigla") (get country))
            lib  (libs siglum)]
        (when lib (str (libs "_self") ": " lib))))]
   [identity,
    (fn [abbr] (-> *abbrevs* (get "Bibliographical Abbreviations") (get abbr)))]
   [identity,
    (fn [abbr] (-> *abbrevs* (get "General Abbreviations") (get abbr)))]])

(defn lookup 
  "Lookup abbreviation in Oxford Music Online's list."
  [abbr]
  (filter identity
    (for [[candidate? retrieve] lookups]
      (if-let [candidate (candidate? abbr)]
        (retrieve candidate)))))





