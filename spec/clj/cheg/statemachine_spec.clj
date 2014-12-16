(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set of state events and a state table
(defn go-create [object event current-state ev]
  (-> object
      (assoc :pos [0 0]
             :lives 3)
      (ev :done)))

( defn go-standing [object event current-state ev]
  (-> object
      (assoc
        :vel [0 0]
        :anim :stand-anim
        :state :standing)))

(defn go-walk [object event current-state ev]
  (-> object
      (assoc
        :vel [1 0]
        :anim  :walk-anim
        :state :walking)))

(defn go-die [{:keys [lives] :as object} event current-state ev]
  (-> object
      (assoc
        :anim  :die-anim
        :state :dieing
        :lives (dec lives))
      (ev :done) ))

(defn go-finished? [{:keys [lives] :as object} event current-state ev]
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
  [ :create
   :done
   :joypad
   :enemy-collision
   :joypad
   :joypad
   :joypad
   :enemy-collision
   :enemy-collision
   :done
   ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing support routines

(defn transition-record [event prev-state next-state]
  (str event " : "prev-state " -> " next-state))

(defn add-transition-record [{:keys [:state-changes] :as player-state} event prev-state next-state]
  (assoc
    player-state
    :state-changes (conj
                     state-changes
                     (transition-record event prev-state next-state))))

(defn next-event [{:keys [ changes object ] :as accum} event]
  (let [new-object (process-event fsm-table object event)]
    (-> accum
        (assoc :object new-object
               :changes (conj
                          changes
                          [(transition-record event (:state object) (:state new-object))])))))

(def runned-events
  (let [starter {:changes [] :object {:state :nothing}}]
    (reduce next-event starter test-events)))

(doseq [c  (:changes  runned-events)]
  (println c)
  )
(println "object is ")
(println (:object runned-events))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; And the tests

; (def fake-player
;   {:current-state :nothing
;    :state-changes []}
;   )
