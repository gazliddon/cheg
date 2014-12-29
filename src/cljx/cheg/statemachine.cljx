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

(derive #+clj clojure.lang.Keyword #+cljs cljs.core/Keyword ::keyword)
(derive #+clj clojure.lang.PersistentHashMap #+cljs cljs.core/PersistentArrayMap ::hash-map)
(derive #+clj clojure.lang.PersistentVector #+cljs cljs.core/PersistentVector ::vector)

(defmulti state->next-state (fn [mapping _] (type mapping)))

(defmethod state->next-state ::keyword [ mapping _ ]
          mapping)

(defmethod state->next-state ::hash-map [mapping current-state]
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
          next-obj       (apply update-func object time-now evargs) ]
      (assoc next-obj :state next-state))
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
