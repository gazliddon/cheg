(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            [cheg.vec :as vec]
            [clojure.pprint :refer :all]
            ))

(defn event [o e & args] (assoc o :event (conj (:event o) (conj [e] args))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set of state events and a state table

(def player-fsm-table 
  {:create           {:nothing :creating}

   :done            {:creating :standing
                     :lose-life :standing}

   :enemy-collision {:walking  :lose-life
                     :standing :lose-life}

   :joypad          {:standing :walking}

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

(defn go-lose-life [{:keys [lives] :as o} t ev & args]
  (-> o
      (assoc :lives (dec lives))
      (event (if (= lives 0)
               :lives-none
               :done))))

(defn player-obj-def []
  {:creating go-create
   :standing go-stand
   :walking go-walk
   :jumping go-jump
   :lose-life go-lose-life } )


(def test-events
  [[:create ]
   [:done ]
   [:joypad :right]
   [:joypad :left]
   [:joypad :right]
   [:joypad :right ]
   [:enemy-collision ]
   [:enemy-collision ]
   [:done ] ])

(defn process-object [fsm-table obj-def {:keys [events object]} event & args]
  (let [next-state    (event->new-state fsm-table event (:state object))

        update-func    (or (next-state obj-def)
                           (fn [o & args] o))

        next-obj       (apply update-func object args)  
        next-events    (conj (:events next-obj) events) ]

    (pprint event)

    {:events (conj events next-events)
     :object (dissoc :events  next-obj)}))

; (defn test-events-again [fsm-table obj-def object events]
;   (reduce (fn [r e] (process-object fsm-table obj-def r e)) object events))


(process-object player-fsm-table player-obj-def {:events [] :object {:state :noting}} :hello)

(pprint test-events)

(pprint 
  (test-events-again
    player-fsm-table 
    player-obj-def 
    {:state :nothing}
    test-events))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; And the tests
