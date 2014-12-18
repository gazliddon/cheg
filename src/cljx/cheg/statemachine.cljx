(ns cheg.statemachine)

(defn state-identity [o & args] o)

(defn get-event-action [table state event]
  (get-in table [state event] state-identity))

(defn process-event [ table {:keys [state] :as object} event & args]
  (let [ev-fun (fn [object event & args] (apply process-event table object event args))
        action-fn (get-event-action table state event)
        new-obj   (apply action-fn object ev-fun state args) ]
    new-obj))


; Replacement state machine :D

(derive clojure.lang.Keyword ::keyword)
; (derive clojure.lang.PersistentArrayMap ::hash-map)
(derive clojure.lang.PersistentHashMap ::hash-map)

(defmulti state->next-state (fn [mapping _] (type mapping)))

(defmethod state->next-state ::keyword [ mapping _ ]
          mapping)

(defmethod state->next-state clojure.lang.PersistentHashMap [mapping current-state]
  (get mapping current-state :no-change))

(defmethod state->next-state nil [_ _]
  :no-change)

(defn event->new-state [fsm-table event current-state]
  (let [mapping (event fsm-table)]
    (state->next-state mapping current-state)
    ))
