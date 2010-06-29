(defproject bizetapp "0.1-SNAPSHOT"
            :description "Prototype for the Bizet Thematic Catalogue."
            :main bizet.run
            :namespaces [bizet.run]
            :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                           [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                           [compojure "0.3.2"]
                           [clojure-saxon "0.9.1"]])
            ;:dev-dependencies [[lein-clojars "0.5.0-SNAPSHOT"]
            ;                   [lein-war "0.0.1-SNAPSHOT"]])
