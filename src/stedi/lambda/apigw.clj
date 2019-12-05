(ns stedi.lambda.apigw
  "Tools for lambdas that are invoked via API Gateway.

  Contains functions to convert API Gateway events into ring requests, and ring
  responses into API Gateway responses."
  ;; NOTE: some of the initial code was copied from here (MIT License):
  ;;       https://github.com/mhjort/ring-apigw-lambda-proxy
  (:require [clojure.string :as string]
            [stedi.lambda.middleware :as middleware])
  (:import [java.io ByteArrayInputStream]
           [java.net URLEncoder]))

(defn- generate-query-string [params]
  (string/join "&" (map (fn [[k v]]
                          (str (URLEncoder/encode (name k)) "=" (URLEncoder/encode (str v))))
                        params)))

(defn- request->http-method [request]
  (-> (:httpMethod request)
      (string/lower-case)
      (keyword)))

(defn- keyword->lowercase-string [k]
  (string/lower-case (name k)))

(defn- map-keys [f m]
  (into {} (map (fn [[k v]] [(f k) v]) m)))

(defn apigw-request->ring-request [apigw-request]
  {:pre [(every? #(contains? apigw-request %) [:httpMethod :path :queryStringParameters])
         (contains? #{"GET" "POST" "OPTIONS" "DELETE" "PUT" "PATCH"} (:httpMethod apigw-request))]}
  {:uri            (:path apigw-request)
   :query-string   (generate-query-string (:queryStringParameters apigw-request))
   :request-method (request->http-method apigw-request)
   :headers        (map-keys keyword->lowercase-string (:headers apigw-request))
   :body           (when-let [body (:body apigw-request)]
                     (ByteArrayInputStream. (.getBytes body "UTF-8")))})

(defn wrap-apigw-lambda-proxy
  "Middleware wrap a ring handler function. The handler will receive a ring
  request map built from an API Gateway request. The ring request will contain
  the following additional keys:

    - :context: com.amazonaws.services.lambda.runtime.Context

  The response should be a standard ring response. It will be converted into an
  API Gateway response."
  [ring-handler]
  (-> (fn [{:keys [input context]}]
        (let [ring-request   (-> input
                                 apigw-request->ring-request
                                 (assoc :context context))
              ring-response  (ring-handler ring-request)
              apigw-response {:statusCode (:status ring-response)
                              :headers    (:headers ring-response)
                              :body       (:body ring-response)}]
          {:output apigw-response}))
      middleware/wrap-json-input
      middleware/wrap-json-output))
