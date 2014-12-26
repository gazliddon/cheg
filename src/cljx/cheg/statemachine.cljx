(ns cheg.statemachine
  (:require
    [clojure.data :refer [diff]]
    ))

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
(derive clojure.lang.PersistentHashMap ::hash-map)

(defmulti state->next-state (fn [mapping _] (type mapping)))

(defmethod state->next-state ::keyword [ mapping _ ]
          mapping)

(defmethod state->next-state clojure.lang.PersistentHashMap [mapping current-state]
  (get mapping current-state nil))

(defmethod state->next-state nil [_ _] nil)

(defn event->new-state [fsm-table event current-state]
  (let [mapping (event fsm-table)]
    (state->next-state mapping current-state)
    ))

(defn event [ o & new-event ]
  (let [ current-events (get o :events [])]
    (assoc o :events (conj current-events new-event ))))

(defn process-an-event [fsm-table obj-def time-now object [ev & evargs]]
  (if-let [next-state    (event->new-state fsm-table ev (:state object)) ]
    (let [update-func    (or (next-state obj-def) (fn [o & ] o))
          next-obj       (apply update-func (assoc object :state next-state :start-time time-now) evargs)
          obj-no-ev      (dissoc object :events)
          next-obj-no-ev  (dissoc next-obj :events)
          ]
      (when-not (= next-obj-no-ev obj-no-ev )
        (println (str "Event " ev " on state " (:state object)))
        (println (diff obj-no-ev next-obj-no-ev))
        (println ""))

      next-obj)
    object))

(defn process-events [fsm-table obj-def time-now arg-object arg-events ]
  (let [ev-fn (partial process-an-event fsm-table obj-def time-now)]

    (loop [object arg-object events arg-events ]
      (let [events (into (get object :events []) events)
            object (dissoc object :events)]  
        (if (empty? events)
          object
          (recur (ev-fn object (first events)) (rest events)))))))

(defn obj-process-events [{:keys [fsm-table on-event] :as object} time-now event & event-args]
  (apply process-events fsm-table on-event time-now object event event-args))
