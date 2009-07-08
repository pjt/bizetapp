(ns bizet.web-utilities
    (:use (bizet utilities)
          (clojure.contrib str-utils)
          compojure
          saxon))

;; HTML funcs

(declare *context*) 

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
        (binding [*context* ctx] 
          (handler (assoc request :uri uri))))))) 

(defn url [path] 
  "Make url relative to *context*."
  (str *context* path))

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

(defmacro nav
  "Navigation section."
  [heading & items]
  `(html
    (ctx-link-to "/" [:img {:src (url "/img/bizet-water.gif")}])
    (if (not= "" ~heading) [:strong ~heading] "")
    [:ul
     (map (fn [[label# path#]] [:li (ctx-link-to path# label#)]) ~(vec items))]))

(defn templ
  "HTML template."
  [title & body]
  ;[{"Content-Type" "text/html;charset=UTF-8"}
  [{"Content-Type" "application/xhtml+xml;charset=UTF-8"}
   (html 
    (xhtml-tag "en"
      [:head
       (apply include-css (map css-path [:main :tei :liquid-blueprint]))
       (apply include-js (map url ["/js/jquery.js" "/js/bizet.js"]))
       [:title title]]
      [:body
       [:div.container
         ;[:div#top.column.span-24
         ; [:div [:img {:src (url "/img/bizet-water.gif")}]]]
         [:div#nav.column.span-3 
            (nav "" ["Home" "/"] ["Entries" "/entries/"])]
         [:div.column.prepend-1.span-17 body]
         [:div#margin.column.prepend-1.span-2.last]]]))])

;; Docs

(def htmlify 
  (comp str 
        #(serialize % (java.io.StringWriter.) {:method "xhtml"})
        (compile-xslt (compile-file "public/tei-to-html.xsl"))))

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

