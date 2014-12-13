(ns cheg.state
  (:require 
    [cheg.gfx :as gfx]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :refer [put! <! >! chan]] ))

(def -player
  { :x 100 :y 100 :xv 0 :yv 0 :col "green"}
  
  )
(defonce app-state
  (atom {
             :messages (chan)

             :mouse-pos [0 0]

             :updating false

             :game-state {:messages (chan)
                          :paused false
                          :player -player
                          :objs [] 
                          :time 0
                          }

             :title "cheg"
             }))

(defn send-game-message [m]
  (let [ch (get-in @app-state [:game-state :messages])]
    (put! ch m)
    ))

(defn -mk-toggle-fn [state addr]
  (fn []
    (swap! state assoc-in addr (not (get-in @state addr)))))

(def toggle-pause-state
  (-mk-toggle-fn app-state [:game-state :paused]))

(defn append-item! [addr o]
  (let [old-coll (get-in @app-state addr )
        new-coll (into old-coll [o])]
    (swap! app-state assoc-in addr new-coll)))

(defn add-obj! [o]
  (append-item! [:game-state :objs] o))

(defn mkspr [x y]
  {
   :start-time (get-in @app-state [:game-state :time])
   :x x
   :y y
   :xv 0
   :yv 0
   :imgs (gfx/get-rand-anim)
   :col "blue"
   })

(defn make-random-spr []
  (mkspr (rand 100) (rand 100)))

(defn make-random-jumpy []
  (make-random-spr))

(defn add-random-jumpy! []
  (add-obj! (make-random-jumpy) ))
