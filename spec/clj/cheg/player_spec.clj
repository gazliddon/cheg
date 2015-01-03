(ns cheg.player-spec
  (:require [speclj.core :refer :all]
            [cheg.player :refer :all]))

(describe "Check linear movement works fine"
          (it "should be in the right place after 2 seconds"
              (let [pos-fn (:linear pos-getters)
                    args {:pos [0 0]
                          :max-vel [3 -1]
                          :start-time 1}
                    ret (pos-fn args 3)]

                (should= {:pos [6 -2]
                          :vel [3 -1]} ret))) )

(describe "Check accelleration works fine"
          (let [dt 5]
           (it (str "should be in the right place after " dt " seconds")
              (let [acceleration [0 -1]
                    max-vel [4 5]
                    args {:pos [0 0]
                          :vel [0 30]
                          :start-time 0 
                          :max-vel max-vel
                          :acceleration acceleration}
                    ret ((:accelerate pos-getters) args dt)]

                (should= {:pos [1 1]
                          :vel [3 -1]} ret))) 
            ))


(accelerate player-record 0)



