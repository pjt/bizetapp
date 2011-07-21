(ns bizet.entries
  (:use bizet.utilities saxon)
  (:import java.io.File java.util.Date))

(def *base-dir* "bz-repos")
(def *works-dir* (str *base-dir* "/xml"))
(def *transcripts-dir* (str *base-dir* "/transcripts"))
(def *xsl-dir*  (str *base-dir* "/xsl"))
(def entries (ref {}))
(def stylesheets (ref {}))

(defstruct entry :id :title :comp-date :sections :tags :doc)
(defstruct stylesheet :filename :fn)

(defn- as-file {:tag File} [f] (if (string? f) (File. f) f))
(defn- last-mod [f] (.lastModified (as-file f)))
(defn- canon-path [f] (.getCanonicalPath (as-file f)))

(let [specials {\á \a, \à \a, \â \a, \ä \a
                \é \e, \è \e, \ê \e, \ë \e
                \í \i, \ì \i, \î \i, \ï \i
                \ó \o, \ò \o, \ô \o, \ö \o
                \ú \u, \ù \u, \û \u, \ü \u
                \æ "ae", \œ "oe"}
      conv    #(get specials % %)]

  (def title->id
    (comp (partial apply str)
          (partial map conv)
          (partial filter #(Character/isLetterOrDigit %))
          downcase)))

(let 
  [title-fn (compile-tei-q
                "/TEI/teiHeader/fileDesc/titleStmt/title[1]/string()")
   compile-tei-file
            (comp 
              (compile-xslt (File. (str *xsl-dir* "/name-gen.xsl")))
              (compile-xslt (File. (str *xsl-dir* "/into-tei-ns.xsl")))
              compile-xml)
   entry-fns
      (struct entry
          (comp title->id title-fn) 
          title-fn
          (compile-tei-q
              "(//date[@type='composition'])[1]/(@when|@notBefore|@from|@notAfter)[1]/string()")
          (comp set (compile-tei-q "/TEI/text/div/@type/string()"))
          (comp set (compile-tei-q "/TEI/text//element()/local-name()"))
          identity)] 

  (defn- entry-builder
    "Builds an individual entry struct."
    [file]
    (let [doc (compile-tei-file file)]
      (with-meta
        (struct entry
          ((:id entry-fns) doc)
          ((:title entry-fns) doc)
          ((:comp-date entry-fns) doc)
          ((:sections entry-fns) doc)
          ((:tags entry-fns) doc)
          ((:doc entry-fns) doc))
          ; metadata
          {:file file 
           :modified (last-mod file) 
           :repos-path (reduce-path-by (canon-path file) (canon-path *base-dir*))
           :transcript? (.contains (.getCanonicalPath file) "/transcripts/")})))) 

(defn- xsl-builder
  "Builds an individual xsl entry struct."
  [file]
  (with-meta
    (struct stylesheet (basename (str file) ".xsl") (compile-xslt file))
    {:file file :modified (last-mod file)}))


(defn pull-entries-from-fs
  "Returns entries hash-map created with .xml files from filesystem; if entry 
  already exists for file & file hasn't been modified, keeps existing entry & 
  doesn't read file."
  [entries]
  (let [e-with-file (transform-keyval entries [(:file (meta v)) v])]
    (into {}
      (map
        #(let [[k v :as e] (find e-with-file %)]
          (if (and e (>= (:modified (meta v)) (last-mod %)))
            [(:id v) v]
            (let [new-entry (entry-builder %)]
                [(:id new-entry) new-entry])))
        (concat 
          (.listFiles (java.io.File. *works-dir*) dot-xml)
          (.listFiles (java.io.File. *transcripts-dir*) dot-xml))))))

(defn transcript?
  [entry]
  (:transcript? (meta entry)))

(defn only-transcripts
  [entries]
  (into {}
        (filter (fn [[id entry]] (transcript? entry)) entries)))

(defn only-works
  [entries]
  (into {}
        (filter (fn [[id entry]] ((complement transcript?) entry)) entries)))

(defn pull-stylesheets-from-fs
  "Returns stylesheets hash-map created with .xsl files from filesystem; if entry 
  already exists for file & file hasn't been modified, keeps existing entry & 
  doesn't read file."
  [entries]
  (let [e-with-file (transform-keyval entries [(:file (meta v)) v])]
    (into {}
      (map
        #(let [[k v :as e] (find e-with-file %)]
          (if (and e (>= (:modified (meta v)) (last-mod %)))
            [(:filename v) v]
            (let [new-entry (xsl-builder %)]
                [(:filename new-entry) new-entry])))
        (.listFiles (java.io.File. *xsl-dir*) dot-xsl)))))
