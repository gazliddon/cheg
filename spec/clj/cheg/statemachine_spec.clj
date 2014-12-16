(ns cheg.statemachine-spec
  (:require [speclj.core :refer :all]
            [cheg.statemachine :refer :all :as sm]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Set of state events and a state table
(defn go-create [object ev & args]
  (-> object
      (assoc :pos [0 0]
             :lives 3)
      (ev :done)))

( defn go-standing [object & args]
  (-> object
      (assoc
        :vel [0 0]
        :anim :stand-anim
        :state :standing)))

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
  [:create
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
  (let [ev-info (str event " " (:state prev-state) " -> "  (:state next-state))]
    (if (= prev-state next-state)
      (str ev-info "\n\tNO CHANGE\n")
      (str ev-info "\n\t"prev-state "\n\t" next-state "\n") 
      )))

(defn next-event [{:keys [ changes object ] :as accum} event]
  (let [new-object (process-event fsm-table object event) ]
    (-> accum
        (assoc :object new-object
               :changes (conj
                          changes
                          [(transition-record event object  new-object)])))))

(def runned-events
  (let [starter {:changes [] :object {:state :nothing}}]
    (reduce next-event starter test-events)))
(println "")
(doseq [changes (:changes runned-events)]
  (println changes))
(println "")
(println "End State")
(println (:object runned-events))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; And the tests

