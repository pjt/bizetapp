(ns bizet
    (:use (compojure html http str-utils) saxon))

(load "entries.clj")

(defn set-count
    "Reduces collection to a hash whose keys are the unique items
    in the collection & whose values are the number of times the
    item appears. Hash is sorted by number, descending."
    [coll]
    (sort-by
        #(val %)                        ; grab val of hash item
        (fn [x y] (- (compare x y)))    ; descending order
        (reduce
            (fn [accum item]
                (merge-with 
                    +
                    accum
                    {item 1}))
            {} 
            coll)))


;; HTML funcs

(defn css-path
    "Concatenate file with path prefix for CSS files. File
    should be given as string or keyword, without .css suffix."
    [file]
    (let    [prefix "/css/"
             file (name file)]
        (str prefix file ".css")))

(defmacro nav
    "Navigation section."
    [heading & items]
    `(html
        [:div {:class "nav"}
            [:strong ~heading]
            [:ul
                ~@(for [[path label] items] 
                    [:li (link-to path label)])]]))

(defn templ
    "HTML template."
    [title & body]
    [{"Content-Type" "text/html;encoding=UTF8"}
        (html
        (doctype :xhtml-strict)
        [:html
          [:head
            [:link {:rel "stylesheet" :type "text/css" :href (css-path :main)}]
            [:title title]]
          [:body
            body]])
    ])

;; Docs

(def htmlstyle (compile-xslt "public/bizet.xsl"))
(def entries ; hard-coded, will fix
    {"jeuxdenfants" (compile-file "public/jeuxdenfants.xml")
     "symphonie1"   (compile-file "public/symphonie1.xml")
     "viellechanson" (compile-file "public/viellechanson.xml")})

(let [div-id (compile-xpath "//div/@xml:id/string()")]
    (def divids 
            (set-count (apply concat
                    (map div-id (vals entries))))))

(let [tags (compile-xpath "//element()/local-name()")]
    (def all-tags
        (set 
            (mapcat tags (vals entries)))))
                
    

;; Servlet def, Route defs

(defservlet bizetapp
    "Bizet Catalog app."
    (GET "/"
        (templ "Bizet Entries" 
            [:h2 "Entries"]
            (unordered-list 
                (map #(link-to (str "/entry/" %) %) (keys entries)))
            [:h2 "Divisions"]
            (unordered-list 
                (map 
                    #(link-to (str "/section/" (key %)) 
                        (format "%s (%s)" (key %) (val %)))
                    divids))
            [:h2 "Search by Tag"]
            (form-to [GET "/search/in/"]
                [:p "Search by "
                    [:select {:name "tag"} (select-options all-tags)]
                    "in " (text-field :terms)
                    (submit-button "Search")])))
    (GET "/entry/:name" 
        (templ "Bizet Entry" (htmlstyle (entries (route :name)))))
    (GET "/section/:name"
        (let [section (route :name)
              div   (compile-xpath (format "//div[@xml:id='%s']" section))]
            (templ (format "%s Sections" section)
                [:h1 (format "%s Sections" section)]
                (str-map
                    (fn [entry] 
                        (let [[nm doc] entry]
                            (if-let sec (div doc)
                                (html
                                    [:h2 (link-to (format "/entry/%s" nm) nm)]
                                    (htmlstyle sec)))))
                    entries))))
              
    (GET "/search/"
        (templ "Search"
            [:p "Search not yet implemented."]))
    (GET "/search/in/"
       (templ "Search In Tags"
            [:p "Search in tags not yet implemented."])) 
    (GET "/*" (serve-file "public" full-path)))
    ;(ANY "/*" (page-not-found)))
