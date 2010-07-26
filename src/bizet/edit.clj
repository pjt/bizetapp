(ns bizet.edit
  (:use saxon bizet.svn
     [bizet.entries :only (entries)]
     [bizet.pages :only (edit-entry)]
     [bizet.web-utilities :only (templ url)]
     [compojure.http.session :only (alter-session session-assoc with-session)]
     [compojure.http.helpers :only (redirect-to)]
     [compojure.control :only (decorate)]
     [compojure.html.gen :only (html)]
     compojure.html.form-helpers
     compojure.http.routes))

(def update-tmpl
  "<?xml version='1.0'?>
  <xsl:stylesheet
    version='2.0'
    xpath-default-namespace='%1$s'
    xmlns='%1$s'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:template match='node()|@*'>
      <xsl:copy>
        <xsl:apply-templates select='@*'/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:template>
    <xsl:template match='%2$s'>%3$s</xsl:template>
  </xsl:stylesheet>")

(defn- prep-update
  [#^String update]
  (-> update (.replaceAll "<!--(.*?)-->" "<xsl:comment>$1</xsl:comment>")))

(let [root-el (compile-xquery "/element()")]
  (defn- root-ns
    "Returns namespace of document's root element."
    [doc]
    (node-ns (root-el doc))))

(defn apply-edit
  "Applies edit to a doc, given select query & update query. Select query
  usually an XPath to a node; update query usually a literal element for
  replacement. Returns edited doc."
  [doc select update]
  (let [update (prep-update update)
        nmspce (root-ns doc)]
    ((compile-xslt (format update-tmpl nmspce select update)) doc)))

(defn return-parent
  [doc #^String select]
  (let [parent (str select "/..")
        nmspce (root-ns doc)]
    (query (with-default-ns nmspce parent) doc)))

(defn- as-xhtml-type
  [& args]
  (apply vector 
         ;{:headers {"Content-Type" "application/xhtml+xml;charset=UTF-8"}} args))
         {:headers {"Content-Type" "text/html;charset=UTF-8"}} args))

(let [webeditor (compile-xslt (java.io.File. "public/webedit.xsl"))]
  (def webedit 
    (comp as-xhtml-type 
          str
          #(serialize % (java.io.StringWriter.) {:method "html"})
          webeditor)))


; routes

(defn retrieve-for-edit
  "Retrieve & return XML document, run through the editing stylesheet. 
  Also, as side-effects, check out file to filesystem, associate document
  & checkout location in session map."
  [session works id]
  (let [xml     (works id)
        co-file (future (svn-co *remote-repos*
                  (:repos-path (meta xml)) (make-co-path-root (:id session))
                    (:svn-user session) (:svn-pword session)))]
    [(session-assoc :editing-doc xml :svn-co-file co-file)
     (webedit (:doc xml))]))

(defn make-edit
  "Apply an edit to the currently edited document (:editing-doc).
  Update reference in session map, re-serialize doc to checkout location,
  and return the HTML for the edited node's parent."
  [session select update]
  (let [xml     (:editing-doc session) 
        edited  (apply-edit (-> session :editing-doc :doc) select update)]
    (future (serialize edited @(:svn-co-file session) {:indent "yes"}))
    [(alter-session assoc-in [:editing-doc :doc] edited)
     (webedit (return-parent edited select))]))

(defn commit
  "Commit changes from checked-out file."
  [session svn-co-file msg]
  (svn-up svn-co-file (:svn-user session) (:svn-pword session))        
  (svn-ci svn-co-file msg (:svn-user session) (:svn-pword session)))

(defn diff
  "Return svn diff on checked-out file."
  [svn-co-file]
  (svn-diff svn-co-file))

(defn svn-login
  "Authenticate against remote SVN repository."
  [session params]
  (if (svn-auth *remote-repos* (params :svn-user) (params :svn-pword))
    [(session-assoc :svn-user (params :svn-user) :svn-pword (params :svn-pword))
     (redirect-to (or (params :edit-url) "/"))]
    :next))

;; Routes themselves

(defroutes edit-routes "Routes for editing."

  (POST "/svn-login"
    (if (every? identity (map params [:svn-user :svn-pword]))
      (svn-login session params)
      :next))

  (ANY "*" ; svn authenticate beyond this point
    (if (not (:svn-user session))
      (html
        [:html [:head [:title "Log in"]]
         [:body
          (form-to [:post (url "/svn-login")]
            (label "svn-user" "Username") " " (text-field "svn-user")
            (label "svn-pword" "Password") " " (password-field "svn-pword")
            (submit-button "Go")
            (hidden-field "edit-url" (or (params :edit-url) (url (:uri request)))))]])
      :next))

  (GET "/diff"
    (if-let [co-file @(:svn-co-file session)]
      (diff co-file)
      :next))

  (GET "/*"
    (retrieve-for-edit session @entries (params :*)))

  (POST "/"
    (if (and (params :select) (-> session :editing-doc :doc))
      (make-edit session (params :select) (params :update))
      :next))

  (POST "/commit"
    (if (params :msg)
      (commit session @(:svn-co-file session) (params :msg))
      :next))

  (GET "/bounce"
    ; TODO: these interfering with stuff at /edit/* -- investigate!
    #_(dosync
      (commute session dissoc :editing-doc :svn-co-file)) ""))

(decorate edit-routes (with-session {:type :memory :expires (* 8 60 60)}))

