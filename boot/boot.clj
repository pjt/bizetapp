;; This file will be evaluated when script/repl or script/run are executed.
;; Populate this file with the code required to start your application.

;; An example boot setup is included below:
(use 'compojure.jetty)

;; Load app/... code
(require 'bizet)

;; Define a new HTTP server on port 8080, with the hello-world servlet
;; defined in app/example.clj
(defserver server
  {:port 8080}
  ;"/*" example/hello-world)
  "/*" bizet/bizetapp)

;; Start the server
(start server)
