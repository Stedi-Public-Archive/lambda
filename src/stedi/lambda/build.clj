(ns stedi.lambda.build
  "Analyzes project paths, compiles lambda functions classes and bundles
  classes with their dependencies."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [stedi.lambda.registry :as registry]
            [stedi.lambda.compile :as compile]))

(def ^:private build-deps
  '{:deps {pack/pack.alpha
           {:git/url "https://github.com/juxt/pack.alpha.git"
            :sha     "2769a6224bfb938e777906ea311b3daf7d2220f5"}}})

(defn- compile-args
  [entrypoint]
  ["clojure"
   "-m" "stedi.lambda.compile"
   entrypoint])

(defn- pack-args
  [classes target-zip]
  ["clojure"
   "-Sdeps" (pr-str build-deps)
   "-m" "mach.pack.alpha.aws-lambda"
   "-e" classes
   target-zip])

(defn- wrap-debug
  [f]
  (fn [input]
    (let [result (f input)]
      (when (System/getenv "DEBUG")
        (println "[debug]" (pr-str result)))
      result)))

(defn- wrap-describe
  [f]
  (fn [input]
    (apply println "[running]" (:args input))
    (let [{:keys [err out] :as result} (f input)]
      (when (or out err) (println))
      (when (not-empty out) (println out))
      (when (not-empty err) (println err))
      result)))

(defn- wrap-throw-on-nonzero
  [f]
  (fn [input]
    (let [result (f input)]
      (when-not (= 0 (:exit result))
        (throw (ex-info "Command failed"
                        {:input  input
                         :result result}))))))

(def ^:private shell
  (-> (fn [{:keys [args]}] (apply sh/sh args))
      (wrap-describe)
      (wrap-debug)
      (wrap-throw-on-nonzero)))

(defn- deps
  []
  (-> (slurp "deps.edn")
      (read-string)))

(defn- project-paths
  []
  (-> (deps)
      (:paths)))

(defn target-zip
  [entrypoint]
  (str "target/lambda/" entrypoint ".jar"))

(defn- bundle
  [entrypoint]
  (let [classes    (compile/target-dir entrypoint)
        target-zip (target-zip entrypoint)]
    (io/make-parents target-zip)
    (shell {:args (compile-args entrypoint)})
    (shell {:args (pack-args classes target-zip)})))

(defn- bundle-all
  []
  (let [paths (project-paths)]
    (doseq [entrypoint (registry/find-entrypoints paths)]
      (bundle entrypoint))))

(defn -main [& [entrypoint]]
  (when-not (get-in (deps) [:deps 'stedi/lambda])
    (throw (Exception. "Could not find stedi/lambda in :deps, did you forget to include it?")))
  (if entrypoint
    (bundle entrypoint)
    (bundle-all))
  (shutdown-agents))
