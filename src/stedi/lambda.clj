(ns stedi.lambda
  (:require [clojure.java.io :as io]))

(def ^:dynamic *entrypoint* nil)

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
  [name args body]
  `(defn ~name ~args
     ~@body))

(defmacro deflambda
  "Defines an AWS lambda function. Takes a request map (similar to ring)
  and can return a string or anything coerceable by
  `clojure.java.io/input-stream`"
  [name args & body]
  (let [entrypoint (str *ns* "/" name)]
    (if (= entrypoint *entrypoint*)
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
           ~(make-handler-function name args body)
           ~(make-lambda-entrypoint name)))
      (do
        (when-not *compile-files*
          (require 'stedi.lambda.registry)
          (let [add-lambda (requiring-resolve 'stedi.lambda.registry/add-lambda)]
            (add-lambda entrypoint)))
        (make-handler-function name args body)))))
