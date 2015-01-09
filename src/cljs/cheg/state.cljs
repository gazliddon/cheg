(ns cheg.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])

  (:require 
    [cljs.core.async :refer [put! <! >! chan]]
    [cheg.gfx :as gfx]
    [cheg.obj :as obj]
    [cheg.spr :as spr]
    [cheg.vec :as vec]
    [cheg.player :as player]
    [cheg.statemachine :as sm]
    [om.dom :as dom :include-macros true] ))

(declare app-state)

(defn send-game-message! [m & args]
  (do
    (println args)
    (let [ch (get-in @app-state [:game-state :messages])]
      (put! ch [m args]))))

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


(defn get-game-time []
  (get-in @app-state [:game-state :game-time]))


(defn homefn [init-pos amplitudes [phx phy] [vx vy] t]
  (let [p1 [(Math/cos (+ phx (* t vx))) (Math/sin (+ phy (* t vy)))]
        p2 (vec/mul amplitudes p1)
        p3 (vec/add init-pos p2)]
    p3))

(defn add-random-jumpy! []
  (let [[x y] (vec/add [ (rand 400) (rand 100)] [400 400]) 
        amp [(rand 300) (rand 300)]
        pha [(rand 7) (rand 7)]
        vel [(rand 3) (rand 3)]
        ]
    (add-obj!
      (spr/mkspr
        {:start-time (get-game-time)
         :behaviour   (fn [o game-time]
                        (let [otime (- game-time (:start-time o))
                              [x y] (homefn [x y] amp pha vel game-time)]
                          (assoc o :x x :y y)))
         :spr-handle :top-left
         :x           x
         :y           y }))))

(defn add-static-img! [x y img]
  (add-obj!
    (mkspr
      {:x    x
       :y    y
       :spr-handle :centered
       :imgs [img]  })))

(defonce app-state
  (atom {:messages (chan)

         :mouse-pos [0 0]

         :game-state {:messages (chan)
                      :paused false
                      :objs [] 
                      :game-time 0
                      :player (spr/mkspr
                                {:state :nothing
                                 :x 100
                                 :y 100}) 

                      :ptest {:yacc 1000
                              :xacc 1000 }

                      }

         :title "cheg" }
        ))

(defn get-player [] (get-in @app-state [:game-state :player]))
(defn set-player! [[player]] (swap! app-state assoc-in [:game-state :player] player))
(defn get-time [] (get-in @app-state [:game-state :game-time]))

(defn handle-game-event [ & ev-and-args ]
  (let [time-now (get-time)
        player (get-player)
        new-player (sm/process-events player/fsm-table player/on-event time-now player [ ev-and-args ])]
    (swap! app-state assoc-in [:game-state :player] new-player)))

