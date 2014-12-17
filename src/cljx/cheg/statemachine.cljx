(ns cheg.statemachine)

(defn state-identity [o & args] o)

(defn get-event-action [table state event]
  (get-in table [state event] state-identity))

(defn process-event [ table {:keys [state] :as object} event & args]
  (let [ev-fun (fn [object event & args] (apply process-event table object event args))
        action-fn (get-event-action table state event)
        new-obj   (apply action-fn object ev-fun state args) ]
    (println (str "event: " event))
    (println (str "old obj" object))
    (println (str "new obj" new-obj))
    (println "")
    new-obj))
