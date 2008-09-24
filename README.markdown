Compojure is an open source web framework for the Clojure programming language,
designed to produce concise and functional code without any messing around with
mammoth configuration files and byzantine libraries of interdependent classes.
It's only requirement is a working Java VM, as it comes bundled with jar files
for Clojure and the Java HTTP server, Jetty.

Compojure is still in active development, but a lot of the API is now
relatively stable.

Quickstart
==========

1. Grab Compojure from github:

        $ git clone git://github.com/weavejester/compojure.git

2. Run Compojure:

        $ script/run

3. An example "Hello World" application should be up and running at:
http://localhost:8080/


File Structure
==============

By default, Compojure is organised with the file structure listed below. But
there's nothing stopping you taking the Compojure libraries and using them in
any fashion you want.

    +- app             - Your main application code
    |
    +- boot
    |  +- boot.clj     - The script that initializes your application
    |
    +- jars            - The jar files used by the application
    |
    +- lib
    |  +- compojure    - The Compojure libraries
    |
    +- public          - Static files that are served if no route is found
    |
    +- script
       +- repl         - Starts an interactive REPL
       +- run          - Runs non-interactively


Core Libraries
==============

Compojure provides several core libraries that provide the bulk of its
functionality.

HTTP
----

The HTTP module provides Compojure with a RESTful and functional way to define
Java servlets. It's syntax was heavily inspired by the Ruby web framework,
Sinatra.

To create a servlet, you pass a series of HTTP resource definitions to the
`servlet` function:

    (def #^{:doc "A simple greeter"} greet
      (servlet
        (GET "/greet" "Hello visitor!")
        (ANY "/*"     (page-not-found))))

Compojure also provides a `defservlet` macro, that works as you might expect:

    (defservlet greet
      "A simple greeter"
      (GET "/greet" "Hello visitor!")
      (ANY "/*"     (page-not-found)))

The resource definitions passed to `defservlet` are Compojure's way of
associating a URL route with code that produces a useful HTTP response, such as
a web page or image.

Resource definitions take the form:

    (method route & body)

The method can be any one of the standard HTTP methods:

    GET  POST  PUT  DELETE  HEAD

Or, if you wish to match any HTTP method, you can use

    ANY

The route can be a fixed string, like "/greet", but often you're going to want
to assign certain parts of the route to parameters that affect the output:

    (GET "/greet/:name"
      (str "Hello " (route :name)))

Here, the resource definition assigns the path after "/greet" to the parameter
`:name`. Parameters from routes can be accessed via the `route` function.

Along with `route`, there are several other bindings available by default in
all resource declarations:

  * method          - the HTTP method
  * full-path       - the full path of the request 
  * (param name)    - a HTTP parameter
  * (header name)   - a HTTP header
  * (route name)    - a named part of the request path
  * session         - a ref to a session-specific map
  * (mime filename) - guesses the mimetype of a filename
  * request         - the HttpServletRequest object
  * context         - the HttpServletContext object
  * response        - the HttServletResponse object

For example:

    (GET "/form"
      (str "<p>Your current name is: " (@session :name) "</p>"
           "<form>Change name: <input name='name' type='text'>"
           "<input type='submit' value='Save'></form>"))

    (POST "/form"
      (dosync
        (alter session assoc :name (param :name))
        (str "Your name was changed to " (@session :name))))


It is possible to modify the response through the response object, but this is
almost never necessary. Instead, Compojure takes a functional approach,
constructing the HTTP response from the return value of the resource.

In the previous examples, you can see how returning a string adds to the
response body. Other standard Clojure types modify the response in different
ways:

 * java.lang.String  - adds to the response body
 * java.lang.Number  - changes the status code
 * Clojure hash map  - alters the HTTP headers
 * Clojure seq       - lazily adds to the response body
 * java.io.File      - streams the file to the response body

These modifications can be chained together using a standard Clojure vector:

    (GET "/text"
      [{"Content-Type" "text/plain"}
       "This is plain text."
       "And some more text."])

    (GET "/bad"
      [404 "<h1>This page does not exist!</h1>"])

    (GET "/download"
      (file "public/compojure.tar.gz"))   ; 'file' is an alias to 'new java.io.File'


HTML
----

The HTML module provides a way of defining HTML or XML through a tree of
vectors.

    (html [:p [:em "Hello World"]])

    => <p>
         <em>Hello World</em>
       </p>

The tag is taken from the first item of the vector, and can be a string,
symbol or keyword. You can optionally specify attributes for the tag by
providing a hash map as the secord item of the vector:

    (html [:div {:class "footer"} "Page 1"])

    => <div class="footer">
         Page 1
       </div>

Any sequences will be expanded out into the containing vector:

    (html [:em '("foo" "bar")])

    => <em>foobar</em>

    (html [:ul
      (map (fn [x] [:li x])
           [1 2 3])])

    => <ul>
         <li>1</li>
         <li>2</li>
         <li>3</li>
       </ul>

The `html` function not only renders valid HTML, it also formats it as best it
can in a human readable format. Block elements like `<p>` and `<div>` are
indented, whilst span elements like `<em>` are rendered inline.

Conversely, the `xml` function has no special formatting.
