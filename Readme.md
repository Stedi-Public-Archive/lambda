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

(defn wrap-slurp
  "Example middleware to show off middleware pattern with Lambdas."
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
uploaded to AWS Lambda.

## Philosophy

`stedi/lambda` keeps dependencies to a minimum and provides a
straightforward way to build all Lambdas for a project as seperate
archives (one per function). It structures the input to the Lambda as
a [Ring][2]-like map with the intention that a rich ecosystem of
middlewares can be built around it.

[1]: https://aws.amazon.com/lambda/
[2]: https://github.com/ring-clojure/ring
