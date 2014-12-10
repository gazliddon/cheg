(ns tester.server-spec
  (:require [speclj.core :refer :all]
            [tester.server :refer :all]))

(describe "Server test"
          (it "should always pas"
              (should= 0 0 )))
