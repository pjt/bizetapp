(in-ns 'bizet)

(def dot-xml
    (proxy [java.io.FilenameFilter] []
        (accept [dir name] (.endsWith name ".xml"))))

