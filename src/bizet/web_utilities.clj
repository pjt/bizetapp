(ns bizet.web-utilities
    (:use bizet.utilities
          ;(clojure.contrib str-utils)
          [compojure :exclude (with-context)]
          saxon))

;; Middleware

(def *context* nil)

(defn with-context 
  "Wrap handler in URL-context code, making routes match only URLs that begin
  with ctx, & binding *context* to the value of ctx. (ctx should not contain the
  trailing slash.)

  Written by James Reeves, compojure mailing list, 04 April 2009."
  [ctx & route-seq] 
  (let [handler (apply routes route-seq) 
        pattern (re-pattern (str "^" ctx "(/.*)?"))] 
    (fn [request] 
      (if-let [[_ uri] (re-matches pattern (:uri request))] 
        (binding [*context* (str *context* ctx)] 
          (handler (assoc request :uri uri))))))) 

(defn url [path] 
  "Make url relative to *context*."
  (str *context* path))

(defn add-trailing-slash
  "Middleware function that adds a trailing slash & retries request when handler 
  returns 404 & :uri didn't already end with a slash."
  [handler]
  (fn [request]
    (let [response (handler request)
          uri      #^String (:uri request)]
      (if (and (= (:status response) 404)
               (not (.endsWith uri "/")))
        (handler (assoc request :uri (str uri "/")))
        response))))

(defn restrict-to-ip
  "Middleware function that restricts requests to clients whose IP address
  matches the pattern (which should be a regex pattern)."
  [handler ip-pattern]
  (fn [request]
    (if (re-matches ip-pattern (:remote-addr request))
      (handler request)
      {:status 403 :headers {} :body "This is not for you."})))

;; HTML funcs

(defn ctx-link-to
  "Like Compojure's link-to, but with *context*-sensitive path."
  [path & content]
  (apply link-to (url path) content))

(defn css-path
  "Concatenate file with path prefix for CSS files. File
  should be given as string or keyword, without .css suffix."
  [file]
  (let  [prefix (url "/css/")
         file (str* file)]
    (str prefix file ".css")))


(defn nav
  "Navigation section."
  [items]
  (unordered-list
   (map (fn [[label path]] (ctx-link-to path label)) items)))

(defn templ
  "HTML template."
  [title & body]
  (let [opts? (first body)
        opts  (when (and  (map? opts?)
                          (every? #{:css :js} (keys opts?)))
                      opts?)
        css   (concat [:main] (as-coll (:css opts)))
        js    (concat ["/js/jquery.js" "/js/bizet.js"] (as-coll (:js opts)))
        body  (if opts (rest body) body)]
    [{:headers {"Content-Type" "text/html;charset=UTF-8"}}
    ;[{:headers {"Content-Type" "application/xhtml+xml;charset=UTF-8"}}
     (html 
      (doctype :html5)
      (xhtml-tag "en"
        [:head
         (apply include-css (map css-path css))
         (apply include-js (map url js))
         [:title title]]
        [:body.bp
         [:div#container
           [:div#header
              ;(ctx-link-to "/" [:img {:src (url "/img/bizet-water.gif")}])
              [:div#title
               [:span "THE BIZET CATALOGUE"]
               [:span "by Hugh Macdonald"]]
             [:div#nav
                (nav {"Home" "/"
                      "List of Works" "/works"
                      "List of Transcripts" "/transcripts"
                      "Indices" "/indices"})
                (nav {"Using the Catalogue" "/howto"
                      "Compiler's Preface" "/preface"
                      "Biography" "/bio"
                      "Contact Us" "/contact"})]
              (form-to [:get (url "/search")]
                [:input#search {:name "q" :value "Search"}])]
           [:div#main body]
           [:div#margin]]]))]))

            
;; Docs

(def htmlify 
  (comp str 
        #(serialize % (java.io.StringWriter.) 
                    {:method "html" :omit-xml-declaration "yes"})
        (compile-xslt (java.io.File. "public/tei-to-html.xsl"))))

;; Error handling

(defn handle-error
  "Returns error message."
  [msg]
  [500 ; NOT SURE IF RIGHT STATUS CODE
    (templ "Error"
      [:p (format "Error: %s" msg)])])

(defmacro catch-all
  "Tries body with any AmbiguousMatch, NotFound errors caught
  & handled appropriately."
  [& body]
  `(try 
     ~@body
     (catch Exception e#
        (.printStackTrace e#)
        (handle-error (.getMessage e#)))))

(defn catching [func] 
  "Decorator that wraps func in catch-all macro."
  (fn [& args] (catch-all (apply func args))))


;; Route funcs

(defn trimming-serve-file
  "Attempts to serve file, trimming directory components left-to-right
  if file not found."
  ([path] (trimming-serve-file "public" path))
  ([fs-root path]
    (or (serve-file fs-root path)
      (loop [paths (map #(reduce-path-by path %) (reverse (dirname-seq path)))]
        (when (seq paths)
          (or (serve-file fs-root (first paths))
            (recur (rest paths)))))
     :next)))

