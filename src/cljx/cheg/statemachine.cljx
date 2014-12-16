(ns cheg.statemachine)

(defn state-identity [o & args] o)

(defn get-event-action [table state event]
  (get-in table [state event] state-identity))

(defn process-event [ table {:keys [state] :as object} event & args]
  (let [ev-fun (partial process-event table)
        action-fn (get-event-action table state event)
        new-obj  (action-fn object ev-fun state args)]
    new-obj))
