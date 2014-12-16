(ns cheg.game
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
            [cljs.core.async :refer [put! <! >! chan]]
            [cheg.gfx :as gfx]
            [cheg.vec :as vec]
            [cheg.obj :as obj]
            [cheg.state :refer [app-state
                                toggle-pause-state
                                send-game-message
                                add-random-jumpy!
                                mkspr
                                ]] 
            ))


(defn get-event-action [table event state]
  (let [identity (fn [o _] o)]
    (get-in table [state event] identity)))

(defn process-events [table init-state events]
  (let [ev-fn (fn [os ev]
                (process-event table os ev) ) ])
  (reduce event-fn init-state events))

; (defn game-scene [] )


; (defn process-event [ table {:keys [current-state] :as object} event]
;   ((get-event-action table current-state event) object event current-state))

; (defn go-die [state event from] state) 
; (defn go-double-jump [state event from] state) 
; (defn go-fall [state event from] state) 
; (defn go-jump [state event from] state) 
; (defn go-quit [state from] state) 
; (defn go-running [state event from] state) 

; (def player-state-table
;   {:spawning {:timeout       go-running }

;    :running  {:button        go-jump
;               :fall-off      go-fall }

;    :falling  {:out-of-bounds go-die
;               :button        go-jump
;               :landed        go-running }

;    :jumping  {:button        go-double-jump }
;    :dieing   {:timeout       go-quit}})



