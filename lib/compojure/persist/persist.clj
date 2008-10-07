;; Lightweight persistence for Clojure data structures
(ns compojure.persist)

(import '(java.io File FileWriter BufferedWriter))

(defn- file-appender
  "Create a new writer to append data to the end of a file."
  [filename]
  (new BufferedWriter
    (new FileWriter filename true)))

(defn save-exprs
  "Append a list of Clojure expressions to the end of a file."
  [filename bindings exprs]
  (send-off
    (agent (file-appender filename))
   #(with-open appender %
      (. appender
        (write (prn-str `(let ~bindings ~@exprs)))))))

(defmacro quote-binding-names
  "Evaluate the expressions in a binding vector."
  [bindings]
  (apply vector
    (mapcat
      (fn [[name expr]] [`(quote ~name) expr])
      (partition 2 bindings))))

(defn restore
  "Restore ref data from a file, if it exists."
  [filename]
  (if (. (new File filename) (exists))
    (dosync (load-file filename))))

(defmacro dosave
  "Operates in the same fashion as (dosync (let ... )), but saves the
  changes in the transaction to a file."
  [filename bindings & body]
  `(dosync
     (let ~bindings ~@body)
     (save-exprs ~filename (quote-binding-names ~bindings) '~body)))
