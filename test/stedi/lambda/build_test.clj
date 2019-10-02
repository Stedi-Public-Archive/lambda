(ns stedi.lambda.build-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest testing is]]
            [stedi.lambda.build :as build])
  (:import [java.io ByteArrayOutputStream]))

(deftest build-test
  (testing "correct jar is built"
    (is (.exists (io/file "./example/target/lambda/stedi.example/hello-lambda.jar"))))

  (testing "entrypoint is invokable"
    ;; This is gross but the classes we want to test aren't available
    ;; until this test runs so `import` will fail if we don't wait and dynamically eval
    (let [output (eval `(do
                          (with-open [is# (io/input-stream (.getBytes "Hello world"))
                                      os# (ByteArrayOutputStream.)]
                            (import '[stedi.lambda ~'Entrypoint])
                            (stedi.lambda.Entrypoint/handler is# os# nil)
                            (read-string (str os#)))))]
      (is (= {:my-payload "Hello world"}
             output)))))
