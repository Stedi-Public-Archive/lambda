(ns stedi.lambda.registry
  "A registry to track loaded lambda entrypoint functions.")

(defonce ^:private ^:dynamic *registry* (atom #{}))

(defn registry
  "Retrieves the current value of the registry."
  []
  @*registry*)

(defn clear!
  "Clears all entries from the registry."
  []
  (reset! *registry* #{}))

(defn add-lambda
  "Adds lambda entrypoint to registry."
  [entrypoint]
  (swap! *registry* conj entrypoint))
