(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            [cheg.vec :as vec]
            ))

(defn event [o e & args] (assoc o :event (conj (:event o) (conj [e] args))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set of state events and a state table

(def fsm-table
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

(defn go-standing [o t & args]
  (-> o
      (assoc
        :vel [0 0]
        :anim :idle)))

(defn go-walking [ {:keys [vel] :as o} t dir & args]
  (let [xv (get {:left -1 :right 1} dir 0)]
    (-> o
        (assoc 
          :vel (vec/add vel [xv 0])
          :anim (if (< xv 0)
                  :walk-left 
                  :walk-right)))))

(defn go-jumping [{:keys [vel] :as o} t & args]
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
   [:done ]
   ])

(defn process-event-2 [fsm-table obj-def object event & args]
  (let [current-state (:state object ) 
        next-state (event->new-state fsm-table event state) ]
    (if (nil? next-state)
      object
      (let [identity-func (fn [object events & args] [object events])
            ev-func (fn [object event & args]
                      (apply process-event-2
                             fsm-table
                             obj-def {:object object 
                                      :events (conj events [event])}
                             args))

            func        (get obj-def next-state identity-func)
            next-object (apply func (assoc object :state next-state) 0 ev-func args)
            ]

        ))
    ))

(defn pfunc [o [event & args]]
  (apply
    process-event-2 fsm-table-2 player-obj-def o event
    args))

(defn test-events-again []
  (reduce pfunc {:events [] :object {:state :nothing}}  test-events))

; (println (take 40 (repeat "-")))
(println (test-events-again))
; (println (take 40 (repeat "-")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing support routines
(defn join-lines [str-array]
  (reduce #(str %1 (apply str %2) "\n") "" str-array))

(defn transition-report [event old-object new-object]
  (let [CR "\n"]
    (str
      "Event: " event ": " (:state old-object)" -> " (:state new-object) CR
      old-object CR
      new-object CR
      "Equal? " (= new-object old-object))))

(defn next-event [{:keys [ changes object ] :as accum} event]
  (let [new-object (process-event fsm-table object event) ]
    (assoc
      accum
      :object new-object
      :changes (conj
                 changes 
                 {:event event
                  :old-object object
                  :new-object new-object
                  :object-change? (= new-object object)}))))

(def run-results
  (let [starter {:changes [] :object {:state :nothing}}]
    (reduce next-event starter test-events)))

(def event-log (:changes run-results))
(def ignored-events (filter #( :object-change? %1) event-log))
(def effecting-events (filter #(complement ( :object-change? %1)) event-log))

(def final-obj (:object run-results))


; (println (take 40 (repeat "-")))
; (println "Events that change the object's state")
; (doseq [{:keys [event old-object new-object]} effecting-events  ]
;   (println (transition-report event old-object new-object )))

; (println (take 40 (repeat "-")))
; (println "Events that had no effect")
; (doseq [{:keys [event old-object new-object]} ignored-events]
;   (println (transition-report event old-object new-object )))

; (doseq [e event-log]
;   (println e))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; And the tests
(describe "Should have been an exact number of events"
          (it "should have x events"
              (should= 10 (count event-log))))

(describe "Should have been an exact number of actual state changes"
          (it "should have x state changes"
              (should= 10 (count effecting-events))))

(describe "Should have been an exact number of actual ignored events"
          (it "should have x ignored events"
              (should= 4 (count ignored-events))))

(describe "The state of the final obj after the state run"
          (it "The final object should be this"
              (should= {:anim :die-anim
                        :vel [1 0]
                        :pos [0 0]
                        :lives 0
                        :state :game-over} final-obj)))

