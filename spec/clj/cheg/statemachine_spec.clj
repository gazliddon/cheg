(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            [cheg.vec :as vec]
            [clojure.pprint :refer :all]
            ))

(defn event [ o & new-event ]
  (let [ current-events (get o :events [])]
    (assoc o :events (conj current-events new-event ))))

(defn process-an-event [fsm-table obj-def object [ev & evargs]]
  (if-let [next-state    (event->new-state fsm-table ev (:state object)) ]
    (let [update-func    (or (next-state obj-def) (fn [o t & ] o))
          next-obj       (apply update-func (assoc object :state next-state) 0 evargs)  ]
      next-obj)
    object))

(defn process-events [fsm-table obj-def arg-object arg-events ]
  (let [ev-fn (partial process-an-event fsm-table obj-def)]
    (loop [object arg-object events arg-events ]

      (let [events (into (get object :events []) events)
            object (dissoc object :events)]  
        (if (empty? events)
          object
          (recur (ev-fn object (first events)) (rest events)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; FSM for the player
;; an fsm table
;; a bunch of routines to call on state change
;; and a table tieing the routines to specific states 

(def player-fsm-table 
  {:create           {:nothing :creating}

   :done            {:creating :standing
                     :lose-life :standing}

   :enemy-collision {:walking  :lose-life
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

(defn go-create [o & args]
  (-> o
      (assoc
        :pos [0 0]
        :lives 3)
      (event :done)))

(defn go-stand [o t & args]
  (-> o
      (assoc
        :vel [0 0]
        :anim :idle)))

(defn go-walk [ anim xv {:keys [vel] :as o} t & args]
  (-> o
      (assoc 
        :vel (vec/add vel [xv 0])
        :anim anim)))
(defn go-walk-left [o t & args] (apply go-walk :walk-left -1 o t & args))
(defn go-walk-left [o t & args] (apply go-walk :walk-right -1 o t & args))

(defn go-jump [{:keys [vel] :as o} t & args]
  (-> o
      (assoc
        :vel (vec/add vel [0 5]))))

(defn go-lose-life [{:keys [lives] :as o} t & args]
  (let  [lives-now (dec lives)]
    (-> o
        (assoc :lives lives-now)
        (event (if (= lives-now 0)
                 :lives-none
                 :done)))) )

(def player-obj-def
  {:creating      go-create
   :standing      go-stand
   :walking-left  go-walk-left
   :walking-right go-walk-right
   :jumping       go-jump
   :lose-life     go-lose-life } )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A bunch of test events to throw at a player object
(def test-events
  [[:create ]
   [:joypad-right]
   [:joypad-left]
   [:joypad-right]
   [:joypad-right ]
   [:enemy-collision ]
   [:enemy-collision ]
   [:joypad-right ]
   [:joypad-right ]
   [:enemy-collision ] ])

(def init-obj {:state :nothing})

(def obj-after-test-events
  (process-events player-fsm-table player-obj-def init-obj test-events))
; (pprint obj-after-test-events)

(pprint obj-after-test-events)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The tests
(describe "Test a full run of mock events"
          (it "Should end in an expected state"
              (should= :game-over (:state obj-after-test-events) ))
          )


;; TODO A test to see if it can deal with irrelevant events fine

