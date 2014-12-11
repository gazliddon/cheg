(ns tester2.server-spec
  (:require [speclj.core :refer :all]
            [tester2.core :refer :all]
            [tester2.server :refer :all]))


(describe "Web Tests"
          (it "It fails FIXME"
              (should= 2 2)))

(describe "gaz clx test"
          (it "Should test my clkx file!"
              (should= 1 ( foo-cljx-test2 1) )))
