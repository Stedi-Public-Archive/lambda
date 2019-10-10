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

**Example**

``` clojure
(ns stedi.example
  (:require [stedi.lambda :refer [defentrypoint]]))

(defn wrap-slurp-input
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  (fn [req]
    ;; :input of req is a java.io.InputStream
    (handler (update req :input slurp))))

(defn wrap-output
  "Example middleware to demonstrate middleware pattern with lambdas. "
  [handler]
  ;; :output can be a String or anything coercible
  ;; by `clojure.java.io/input-stream`
  (fn [req] {:output (handler req)}))

(defn hello [{:keys [input]}]
  (format "Hello, %s!" input))

(defentrypoint hello-lambda
  (-> hello
      wrap-slurp-input
      wrap-output))

(comment
  (require '[clojure.java.io :as io])

  ;; you can invoke the fn defined by defentrypoint directly

  (hello-lambda {:input (io/input-stream (.getBytes "you"))})
  ;; => {:output "Hello, you!"}
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
