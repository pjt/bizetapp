;; compojure.http
;;
;; Compojure library for constructing HTTP servlet proxy objects. 
;; 
;; Here's a small taste of the syntax:
;;
;;   (defservlet example
;;     "An example servlet that contains a bit of everything."
;;     (GET "/"
;;       "Hello World")
;;     (GET "/:name"
;;       (str "Hello " (route :name)))
;;     (GET "/image"
;;       (file "public/image.png"))
;;     (GET "/error"
;;       [500 "Error 500"])
;;     (POST "/mesg"
;;       (redirect-to "/"))
;;     (GET "/header"
;;       [{"X-Fortune" "Be prepared!"}
;;        "Custom X-Header"])
;;     (PUT "/:var"
;;       (dosync
;;         (commute session assoc :var (route :var)))
;;     (ANY "/*"
;;       (page-not-found))

(ns compojure.http
  (:use (compojure control
                   file-utils
                   parser
                   str-utils))
  (:import (java.io File
                    FileInputStream)
           (java.net URL)
           (java.util Enumeration
                      Map$Entry)
           (javax.servlet ServletContext)
           (javax.servlet.http Cookie
                               HttpServlet
                               HttpServletRequest
                               HttpServletResponse)))
;;;; Mimetypes ;;;;
 
(defn context-mimetype
  "Guess the mimetype of a filename. Defaults to 'application/octet-stream'
  if the mimetype is unknown."
  [#^ServletContext context filename]
  (or (.getMimeType context filename)
      "application/octet-stream"))
 
;;;; Routes ;;;;
 
(defstruct url-route
  :regex
  :keywords)
 
(defn compile-route
  "Turn a route string into a regex and seq of symbols."
  [route-str]
  (let [splat #"\*"
        word  #":(\w+)"
        path  #"[^:*]+"]
    (struct url-route
      (re-pattern
        (apply str
          (parse route-str
            splat "(.*?)"
            word  "([^/.,;?]+)"
            path  #(re-escape (.group %)))))
      (filter (complement nil?)
        (parse route-str
          splat :*
          word  #(keyword (.group % 1))
          path  nil)))))
 
(defn- match-route
  "Match a path against a parsed route. Returns a map of keywords and their
  matching path values."
  [route path]
  (let [matcher (re-matcher (route :regex) path)]
    (if (.matches matcher)
      (reduce
        (partial merge-with
          #(conj (ifn vector? vector %1) %2))
        {}
        (map hash-map
          (route :keywords)
          (rest (re-groups matcher)))))))

;;;; Handler functions ;;;;
 
(defstruct http-handler
  :method
  :route
  :function)

(defn- first-if-one
  "Returns the first value of a collection if there is only one element,
  otherwise returns the collection."
  [coll]
  (if (rest coll)
    coll
    (first coll)))

(defn- parse-key-value
  "Parse a key and value to make them more Clojure-friendly."
  [key val]
  [(keyword key) (first-if-one (vec val))])

(defn get-params
  "Creates a name/value map of all the request parameters."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat 
      (fn [#^Map$Entry e] (parse-key-value (.getKey e) (.getValue e)))
      (.getParameterMap request))))

(defn get-headers
  "Creates a name/value map of all the request headers."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat
      #(parse-key-value (.toLowerCase %)
                        (enumeration-seq (.getHeaders request %)))
       (enumeration-seq (.getHeaderNames request)))))

(defn get-session
  "Returns a ref to a hash-map that acts as a HTTP session that can be updated
  within a Clojure STM transaction."
  [#^HttpServletRequest request]
  (let [session (.getSession request)]
    (or (.getAttribute session "clj-session")
        (let [clj-session (ref {})]
          (.setAttribute session "clj-session" clj-session)
          clj-session))))
 
(defn get-cookies
  "Creates a name/value map from all of the cookies in the request."
  [#^HttpServletRequest request]
  (apply hash-map
    (mapcat #(list (keyword (.getName %)) (.getValue %))
             (.getCookies request))))

(defmacro handler-fn
  "Macro that wraps the body of a handler up in a standalone function."
  [& body]
  `(fn ~'[route context request]
     (let ~'[method    (.getMethod    request)
             full-path (.getPathInfo  request)
             params    (compojure.http/get-params  request)
             headers   (compojure.http/get-headers request)
             mimetype #(compojure.http/context-mimetype context (str %))
             session   (compojure.http/get-session request)
             cookies   (compojure.http/get-cookies request)]
       (do ~@body))))
 
(defn- find-method
  "Returns either the value of the '_method' HTTP parameter, or the method
  of the HTTP request."
  [#^HttpServletRequest request]
  (or (.getParameter request "_method")
      (.getMethod request)))

(defn- send-stream
  "Send a stream of data to the HTTP response."
  [context response stream filename]
  (.setHeader response
    "Content-Type" (context-mimetype context (str filename)))
  (with-open in stream
    (pipe-stream in (.getOutputStream response))))
  
(defn- update-response
  "Destructively update a HttpServletResponse using a Clojure datatype:
    string - Adds to the response body
    seq    - Adds all containing elements to the response body
    map    - Updates the HTTP headers
    Number - Updates the status code
    File   - Updates the response body via a file stream
    URL    - Updates the response body via a stream to the URL
    vector - Iterates through its contents, successively updating the response
             with each value"
  [#^HttpServletResponse response context update]
  (cond
    (vector? update)
      (doseq u update
        (update-response response context u))
    (string? update)
      (.. response (getWriter) (print update))
    (seq? update)
      (let [writer (.getWriter response)]
        (doseq d update
          (.print writer d)))
    (map? update)
      (doseq [k v] update
        (.setHeader response k v))
    (instance? Number update)
      (.setStatus response update)
    (instance? File update)
      (send-stream context response (new FileInputStream update) update)
    (instance? URL update)
      (send-stream context response (.openStream update) update)
    (instance? Cookie update)
      (.addCookie response update)))
        
(defn- apply-http-handler
  "Finds and evaluates the handler that matches the HttpServletRequest. If the
  handler returns :next, the next matching handler is evaluated."
  [handlers context request response]
  (let [method (find-method request)
        path     (.getPathInfo request)
        method= #(or (nil? %) (= method %))
        route?   (fn [handler]
                   (if (method= (handler :method))
                     (match-route (handler :route) path)))
        response? (fn [handler]
                    (if-let route-params (route? handler)
                      (let [func (handler :function)
                            resp (func route-params
                                       context
                                       request)]
                        (if (not= :next resp)
                          (or resp [])))))]
    (update-response response context
      (some response? handlers))))

;;;; Public macros ;;;;

(defmacro GET "Creates a GET handler."
  [route & body]
  `(struct http-handler "GET" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro PUT "Creates a PUT handler."
  [route & body]
  `(struct http-handler "PUT" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro POST "Creates a POST handler."
  [route & body]
  `(struct http-handler "POST" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro DELETE "Creates a DELETE handler."
  [route & body]
  `(struct http-handler "DELETE" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro HEAD "Creates a HEAD handler."
  [route & body]
  `(struct http-handler "HEAD" (compile-route ~route) (handler-fn ~@body)))
 
(defmacro ANY "Creates a handler that responds to any HTTP method."
  [route & body]
  `(struct http-handler nil (compile-route ~route) (handler-fn ~@body)))

;;;; Helper functions ;;;;

(defn new-cookie
  "Create a new Cookie object."
  [name value & attrs]
  (let [cookie (new Cookie (str* name) value)
        attrs  (apply hash-map attrs)
        iff    (fn [attr func] (if attr (func attr)))]
    (iff (attrs :comment) #(.setComment cookie %))
    (iff (attrs :domain)  #(.setDomain  cookie %))
    (iff (attrs :max-age) #(.setMaxAge  cookie %))
    (iff (attrs :path)    #(.setPath    cookie %))
    (iff (attrs :secure)  #(.setSecure  cookie %))
    (iff (attrs :version) #(.setVersion cookie %))
    cookie))
 
(defn redirect-to
  "A shortcut for a '302 Moved' HTTP redirect."
  [location]
  [302 {"Location" location}])
 
(defn page-not-found
  "A shortcut to create a '404 Not Found' HTTP response."
  ([]         (page-not-found "public/404.html"))
  ([filename] [404 (file filename)]))
 
(defn- find-index-file
  "Search the directory for index.*"
  [dir]
  (first (filter
          #(re-matches #"index\\..*" (.toLowerCase (.getName %)))
           (.listFiles dir))))

(defn serve-file
  "Attempts to serve up a static file from a directory, which defaults to
  './public'. Nil is returned if the file does not exist. If the file is a
  directory, the function looks for a file in the directory called 'index.*'."
  ([path]
    (serve-file "public" path))
  ([root path]
    (let [filepath (file root path)]
      (cond
        (.isFile filepath)
          filepath
        (.isDirectory filepath)
          (find-index-file filepath)))))
 
;;;; Servlet creation ;;;;
 
(defn http-service
  "Represents the service method called by a HttpServlet."
  [#^HttpServlet this request response handlers]
  (.setCharacterEncoding response "UTF-8")
  (apply-http-handler handlers
                      (.getServletContext this)
                      request
                      response))
(defn servlet
  "Create a servlet from a sequence of handlers."
  [& handlers]
  (proxy [HttpServlet] []
    (service [request response]
      (http-service this request response handlers))))

(defn update-servlet
  "Update an existing servlet proxy with a new set of handlers."
  [object & handlers]
  (update-proxy object
    {'service (fn [this request response]
                (http-service this request response handlers))}))

(defmacro defservlet
  "Defines a new servlet with an optional doc-string, or if a servlet is
  already defined, it updates the existing servlet with the supplied handlers.
  Note that updating is not a thread-safe operation."
  [name doc & handlers]
  (if (string? doc)
    `(do (defonce
          ~(with-meta name (assoc (meta name) :doc doc))
           (proxy [HttpServlet] []))
         (update-servlet ~name ~@handlers))
    `(do (defonce ~name
           (proxy [HttpServlet] []))
         (update-servlet ~name ~doc ~@handlers))))

(defmacro defservice
  "Defines a 'MyClass-service' method suitable for being called by a
  HttpServlet class created by genclass."
  [cname & handlers]
  `(defn ~(symbol (str cname "-service"))
   ~'[this request response]
     (http-service ~'this ~'request ~'response (list ~@handlers))))
