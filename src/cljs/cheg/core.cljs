(ns cheg.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defonce app-state (atom {:text "Gaz really says Hello Chestnut!"}))

(def canvas-dims  #js {:width 200 :height 300 })

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (dom/div nil
                   (dom/h1 nil (:text app))
                   (dom/p nil (:text app))
                   (dom/canvas canvas-dims )
                   (dom/p nil "yeah!") ))))
    app-state
    {:target (. js/document (getElementById "app"))}))
