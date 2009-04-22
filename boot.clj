(ns boot
   (:use compojure)
   (:require bizet))

(defserver server
  {:port 8080}
  "/*" (servlet bizet/bizetapp))

;; Start the server
(start server)
