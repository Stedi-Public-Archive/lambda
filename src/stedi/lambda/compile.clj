(ns stedi.lambda.compile
  "Wrapper to `clojure.core/compile` that dynamically binds
  `stedi.lambda/*compile-entrypoint*` such that only one lambda
  function gets compiled. See `stedi.lambda/defentrypoint` for more
  information."
  (:require [clojure.java.io :as io]
            [stedi.lambda :as lambda])
  (:refer-clojure :exclude [compile]))

(defn target-dir [entrypoint]
  (str "target/lambda/" entrypoint "/classes"))

(defn compile [entrypoint]
  (let [target (target-dir entrypoint)
        sym    (symbol entrypoint)]
    (io/make-parents (str target "/."))
    (binding [lambda/*compile-entrypoint* entrypoint
              *compile-path*              target]
      (clojure.core/compile (symbol (namespace sym))))))

(defn -main [entrypoint]
  (compile entrypoint))
