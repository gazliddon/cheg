(ns cheg.state
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
    [cheg.gfx :as gfx]
    [cheg.obj :as obj]
    [cheg.vec :as vec]
    [cheg.player :as player]
    [cheg.statemachine :as sm]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :refer [put! <! >! chan]] ))

(declare app-state)

(def -player
  { :x 100 :y 100 :xv 0 :yv 0 :col "green"}
  
  )

(defn send-game-message! [m & args]
  (do
    (let [ch (get-in @app-state [:game-state :messages])]
      (println (str  "Sending game msg" m args))
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


(defn spr-default-behaviour [o t]
  o)

(defn spr-default-render [r {:keys [x y spr-handle] :as o} time-now]
  (let [id (obj/obj-get-frame o time-now)
        offset (gfx/get-img-offset id spr-handle)
        [nx ny] (vec/add [x y] offset) ]
      (obj/static-img r nx ny id)
    )
  )

(def spr-defaults {:start-time 0
                   :x 0 :y 0 :vx 0 :yv 0
                   :imgs [:flap-f1]
                   :behaviour spr-default-render
                   :render spr-default-render
                   :spr-handle :top-left
                   })

(defn mkspr [i]
  (let [vals (merge spr-defaults i)]
   vals))

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
      (mkspr
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
  (atom {    :messages (chan)
             :mouse-pos [0 0]
             :updating false

             :stats {:num-objs 0
                     :px 10
                     :py 10
                     }


             :game-state {:messages (chan)
                          :paused false
                          :objs [] 
                          :game-time 0
                          :player (mkspr {:state :nothing
                                          :x 100
                                          :y 100}) 
                          }

             :title "cheg"
             }))


(defn get-player [] (get-in @app-state [:game-state :player]))
(defn set-player! [[player]] (swap! app-state assoc-in [:game-state :player] player))
(defn get-time [] (get-in @app-state [:game-state :game-time]))

(defn handle-game-event [ & ev-and-args ]

  (let [time-now (get-time)
        player (get-player)
        new-player (sm/process-events player/fsm-table player/on-event time-now player [ ev-and-args ])]
    (swap! app-state assoc-in [:game-state :player] new-player)))

