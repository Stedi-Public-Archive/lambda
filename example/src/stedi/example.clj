(ns stedi.example
  (:require [stedi.lambda :refer [defentrypoint]]))

(defn wrap-slurp-input
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  (fn [req]
    ;; :input of req is a java.io.InputStream
    (handler (update req :input slurp))))

(defn wrap-output
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  ;; :output can be a String or anything coercible
  ;; by `clojure.java.io/input-stream`
  (fn [req] {:output (handler req)}))

(defn hello [{:keys [input]}]
  (format "Hello, %s!" input))

(defentrypoint hello-lambda
  (-> hello
      wrap-slurp-input
      wrap-output))

(comment
  (require '[clojure.java.io :as io])

  ;; you can invoke the fn defined by defentrypoint directly

  (hello-lambda {:input (io/input-stream (.getBytes "you"))})
  ;; => {:output "Hello, you!"}
  )
