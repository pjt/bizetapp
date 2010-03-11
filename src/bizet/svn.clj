(ns bizet.svn
  (:use
     [clojure.contrib.shell-out :only (sh)]
     [clojure.contrib.str-utils :only (str-join)]
     [xml-service.paths :only (basename dirname dirname-seq reduce-path-by)]
     [xml-service.utilities :only (as-abs-path throw-fmt uuid)]))

(defn make-co-path-root
  [session-id]
  (str-join "/" 
        ["/tmp/webedit-tmp" session-id (uuid)]))

(defn co-path-session-root
  [co-path]
  (nth (reverse (dirname-seq (as-abs-path co-path))) 3))

(defn co-path-instance-root
  [co-path]
  (nth (reverse (dirname-seq (as-abs-path co-path))) 4))

(defn- svn-base
  "Runs svn cmd, returns sh's :return-map. Supply username, password, cmd,
  & any args."
  [user pword cmd & args]
  (apply sh :return-map true
      "svn" cmd
      "--no-auth-cache" "--non-interactive"
      (concat 
        (when user ["--username" user "--password" pword])
        args)))

(defmacro with-svn-cmd
  "Calls svn command ([user pword cmd & args]); if succeeds, evalutes first 
  form; if fails, evaluates second. The symbols 'exit, 'out, & 'err are bound 
  to results of call."
  [[user pword cmd & args] succeed fail]
  `(let [{:keys [~'exit ~'out ~'err]} 
          (svn-base ~user ~pword ~cmd ~@args)]
     (if (zero? ~'exit)
       ~succeed
       ~fail)))

(defn svn-auth
  "Tests authentication (by way of `svn ls`) against a repos."
  [url user pword]
  (with-svn-cmd [user pword "ls" url]
    true
    false))
    ;(format "Username & password invalid at %s" url)))

(defn svn-co
  "Checks out path located at URL to directory, returns directory name if 
  successful, raises error otherwise."
  [url repos-path co-root user pword]
  (let [full-url (str url (dirname repos-path))
        co-path  (str co-root (if (.endsWith co-root "/") "" "/") repos-path)]
    (with-svn-cmd [user pword "checkout" full-url (dirname co-path)]
      (java.io.File. co-path)
      (throw-fmt "Unable to checkout %s: %s" full-url err))))

(defn svn-diff
  "Returns diff on SVN checkout, raises error on problem."
  [file & [user pword]]
  (let [file (as-abs-path file)]
    (with-svn-cmd [user pword "diff" file]
      out
      (throw-fmt "Unable to diff %s: %s" (basename file) err))))

(defn svn-up
  "Updates working copy at directory."
  [dir & [user pword]]
  (let [dir (as-abs-path dir)]
    (with-svn-cmd [user pword "up" dir]
      true
      (throw-fmt "Unable to update %s: %s" (basename dir) err))))

(defn svn-ci
  "Commits a change, or raises error."
  [file msg user pword]
  (let [file (as-abs-path file)]
    (with-svn-cmd [user pword "ci" file "-m" msg]
      out
      (throw-fmt "Unable to checkin %s: %s" (basename file) err))))

