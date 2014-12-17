(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set of state events and a state table
(defn go-create [object ev & args]
  (-> object
      (assoc 
             :pos [0 0]
             :lives 3)
      (ev :done)))


( defn go-standing [object & args]
  (-> object
      (assoc
        :vel [0 0]
        :anim :stand-anim
        :state :standing)))

(println (go-create {} go-standing))

(defn go-walk [object & args]
  (-> object
      (assoc
        :vel [1 0]
        :anim  :walk-anim
        :state :walking
        )))

(defn go-die [{:keys [lives] :as object} ev & args]
  (-> object
      (assoc
        :anim  :die-anim
        :state :dieing
        :lives (dec lives))
      (ev :done) ))

(defn go-finished? [{:keys [lives] :as object} & args]
  (-> object
      (assoc :state (if (= 0 lives)
                      :game-over
                      :standing))))
(def fsm-table 
  
  {:nothing {:create            go-create
             :done              go-standing}

   :standing {:joypad           go-walk
              :enemy-collision  go-die }

   :walking  {:no-joypad        go-standing
              :enemy-collision  go-die }

   :dieing   {:done             go-finished? } })

(def test-events
  [:create               ; nothing -> created
   :done                 ; ignored
   :joypad               ; standing -> :walking
   :enemy-collision      ; walking ->
   :joypad
   :joypad
   :joypad
   :enemy-collision
   :enemy-collision
   :done ])

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


(println (take 40 (repeat "-")))
(println "Events that change the object's state")
(doseq [{:keys [event old-object new-object]} effecting-events  ]
  (println (transition-report event old-object new-object )))

; (println (take 40 (repeat "-")))
; (println "Events that had no effect")
; (doseq [{:keys [event old-object new-object]} ignored-events]
;   (println (transition-report event old-object new-object )))

(doseq [e event-log]
  (println e))


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

