(defproject bizetapp "0.1-SNAPSHOT"
            :description "Prototype for the Bizet Thematic Catalogue."
            :main bizet.run
            ;:namespaces [bizet.run]
            :jvm-opts ["-Dfile.encoding=UTF8"]
            :dependencies [[org.clojure/clojure "1.2.0"]
                           [org.clojure/clojure-contrib "1.2.0"]
                           [compojure "0.3.2"]
                           [clojure-saxon "0.9.1-SNAPSHOT"]])
