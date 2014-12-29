(ns cheg.keys
  (:require-macros [cljs.core.async.macros :refer [go]])
	(:require [om.core :as om :include-macros true]
              [om.dom :as omdom :include-macros true]
              [cheg.state :as ST]  
              [cljs.core.async :refer [put! chan <! alts!]]))

(enable-console-print!)

(def ONE-KEY         49)
(def TWO-KEY         50)
(def THREE-KEY       51)
(def FOUR-KEY        52)
(def FIVE-KEY        53)
(def SIX-KEY         54)
(def A-KEY           65)
(def D-KEY           68)
(def E-KEY           69)
(def C-KEY           67)
(def G-KEY           71)
(def Z-KEY           90)
(def X-KEY           88)
(def Q-KEY           81)
(def V-KEY           86)
(def W-KEY           87)
(def O-KEY           79)
(def MINUS-KEY       189)
(def PLUS-KEY        187)
(def LEFT-ARROW-KEY  37)
(def RIGHT-ARROW-KEY 39)
(def BACKSPACE-KEY   8)

(defn translate-key-event [event]
  {:key-code  (.-keyCode event)
   :meta-key  (.-metaKey event)
   :shift-key (.-shiftKey event)
   :ctrl-key  (.-ctrlKey event) })

(def key-to-message
  { \w :joypad-up
    \s :joypad-down
    \a :joypad-left
    \d :joypad-right
    \l :button
    \c [ :create [100 100]]
    \p :toggle-button
    \r :reset })

(def misc (atom {}))

(defn process-key-event [key-event]
  "process a goog events keyboard event into a game message"
  (let [clj-event (translate-key-event key-event)
        key-code (:key-code clj-event)
        key-char (char key-code)
        event (get key-to-message key-char :key-press)]
    (if (vector? event)
      (apply ST/send-game-message! :game-event event)  
      (ST/send-game-message! :game-event event))))

