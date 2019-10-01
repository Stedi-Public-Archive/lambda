(ns stedi.lambda.analyze
  "Analyzes paths provided for `deflambda` invocations."
  (:require [clojure.java.io :as io]
            [stedi.lambda.registry :as registry]))

(defn analyze
  [paths]
  (registry/clear!)
  (let [files (->> paths
                   (mapcat (comp file-seq io/file))
                   (map str)
                   (filter #(.endsWith % ".clj")))]
    (doseq [file files]
      (println "[analyze] Checking for lambdas in" file)
      (load-file file))
    (let [found (registry/registry)]
      (println "[analyze] Found entrypoints" (pr-str found))
      found)))
