(ns bizet.run
  (:gen-class)
  (:use [compojure :only (run-server servlet)]
        [bizet.web-utilities :only (with-context)])
  (:require bizet))

(defn -main
  [& args]
  (run-server {:port 8080}
    "/bizet/*" (servlet (with-context "/bizet" bizet/bizetapp))))
