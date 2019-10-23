# stedi/lambda

A Clojure CLI build tool to generate deployable [AWS Lambda][1] archives for
Clojure projects.

## Installation

This is only available through git deps right now:

``` clojure
{:deps {stedi/lambda {:git/url "https://github.com/stediinc/lambda"
                      :sha     "<insert sha>"}}}
```

## Getting Started

`stedi/lambda` expects Lambda handlers to be declared with the
`stedi.lambda/defentrypoint` macro. Using this macro will register a
Lambda handler so the build tool knows what to compile.

**Examples**

``` clojure
(ns stedi.example
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [stedi.lambda :refer [defentrypoint]]))

(defentrypoint hello
  (fn [{:keys [input]}]
    ;; :input is a java.io.InputStream
    (let [who (slurp input)]
      ;; :output can be a String or anything coercible
      ;; by clojure.java.io/input-stream
      {:output (format "Hello, %s!" who)})))

(comment
  ;; You can invoke the fn defined by defentrypoint directly:
  (hello {:input (io/input-stream (.getBytes "you"))})
  ;; => {:output "Hello, you!"}
  )

;; You can wrap your functions in middleware. We recommend
;; that all middleware functions take a map with `:input`
;; and return a map with `:output`, and are limited to modifying
;; input and output

(defn wrap-json-input
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  (fn [req]
    ;; :input of req is a java.io.InputStream
    (handler (-> req
                 (update :input io/reader)
                 (update :input #(json/read % :key-fn keyword))))))

(defn wrap-json-output
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  ;; :output can be a String or anything coercible
  ;; by `clojure.java.io/input-stream`
  (fn [req] (-> (handler req)
                (update :output json/write-str))))

;; The handler fn still expects a map with :input and returns a map
;; with :output, but :input has been modified by the wrap-json-input
;; middleware function, and :output will be modified by the
;; wrap-json-output function.
(defn hello-handler [{:keys [input]}]
  {:output {:greeting (format "Hello again, %s!" (:who input))}})

(defentrypoint hello-again
  (-> hello-handler
      wrap-json-input
      wrap-json-output))

(comment
  ;; You can invoke the fn defined by defentrypoint directly:
  (hello-again {:input (io/input-stream (.getBytes (json/json-str {:who "you"})))})
  ;; => {:output "{\"greeting\":\"Hello again, you!\"}"}
  )
```

**Building**

```bash
clj -m stedi.lambda.build
```

This will generate jar files in the `target` directory that can be
uploaded to AWS Lambda.

**Configuration**

In the AWS Lambda console, set the
`Handler` to `stedi.lambda.Entrypoint::handler`.

## Philosophy

`stedi/lambda` keeps dependencies to a minimum and provides a
straightforward way to build all Lambdas for a project as seperate
archives (one per function). It structures the input to the Lambda as
a [Ring][2]-like map with the intention that a rich ecosystem of
middlewares can be built around it.

[1]: https://aws.amazon.com/lambda/
[2]: https://github.com/ring-clojure/ring
