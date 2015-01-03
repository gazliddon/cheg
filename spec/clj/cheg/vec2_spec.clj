(ns cheg.vec2-spec
  (:require [speclj.core :refer :all]
            [cheg.vec2 :refer :all]
            [cheg.vec2overloads :refer :all]))

(def v1 (->Vec2 10 1))
(def v2 (->Vec2 2 3))

(describe "Testing Vector Additions"
          (it "should add two vectors nicely"
              (should (= (->Vec2 12 4) (!+ v1 v2))))
          
         (it "should add a vector to scalar int"
              (should (= (->Vec2 11 2) (!+ v1 1)))) 

          (it "should add a vector to scalar float"
              (should (= (->Vec2 11.11 2.11) (!+ v1 1.11)))))

(describe "Testing Vector Muliplications"
          (it "should multiply two vectors"
              (should (= (->Vec2 20 3) (!* v1 v2))))
          
         (it "should multiplay a vector by a scalar in"
              (should (= (->Vec2 20 2) (!* v1 2)))) 

          (it "should multiply a vector by a floating point number"
              (should (= (->Vec2 15 1.5) (!* v1 1.5)))))
