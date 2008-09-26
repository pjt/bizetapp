(in-ns 'bizet)

(defstruct entry :id :title :comp-date :divids :tags :doc)

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
                "//div[@xml:id='history']/date[@type='composition'][1]/string()")
            (comp set (compile-xpath "//div/@xml:id/string()"))
            (comp set (compile-xpath "/TEI/text//element()/local-name()"))
            identity)] 

    (defn- entry-builder
        "Builds an individual entry struct."
        [path]
        (let [doc (compile-file path)]
            (struct entry
                ((:id entry-fns) doc)
                ((:title entry-fns) doc)
                ((:comp-date entry-fns) doc)
                ((:divids entry-fns) doc)
                ((:tags entry-fns) doc)
                ((:doc entry-fns) doc)))))
            

(def dot-xml
    (proxy [java.io.FilenameFilter] []
        (accept [dir name] (.endsWith name ".xml"))))

(def entries
    (apply hash-map
        (mapcat 
            (fn [path] 
                (let [e (entry-builder path)]
                    [(:id e) e]))
            (map str (.listFiles (java.io.File. "public") dot-xml)))))

;(def entries
;    (ref 
;        (apply hash-map
;            (mapcat (fn [path] [path (entry-builder path)])
;                (.list (File. "public") dot-xml)))))

