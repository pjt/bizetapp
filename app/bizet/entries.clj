(ns bizet.entries
  (:use bizet.utilities saxon)
  (:import java.io.File java.util.Date))

(def *data-dir* "../bz-repos/xml")

(defstruct entry :id :title :comp-date :sections :tags :doc)

(defn last-mod [#^File f] (.lastModified f))
(defn compile-tei-q 
  [q] (compile-xquery (with-default-ns "http://www.tei-c.org/ns/1.0" q)))

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
            (comp (compile-xslt (compile-file "public/into-tei-ns.xsl")) compile-file)
   entry-fns
      (struct entry
          (comp title->id title-fn) 
          title-fn
          (compile-tei-q
              "//date[@type='composition'][1]/(@when|@notBefore|@from)/string()")
          (comp set (compile-tei-q "/TEI/text/div/@type/string()"))
          (comp set (compile-tei-q "/TEI/text//element()/local-name()"))
          identity)] 

  (defn- entry-builder
    "Builds an individual entry struct."
    [file]
    (let [doc (compile-tei-file (str file))]
      (with-meta
        (struct entry
          ((:id entry-fns) doc)
          ((:title entry-fns) doc)
          ((:comp-date entry-fns) doc)
          ((:sections entry-fns) doc)
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
 
