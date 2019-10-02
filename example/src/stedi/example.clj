(ns stedi.example
  (:require [stedi.lambda :refer [defentrypoint]]))

(defn wrap-slurp
  "Example middleware to show off middleware pattern with lambdas."
  [handler]
  (fn [{:keys [input-stream] :as req}]
    (let [resp (handler (-> req
                            (assoc :payload (slurp input-stream))))]
      {:output (pr-str resp)})))

(defn hello [{:keys [payload]}]
  {:my-payload payload})

(defentrypoint hello-lambda
  (-> hello
      (wrap-slurp)))
