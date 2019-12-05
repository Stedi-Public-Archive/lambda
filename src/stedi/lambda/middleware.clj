(ns stedi.lambda.middleware
  "Some useful lambda middlewares."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn wrap-json-input
  "Parse the input as JSON. Downstream handlers with receive a map where the value
  of `:input` is a clojure map with keyword keys."
  [handler]
  (fn [req]
    ;; :input of req is a java.io.InputStream
    (handler (-> req
                 (update :input io/reader)
                 (update :input #(json/read % :key-fn keyword))))))

(defn wrap-json-output
  "Wrap a handler that will return a value that is convertible to JSON. This
  middleware will write the JSON to the lambda's response stream."
  [handler]
  ;; :output can be a String or anything coercible
  ;; by `clojure.java.io/input-stream`
  (fn [req]
    (-> (handler req)
        (update :output json/write-str))))
