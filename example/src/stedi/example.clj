(ns stedi.example
  (:require [stedi.lambda :refer [defentrypoint]]))

(defn wrap-slurp
  "Example middleware to show off middleware pattern with lambdas."
  [handler]
  (fn [{:keys [input] :as req}]
    (let [resp (handler (-> req
                            (assoc :input (slurp input))))]
      {:output (pr-str resp)})))

(defn hello [{:keys [input]}]
  {:my-payload input})

(defentrypoint hello-lambda
  (-> hello
      (wrap-slurp)))
