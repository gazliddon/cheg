(ns cheg.player
  (:require
    [cheg.spr :as spr]
    [cheg.statemachine :as SM]
    [cheg.vec :as v]))

(defn get-renderable [{:keys [start-time pos vel]} time-now]
  (let [obj-time (- time-now start-time)  
        [x y] (v/add pos (v/mul-s vel obj-time))
        renderable (spr/mkspr {:x x
               :y y
               :imgs [:run-f1]
               :spr-handle :bottom-middle })
        ]
    renderable))

(defn player-renderable [{:keys [renderable] :as o} time-now]
  (if renderable
    (renderable o time-now )
    {:x 0 :y 0}))

(defn v2-half-at-sq
  "1/2at^2 for element vectors"
  [acc t]
  (v/mul  (v/mul t t) (v/mul-s acc 0.5)))

(defn accelerate [{:keys [pos vel acceleration start-time max-vel] } time-now]
  (let [t (- time-now start-time)
        tv [t t ]
        ut (v/mul vel tv)
        time-accelerating (v/div acceleration (v/sub max-vel vel)) 
        time-accelerating [1000 1000]
        acc-t (v/min tv time-accelerating)
        vel-t (v/max [0 0] (v/sub tv time-accelerating))
        acc-p (v2-half-at-sq acceleration acc-t)
        vel-p (v/mul max-vel vel-t)
        ]

    {:pos (v/add ut (v/add pos (v/add acc-p vel-p)))
     :vel (v/min max-vel (v/mul-s acceleration t))
     :vel-t vel-t
     :acc-t acc-t
     :acc-p acc-p
     :vel-p vel-p 
     :ut ut
     }
    ))

(def pos-getters
  {:null-pos ( fn [_ _] {:pos [100 100]
                         :vel [0 0]} )
   
   :stationary (fn [{:keys [pos]} _]
                 {:pos pos
                  :vel [0 0] })

   :linear (fn [{:keys [pos start-time max-vel]} time-now]
             (let [delta-t (- time-now start-time)
                   delta-p (v/mul-s max-vel delta-t) ]
               {:pos (v/add pos delta-p)
                :vel max-vel }))

   :accelerate accelerate })

(defn get-pos [o time-now]
  (let [pos-getter-id (get o :render-id :null)
        pos-getter (pos-getter-id pos-getters) ]
    (pos-getter o time-now)))

(defn move-func [o time-now & assocs]
  (let [assoc-fn (fn [o & vals] (apply assoc o vals))]
    (-> o (assoc :pos (get-pos o time-now)
                 :start-time time-now)
       (assoc-fn assocs))))

(defn move-accelerate [o time-now max-vel time-to-get-there]
  (move-func
    o time-now
    :render-id :accelerate
    :max-vel max-vel
    :time-to-get-there time-to-get-there))

(defn move-linear [o time-now max-vel]
  (move-func
    o time-now
    :render-id :linear
    :max-vel max-vel))

(defn move-stationary [o time-now pos]
  (move-func
    o time-now
    :render-id :stationary  
    :pos pos))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FSM for the player
;; an fsm table
;; a bunch of routines to call on state change
;; and a table tieing the routines to specific states 

(def fsm-table 
  {:create           {:nothing :creating}

   :done            {:creating :standing
                     :lose-life :standing}

   :enemy-collision {:walking-right  :lose-life
                     :walking-left :lose-life
                     :standing :lose-life}

   :joypad-left     {:standing :walking-left
                     :walking-right :walking-left }

   :joypad-right    {:standing :walking-right
                     :walking-left :walking-right }

   :lives-none      :game-over 

   :out-of-bounds   :lose-life

   :reset           :creating

   :button          {:standing :jumping
                     :walking-right :jumping 
                     :walking-left  :jumping}
   })

(defn reset [o time-now]
  (let [{:keys [x y ]} (get-renderable o time-now)]
    (assoc
      o
      :start-time time-now
      :pos [x y])))

(defn go-create [o time-now pos & args]
  (-> o
      (move-stationary (or pos [100 100] ))
      (assoc
        :start-time time-now
        :lives 3)
      (SM/event :done)))

(defn go-stand [o time-now & args]
  (-> o
      (reset time-now)
      (assoc
        :anim :idle)))

(defn go-walk [o time-now anim xv]
  (-> o
      (move-linear  )
      (assoc 
        :vel [xv 0]
        :anim anim)))

(defn go-walk-left [o time-now & _ ]  (go-walk o time-now :walking-left -120))
(defn go-walk-right [o time-now & _ ] (go-walk o time-now :walking-right 120))

(defn go-jump [{:keys [vel] :as o} time-now & args]
  (-> o
      (reset time-now)
      (assoc
        :vel (v/add vel [0 5 * 60]))))

(defn go-lose-life [{:keys [lives] :as o} time-now & args]
  (let  [lives-now (dec lives)]
    (-> o
        (reset time-now)
        (assoc :lives lives-now)
        (SM/event (if (= lives-now 0)
                    :lives-none
                    :done)))) )
(def on-event
  {:creating      go-create
   :standing      go-stand
   :walking-left  go-walk-left
   :walking-right go-walk-right
   :jumping       go-jump
   :lose-life     go-lose-life } )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


