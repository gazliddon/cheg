(ns cheg.obj-spec
  (:require [speclj.core ]
            [cheg.obj ]))

; (describe "Server test"
;           (it "should always pass"
;               (should= 0 0 )))

; (def good-obj {:x 100
;                :y 100
;                :xv 20
;                :yv 12
;                :frame 0})

; (def bad-obj  {:xa 100
;                :ya 100
;                :xvx 20
;                :yvz 12})


; (defn random-obj [s] {:x (* 10 s)
;                       :y (* 1.1 s)
;                       :xv (/ s 0.8)
;                       :yv (+ s 0.1)})

; (def random-objs (take 100 (map random-obj (range))))

; (describe "Can I tell if something is an obj"
;           (it "should tell me the good-obj record is an obj"
;               (should-be is-obj? good-obj )))

; (describe "Can I tell if something isn't an obj"
;           (it "should tell me the bad-obj record isn't an obj"
;               (should-not-be is-obj? bad-obj )))

; (describe "Does pos detection work?"
;           (it "should return true for the good-obj"
;               (should= (has-pos? good-obj) true)))

; (describe "Creating a new obj by adding velocites"
;           (it "should add the vels to pos"
;               (let [new-obj (obj-add-vels good-obj)]
;                 (should= (:x new-obj) 120)
;                 (should= (:y new-obj) 112))))

; (describe "Updating all of the objects"
;           (it "Should make a load of update objects from old objects"
;               (let [new-objs (update-objs good-obj random-objs)]
;                 (should-be #(every? is-obj? %) new-objs)
;                 (should-be vector? new-objs))))

