(ns boot
   (:use compojure [bizet.web-utilities :only (with-context)])
   (:require bizet))

(defserver server
  {:port 8080}
  "/bizet/*" (servlet (with-context "/bizet" bizet/bizetapp)))

;; Start the server
(start server)
