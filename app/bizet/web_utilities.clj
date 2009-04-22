(ns bizet.web-utilities
    (:use (bizet utilities)
          (clojure.contrib str-utils)
          compojure
          saxon))

;; HTML funcs

(def *context* nil)

(defn context-path
  [path]
  (str (or *context* "") path))

(defn css-path
  "Concatenate file with path prefix for CSS files. File
  should be given as string or keyword, without .css suffix."
  [file]
  (let  [prefix (context-path "/css/")
         file (str* file)]
    (str prefix file ".css")))

(defmacro nav
  "Navigation section."
  [heading & items]
  `(html
    (if (not= "" ~heading) [:strong ~heading] "")
    [:ul
     (map (fn [[label# path#]] [:li (link-to path# label#)]) ~(vec items))]))

(defn templ
  "HTML template."
  [title & body]
  [{"Content-Type" "text/html;charset=UTF-8"}
   (html (doctype :xhtml-strict)
    (xhtml-tag "en"
      [:head
       (apply include-css (map css-path [:main :tei :liquid-blueprint]))
       (apply include-js (map context-path ["/js/jquery.js" "/js/bizet.js"]))
       [:title title]]
      [:body
       [:div.container
         [:div#nav.column.span-3 
            (nav "" ["Home" "/"] ["Entries" "/entries/"])]
         [:div.column.span-18 body]
         [:div#margin.column.prepend-1.span-2.last]]]))])

;; Docs

(def htmlify 
  (comp (fn [nd] (let [sw (java.io.StringWriter.)]
                   (serialize nd sw {:method "xhtml"})
                   (str sw)))
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

