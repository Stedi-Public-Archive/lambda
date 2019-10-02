(ns stedi.lambda
  (:require [clojure.java.io :as io]))

(def ^{:doc     "The name of the current endpoint being compiled by `stedi.lambda.compile`"
       :dynamic true}
  *compile-entrypoint* nil)

(defn- make-lambda-entrypoint
  [handler-name]
  `(defn ~'-handler [is# os# context#]
     (let [output# (-> (~handler-name {:input-stream is#
                                       :context      context#})
                       (:output))
           stream# (io/input-stream
                     (if (string? output#)
                       (.getBytes output#)
                       output#))]
       (io/copy stream# os#))))

(defn- make-handler-function
  [name body]
  `(def ~name ~@body))

(defmacro defentrypoint
  "Defines a lambda entrypoint, its value should resolve to a function
  that takes a lambda request map and returns a lambda response map.

  Request Map:
    :input-stream - an input stream of the payload
    :context      - an instance of `com.amazonaws.services.lambda.runtime.Context`

  Response Map:
    :output - a String or anything coercable by `clojure.java.io/input-stream`
              to be returned as the response"
  [name & body]
  (let [entrypoint (str *ns* "/" name)]
    (if (= entrypoint *compile-entrypoint*)
      (do
        (println "[compiling]" entrypoint)
        `(do
           (gen-class
             :name stedi.lambda.Entrypoint
             :methods [^:static [~'handler
                                 [java.io.InputStream
                                  java.io.OutputStream
                                  com.amazonaws.services.lambda.runtime.Context]
                                 ~'void]])
           ~(make-handler-function name body)
           ~(make-lambda-entrypoint name)))
      (do
        (when-not *compile-files*
          (require 'stedi.lambda.registry)
          (let [add-lambda (requiring-resolve 'stedi.lambda.registry/add-entrypoint)]
            (add-lambda entrypoint)))
        (make-handler-function name body)))))
