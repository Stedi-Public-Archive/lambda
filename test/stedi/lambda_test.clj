(ns stedi.lambda-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [stedi.lambda :as lambda :refer [defentrypoint]]
            [stedi.lambda.apigw :as apigw])
  (:import com.amazonaws.services.lambda.runtime.Context
           java.io.ByteArrayOutputStream))

(defn my-ring-handler [request]
  {:status  200
   :headers {"x-whatever" "abc123"}
   :body    (json/write-str {:id 123 :value "stuff"})})

(defentrypoint ring-handler
  (-> my-ring-handler
      apigw/wrap-apigw-lambda-proxy))

(deftest test-ring-lambda
  (let [apigw-request {:httpMethod            "GET"
                       :path                  "/foo/bar"
                       :queryStringParameters {:x 42 :y "blah"}}
        istream       (io/input-stream (.getBytes (json/write-str apigw-request)))
        context       nil
        response      (ring-handler {:input istream :context context})]
    (let [response (json/read-str (:output response) :key-fn keyword)
          body     (json/read-str (:body response) :key-fn keyword)]
      (is (= "stuff" (:value body)))
      (is (= 123 (:id body))))))
