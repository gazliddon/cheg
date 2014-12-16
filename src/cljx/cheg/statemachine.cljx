(ns cheg.statemachine
   )

(defn state-identity [o _ _]
  o)

(defn p-obj [o]
  (println o)
  o)

(defn get-event-action [table event state]
  (get-in table [state event] state-identity))

(defn process-event [ table {:keys [state] :as object} event]
  (-> object
      (p-obj)
      ((get-event-action table state event) event state)  
      (p-obj)
      )
  )
