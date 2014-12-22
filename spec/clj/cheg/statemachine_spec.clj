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
;; Set of state events and a state table

(def player-fsm-table 
  {:create           {:nothing :creating}

   :done            {:creating :standing
                     :lose-life :standing}

   :enemy-collision {:walking  :lose-life
                     :standing :lose-life}

   :joypad          {:standing :walking
                     :walking :walking}

   :lives-none      :game-over 

   :out-of-bounds   :lose-life

   :button          {:standing :jumping
                     :walking  :jumping}
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

(defn go-walk [ {:keys [vel] :as o} t dir & args]
  (let [xv (get {:left -1 :right 1} dir 0)]
    (-> o
        (assoc 
          :vel (vec/add vel [xv 0])
          :anim (if (< xv 0)
                  :walk-left 
                  :walk-right)))))

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
                 :done))))

  )(def player-obj-def
  {:creating go-create
   :standing go-stand
   :walking go-walk
   :jumping go-jump
   :lose-life go-lose-life } )

(def test-events
  [[:create ]
   [:joypad :right]
   [:joypad :left]
   [:joypad :right]
   [:joypad :right ]
   [:enemy-collision ]
   [:enemy-collision ]
   [:joypad :right ]
   [:joypad :right ]
   [:enemy-collision ] ])


(def init-obj {:state :nothing})

; (def obj-record-after-one-step
;   (process-an-event player-fsm-table player-obj-def init-obj [ :create ]))

; (pprint obj-record-after-one-step)

(def obj-after-test-events
  (process-events player-fsm-table player-obj-def init-obj test-events))
; (pprint obj-after-test-events)

(pprint obj-after-test-events)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; And the tests
; (describe "Testing obj-record-after-one-step"
;           (it "Should step a blank player through one step of the state machne"
;               (let [ {:keys [state events] :as object} obj-record-after-one-step ]
;                 (should= [:done] events )
;                 (should= :creating state ))))



; test that the end state is what I expect

(describe "Test a full run of mock events"
          (it "Should end in an expected state"
              (should= :game-over (:state obj-after-test-events) ))
          )

