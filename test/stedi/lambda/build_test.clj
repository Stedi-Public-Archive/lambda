(ns stedi.lambda.build-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.test :refer [deftest testing is]]
            [stedi.lambda.build :as build])
  (:import [java.io ByteArrayOutputStream]))

(defn- clean-files!
  [dir]
  (doseq [file (->> (io/file dir)
                    (file-seq)
                    (filter #(.isFile %)))]
    (io/delete-file file)))

(deftest build-test
  (clean-files! "./example/target")
  (testing "example project builds successfully"
    (let [{:keys [exit out err]}
          (sh/with-sh-dir "./example"
            (sh/sh "clojure" "-m" "stedi.lambda.build"))]
      (when (not-empty out) (println out))
      (when (not-empty err) (println err))
      (is (= 0 exit))))

  (testing "correct jars are built"
    (is (.exists (io/file "./example/target/lambda/stedi.example/hello-lambda.jar"))))

  (testing "entrypoint is invokable"
    ;; This is gross but the classes we want to test aren't available
    ;; until this test runs so `import` will fail if we don't wait and
    ;; dynamically eval
    (let [output (eval `(do
                          (with-open [is# (io/input-stream (.getBytes "Hello world"))
                                      os# (ByteArrayOutputStream.)]
                            (import '[stedi.lambda ~'Entrypoint])
                            (stedi.lambda.Entrypoint/handler is# os# nil)
                            (read-string (str os#)))))]
      (is (= {:my-payload "Hello world"}
             output)))))
