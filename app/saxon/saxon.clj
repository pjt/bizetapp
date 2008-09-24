(ns saxon

;; Saxon Clojure wrapper
;; Requires that the saxon9.jar & the saxon9-s9api.jar be on classpath

    (:import 
        (java.io File)
        (javax.xml.transform.stream StreamSource)
        (net.sf.saxon.s9api 
            Processor 
            Serializer 
            Serializer$Property
            XPathCompiler
            XdmDestination
            XdmValue
            XdmItem
            XdmNode
            XdmNodeKind
            XdmAtomicValue
            QName)))

;;
;; Private functions
;;

(defn- get-proc
    "Returns the Saxon Processor object, the thread-safe 
    generator class for documents, stylesheets, & XPaths.
    Creates & defs the Processor if not already created."
    []
    (defonce #^{:private true} *p* (Processor. false))
    *p*)

;; Well, except this is public -- maybe doesn't need to be
(defn atomic?
    "Returns true if XdmItem or a subclass
    (XdmAtomicValue, XdmNode) is an atomic value."
    [#^XdmItem val]
    (.isAtomicValue val))

(defn- wrap-xdm-val
    "Makes an XdmValue Clojure-friendly. A Saxon value 
    (XdmValue) is a sequence of zero or more items, where 
    an item is either an atomic value (number, string, 
    URI) or a node. 
    
    This function makes sequences greater than one into 
    Clojure seqs, and turns XdmAtomicValues into their
    corresponding Java datatypes (Strings, the numeric
    types)."
    [#^XdmValue val]
    (let    
        [size   (.size val)
         unwrap-atom #(if (atomic? %) (.getValue #^XdmAtomicValue %) %)]
        (cond 
            (= size 0)
                nil
            (= size 1)
                (unwrap-atom val)
            :default
                (map unwrap-atom val))))

;;
;; Public functions
;;

(defn compile-file
    "Compiles XML file into an XdmNode, the Saxon 
    currency for in-memory tree representation."
    [#^String path]
    (.. (get-proc) (newDocumentBuilder) (build (File. path))))
    ;(StreamSource. (File. path)))

(defn compile-xslt
    "Compiles stylesheet (given as path to file), returns 
    function that applies it to XML doc or node."
    [xslt-file]
    (let    [proc   (get-proc)
             comp   (.newXsltCompiler proc)
             exe    (.compile comp (StreamSource. (File. xslt-file)))]

        (fn [xml & params]
            (let    [xml    (if (string? xml) 
                                (compile-file xml) 
                                xml)
                     xdm-dest    (XdmDestination.)
                     transformer (.load exe)] ; created anew, is thread-safe
                (when params
                    (let [prms  (first params)
                          ks    (keys prms)]
                        (doseq k ks
                            (.setParameter transformer
                                (QName. (name k))
                                (XdmAtomicValue. (k prms))))))
                (doto transformer
                    (setInitialContextNode xml)
                    (setDestination xdm-dest)
                    (transform))
                (.getXdmNode xdm-dest)))))

; helper for compile-xpath
(defn- add-ns-to-xpath
    "Adds namespaces to XPathCompiler from map. Returns same 
    XPathCompiler."
    [#^XPathCompiler xp-compiler ns-map]
    (doseq [pre uri] ns-map
        (.declareNamespace xp-compiler (name pre) uri))
    xp-compiler)
        
(defn compile-xpath
    "Compiles XPath expression (given as string), returns
    function that applies it to XML doc or node. Takes optional
    map of prefixes (as keywords) and namespace URIs."
    [#^String xpath & ns-map]
    (let    [proc   (get-proc)
             comp   (.newXPathCompiler proc) 
             comp   (if ns-map 
                        (add-ns-to-xpath comp (first ns-map)) 
                        comp)
             exe    (.compile comp xpath)
             selector (.load exe)]

        (fn [xml]
            (let [xml (if (string? xml) 
                        (compile-file xml) 
                        xml)]
                (.setContextItem selector xml)
                (wrap-xdm-val (.evaluate selector)))))) 

;; Node functions

(defn parent-node
    "Returns parent node of passed node."
    [#^XdmNode nd]
    (.getParent nd))

(defn node-name
    "Returns the name of the node (as QName)."
    [#^XdmNode nd]
    (.getNodeName nd))

(defn node-ns
    "Returns the namespace of the node or node name."
    [q]
    (if (= (class q) QName)
        (.getNamespaceURI q)
        (node-ns (node-name q))))

(def #^{:private true} 
    node-kind-map
        {XdmNodeKind/DOCUMENT   :document
         XdmNodeKind/ELEMENT    :element
         XdmNodeKind/ATTRIBUTE  :attribute
         XdmNodeKind/TEXT       :text
         XdmNodeKind/COMMENT    :comment
         XdmNodeKind/NAMESPACE  :namespace
         XdmNodeKind/PROCESSING_INSTRUCTION :processing-instruction})

(defn node-kind
    "Returns keyword corresponding to node's kind."
    [#^XdmNode nd]
    (node-kind-map (.getNodeKind nd)))

; Node-kind predicates

(defn document?
    "Returns true if node is document."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/DOCUMENT))

(defn element?
    "Returns true if node is element."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/ELEMENT))

(defn attribute?
    "Returns true if node is attribute."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/ATTRIBUTE))

(defn text?
    "Returns true if node is text."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/TEXT))

(defn comment?
    "Returns true if node is comment."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/COMMENT))
  
(defn namespace?
    "Returns true if node is namespace."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/NAMESPACE))

(defn processing-instruction?
    "Returns true if node is processing instruction."
    [#^XdmNode nd]
    (.equals (.getNodeKind nd) XdmNodeKind/PROCESSING_INSTRUCTION))


