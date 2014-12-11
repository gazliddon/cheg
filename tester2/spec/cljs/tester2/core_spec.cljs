(ns tester2.core-spec
  (:require-macros [speclj.core :refer [describe it should= run-specs]]
                   [clojure.core.async :refer [go]])
  (:require [speclj.core :as spec]
            [tester2.core]
            [om.dom :as dom :include-macros true]))

(describe "A ClojureScript test"
          (it "fail. Fix it!"
              (should= 0 1)))
