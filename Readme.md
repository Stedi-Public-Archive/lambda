# stedi/lambda

A Clojure CLI build tool to generate deployable lambda archives for
Clojure projects.

## Installation

This is only available through git deps right now:

``` clojure
{:deps {stedi/lambda {:git/url "https://github.com/stediinc/lambda"
                      :sha     "<insert sha>"}}}
```

## Getting Started

`stedi/lambda` expects lambda handlers to be declared with the
`stedi.lambda/defentrypoint` macro. Using this macro will register a
lambda handler so the build tool knows what to compile.

**Example**

``` clojure
(ns stedi.example
  (:require [stedi.lambda :refer [defentrypoint]]))

(defn wrap-slurp
  "Example middleware to show off middleware pattern with lambdas."
  [handler]
  (fn [{:keys [input-stream] :as req}]
    (let [resp (handler (-> req
                            (assoc :payload (slurp input-stream))))]
      {:output (pr-str resp)})))

(defn hello [{:keys [payload]}]
  {:my-payload payload})

(defentrypoint hello-lambda
  (-> hello
      (wrap-slurp)))
```

**Building**

```bash
clj -m stedi.lambda.build
```

This will generate jar files in the `target` directory that can be
uploaded to AWS lambda.

## Philosophy

`stedi/lambda` keeps dependencies to a minimum and provides a
straightforward way to build all lambdas for a project as seperate
archives (one per function). It structures the input to the lambda as
a ring-like map with the intention that a rich ecosystem of
middlewares can be built around it.
