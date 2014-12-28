(ns cheg.player
  (:require
    [cheg.state :as ST]
    [cheg.statemachine :as SM]
    [cheg.vec :as vec]))


(defn get-renderable [time-now {:keys [start-time pos vel]} ]
  (let [obj-time (- time-now start-time)  
        [x y] (vec/add pos (vec/mul-s vel obj-time))
        renderable (ST/mkspr {:x x
               :y y
               :imgs [:run-f1]
               :spr-handle :bottom-middle })
        ]

    renderable))

(defn player-renderable [{:keys [renderable] :as o} time-now]
  (if renderable
    (renderable time-now o)
    {:x 0 :y 0}))

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

(defn do-player [f o time-now & args]
  (-> o
      (reset time-now)
      (apply f o time-now args)))


(defn go-create [o time-now pos & args]
  (-> o
      (assoc
        :start-time time-now
        :renderable get-renderable
        :vel [0 0]
        :pos pos
        :lives 3)
      (SM/event :done)))

(defn go-stand [o time-now & args]
  (-> o
      (reset time-now)
      (assoc
        :vel [0 0]
        :anim :idle)))

(defn go-walk [o time-now anim xv]
  (-> o
      (reset time-now)
      (assoc 
        :vel [xv 0]
        :anim anim)))

(defn go-walk-left [o time-now & _ ] (go-walk o time-now :walking-left -120))
(defn go-walk-right [o time-now & _ ] (go-walk o time-now :walking-right 120))

(defn go-jump [{:keys [vel] :as o} time-now & args]
  (-> o
      (reset time-now)
      (assoc
        :vel (vec/add vel [0 5 * 60]))))

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

