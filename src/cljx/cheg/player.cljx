(ns cheg.player
  (:require
    [cheg.state :as ST]
    [cheg.statemachine :as sm]
    [cheg.vec :as vec]))

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

(defn go-create [o pos & args]
  (-> o
      (assoc
        :pos pos
        :lives 3)
      (sm/event :done)))

(defn go-stand [o & args]
  (-> o
      (assoc
        :vel [0 0]
        :anim :idle)))

(defn go-walk [ anim xv {:keys [vel] :as o} & args]
  (-> o
      (assoc 
        :vel [xv 0]
        :anim anim)))

(defn go-walk-left [o & args] (apply go-walk :walking-left -1 o args))

(defn go-walk-right [o & args] (apply go-walk :walking-right 1 o args))

(defn go-jump [{:keys [vel] :as o} & args]
  (-> o
      (assoc
        :vel (vec/add vel [0 5]))))

(defn go-lose-life [{:keys [lives] :as o} & args]
  (let  [lives-now (dec lives)]
    (-> o
        (assoc :lives lives-now)
        (sm/event (if (= lives-now 0)
                 :lives-none
                 :done)))) )
(def on-event
  {:creating      go-create
   :standing      go-stand
   :walking-left  go-walk-left
   :walking-right go-walk-right
   :jumping       go-jump
   :lose-life     go-lose-life } )

(defn get-renderable [time-now {:keys [start-time pos vel]} ]
  (let [obj-time (- time-now start-time)  
        [x y] (vec/add pos (vec/mul-s vel obj-time))
        renderable (ST/mkspr {:x x
               :y y
               :imgs [:run-f1]
               :spr-handle :bottom-middle })
        ]

    renderable
    ))

