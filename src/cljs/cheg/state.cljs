(ns cheg.state
  (:require [cljs.core.async :refer [put! <! >! chan]] ))

(def -player
  { :x 100 :y 100 :xv 0 :yv 0 :col "green"}
  
  )

(def -state { :messages (chan)

             :updating false

             :game-state {:messages (chan)
                          :paused false
                          :player -player
                          :objs [] 
                          }

             :title "cheg"
             }
  )


(defonce app-state
  (atom -state))

(defn send-game-message [m]
  (let [ch (get-in @app-state [:game-state :messages])]
    (put! ch m)
    ))

(defn -mk-toggle-fn [state addr]
  (fn []
    (swap! state assoc-in addr (not (get-in @state addr)))))

(def toggle-pause-state
  (-mk-toggle-fn app-state [:game-state :paused]))

