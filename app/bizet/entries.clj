(ns bizet.entries
  (:use bizet.utilities saxon))

(def *data-dir* "../bz-repos/xml")

(defstruct entry :id :title :comp-date :divids :tags :doc)

(defn last-mod [#^java.io.File f] (.lastModified f))

(defn- title->id
  [title]
  ((comp downcase (partial apply str))
      (remove #(Character/isWhitespace %) title)))

(let 
  [title-fn (compile-xpath 
                "/TEI/teiHeader/fileDesc/titleStmt/title[@type='main'][1]/string()")
   entry-fns
      (struct entry
          (comp title->id title-fn) 
          title-fn
          (compile-xpath 
              "//div[@xml:id='composition']//date[@type='composition'][1]/string()")
          (comp set (compile-xpath "/TEI/text/div/@xml:id/string()"))
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
          {:file file :modified (last-mod file)})))) 
            


(defn pull-entries-from-fs
  "Returns entries hash-map created with .xml files from 
  filesystem; if entry already exists for file & file hasn't
  been modified, keeps existing entry & doesn't read file."
  [entries]
  (let [e-with-file (transform-keyval entries [(:file (meta v)) v])]
    (apply conj {}
      (map
        #(let [e (find e-with-file %)]
          (if (and e (>= (:modified (meta (val e))) (last-mod %)))
            [(:id (val e)) (val e)]
            (let [new-entry (entry-builder %)]
                [(:id new-entry) new-entry])))
        (.listFiles (java.io.File. *data-dir*) dot-xml)))))
 
