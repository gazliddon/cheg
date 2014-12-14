(ns cheg.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
    [cheg.gfx :as gfx]
    [cheg.obj :as obj]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :refer [put! <! >! chan]] ))

(def -player
  { :x 100 :y 100 :xv 0 :yv 0 :col "green"}
  
  )

(defonce app-state
  (atom {    :messages (chan)
             :mouse-pos [0 0]
             :updating false

             :stats {:num-objs 0
                     :px 10
                     :py 10
                     }

             :game-state {:messages (chan)
                          :paused false
                          :player -player
                          :objs [] 
                          :game-time 0
                          }

             :title "cheg"
             }))

(defn send-game-message [m v]
  (let [ch (get-in @app-state [:game-state :messages])]
    (put! ch [m v])))

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

(def spr-defaults {:start-time 0
                   :x 0 :y 0 :vx 0 :yv 0
                   :imgs [:flap-f1]
                   :behaviour (fn [o] o)
                   :spr-handle :top-left
                   :render-xform      (fn [o game-time] o)
                   })

(defn mkspr [i]
  (let [vals (merge spr-defaults i)]
   vals))

(defn get-game-time []
  (get-in @app-state [:gamestate :game-time]))

(defn add-random-jumpy! []
  (add-obj!
    (mkspr
      {:start-time (get-game-time)
       :behaviour   (fn [o game-time] (obj/obj-home-on-pos 100 100 o))
       :spr-handle :top-left
       :x           (rand 100)
       :y           (rand 100) })))

(defn add-static-img! [x y img]
  (add-obj!
    (mkspr
      {:x    x
       :y    y
       :spr-handle :centered
       :imgs [img]  })))





