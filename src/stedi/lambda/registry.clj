(ns stedi.lambda.registry
  "A registry to track loaded lambda entrypoints."
  (:require [clojure.java.io :as io]))

(def ^:private ^:dynamic *registry* nil)

(defn add-entrypoint
  "Adds lambda entrypoint to registry."
  [entrypoint]
  (when *registry*
    (swap! *registry* conj entrypoint)))

(defn find-entrypoints
  [paths]
  (binding [*registry* (atom #{})]
    (let [files (->> paths
                     (mapcat (comp file-seq io/file))
                     (map str)
                     (filter #(.endsWith % ".clj")))]
      (doseq [file files]
        (println "[analyze] Checking for lambdas in" file)
        (load-file file))
      (let [found @*registry*]
        (println "[analyze] Found entrypoints" (pr-str found))
        found))))
