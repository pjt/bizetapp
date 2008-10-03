(in-ns 'bizet)

(defstruct entry :id :title :comp-date :divids :tags :doc)

(def mod (memfn lastModified))

(defn- title->id
    [title]
    (str-map #(Character/toLowerCase %) 
        (filter (complement #(Character/isWhitespace %)) title)))

(let 
    [title-fn (compile-xpath 
                "/TEI/teiHeader/fileDesc/titleStmt/title[@type='main'][1]/string()")
     entry-fns
        (struct entry
            (comp title->id title-fn) 
            title-fn
            (compile-xpath 
                "//div[@xml:id='history']//date[@type='composition'][1]/string()")
            (comp set (compile-xpath "//div/@xml:id/string()"))
            (comp set (compile-xpath "/TEI/text//element()/local-name()"))
            identity)] 

    (defn- entry-builder
        "Builds an individual entry struct."
        [file]
        (let [doc (compile-file (str file))]
            (with-meta
                (struct entry
                    ((:id entry-fns) doc)
                    ((:title entry-fns) doc)
                    ((:comp-date entry-fns) doc)
                    ((:divids entry-fns) doc)
                    ((:tags entry-fns) doc)
                    ((:doc entry-fns) doc))
                    ; metadata
                    {:file file :modified (mod file)})))) 
            

(def #^{:private true} 
    *data-dir* "public")

(def #^{:private true}
    dot-xml
        (proxy [java.io.FilenameFilter] []
            (accept [dir name] (.endsWith name ".xml"))))

(defmacro transform-keyval
    "Transforms key-value pairs according to passed form.
    Passed form will have the vars 'k' & 'v' available to
    it, & will be evaulated for each key-value pair. Results
    of evaluation should be a vector of new [key val]."
    [hashmap form]
    `(apply hash-map
        (mapcat
            (fn ~'[[k,v]]
                ~form)
            (seq ~hashmap))))

(defn pull-entries-from-fs
    "Returns entries hash-map created with .xml files from 
    filesystem; if entry already exists for file & file hasn't
    been modified, keeps existing entry & doesn't read file."
    [entries]
    (let [e-with-file (transform-keyval entries [(:file (meta v)) v])
          mod (memfn lastModified)]
        (apply conj {}
            (map
                #(let [e (find e-with-file %)]
                    (if (and e (>= (:modified (meta (val e))) (mod %)))
                        [(:id (val e)) (val e)]
                        (let [new-entry (entry-builder %)]
                            [(:id new-entry) new-entry])))
                (.listFiles (java.io.File. *data-dir*) dot-xml)))))
 
(def entries (ref {}))

;(def entries
;    (ref 
;        (apply hash-map
;            (mapcat 
;                (fn [file] 
;                    (let [e (entry-builder file)]
;                        [(:id e) e])) ; key entry to its id 
;                (.listFiles (java.io.File. *data-dir*) dot-xml)))))
;
;(defn update-files
;    "Updates, in-place, the 'entries' ref by polling
;    the filesystem to check for changes to existing, or
;    new, files."
;    [])
