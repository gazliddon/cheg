(ns cheg.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put! <! >! chan]]
            [om.dom :as dom :include-macros true]
            [cheg.obj :as objs]))
(enable-console-print!)
; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ;; Utils / needs own file
(defn hook-to-reqanim [f]
  (f)
  (js/requestAnimationFrame
    #(hook-to-reqanim f)))

; Log to console
(defn log [a]
  (.log js/console a) )

(defn rand-range [low hi]
  (+ low  (rand (- hi low))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn draw-blob [ctx x y w h col]
  (doto ctx
    (aset "fillStyle" col)
    (.moveTo x y)
    (.fillRect x y w h) ))

(defn draw-objs [ctx objs]
  (let [w 20 h 20]
    (doseq [o objs]
      (let [{:keys [x y col]} o ]
        (draw-blob ctx x y w h col)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn render-all [ game-state surface ]
  (let
    [ width    (.-width surface)
      height   (.-height surface)
      objs     (:objs game-state) ]
    (doto
      (.getContext surface "2d")
      (aset "imageSmoothingEnabled" "false")
      (aset "fillStyle" "#220022")
      (.fillRect 0 0 width height)
      (draw-objs objs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn om-render-all [game-state owner node-ref]
  (let [surface (om/get-node owner node-ref)
        ctx (.getContext surface "2d")]
    (render-all game-state surface))) 

(defn header [ app owner ]
  (reify
    om/IRender
    (render [ this ]
      (dom/h1 nil (:title app)))))

(defn stats [ game owner ]
  (reify
    om/IInitState
    (init-state [_]
      {:text "nothing"})

    om/IWillReceiveProps
    (will-receive-props [ this next-props ]
      (let [nobjs (count ( :objs next-props ))
            player (:player next-props)
            px (:x player)
            py (:y player)
            pstr (str "{ " px "," px " }")
            status (str "There are " nobjs " objs, player is at " pstr)]
      (om/set-state! owner :text status)))

    om/IRenderState
    (render-state [ _ state ]
      (dom/p nil (:text state)))))

(defn canvas [ game owner ]
  (reify
    om/IDidMount
    (did-mount [_]
      (om-render-all game owner "surface-ref"))

    om/IDidUpdate
    (did-update [_ _ _]
      (om-render-all game owner "surface-ref"))

    om/IRender
    (render [_]
      (dom/canvas #js {:className "canv"
                       :id        "surface"
                       :ref       "surface-ref" }))))

(defn container [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (let [game (:game-state app) ]
        (dom/div
          #js {:className "container"}
          (om/build header app)
          (om/build canvas game)
          (om/build stats game)
          (dom/p nil "Bottom"))))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)))))

(def app-state
  (atom {
         :updating false
         :game-state {
                      :player { :x 100 :y 100 :xv 0.1 :yv 0.1 }

                      :objs [
                             {:x 30 :y 20 :yv 0.1 :xv 0.5 :col "yellow"}
                             {:x 31 :y 30 :yv 0.2 :xv 0.6 :col "blue"}
                             {:x 35 :y 10 :yv 0.3 :xv 0.7 :col "black"}
                             {:x 30 :y 90 :yv 0.4 :xv 0.8 :col "white"}
                             ]     
                      }

         :title "cheg"
         }))


(def cols
  ["yellow"
   "red"
   "orange"
   "green"
   "blue"
   "white"
   "purple"])

(defn update []
  (let [game     (:game-state @app-state)
        player   (:player game)
        objs     (:objs game)
        new-objs (objs/update-objs player objs) ]
    (swap! app-state assoc-in [:game-state :objs] new-objs)))

(defn mk-obj [x y]
  { :x x :y y :xv (rand-range -9 9) :yv (rand-range -9 9) :col (rand-nth cols)})


(defn add-obj [x y]
  (let [obj        (mk-obj x y)
        objs       (get-in @app-state [:game-state :objs])
        new-objs   (into objs [obj]) ]
  
  (swap! app-state assoc-in [:game-state :objs] new-objs))
  "done")

(defn add-rand-objs [n]
  (dotimes [_ n]
    (add-obj (rand 100) (rand 100))))

(defn kill-objs []
  (swap! app-state assoc-in [:game-state :objs] []))

(defn main []
  (om/root
    page
    app-state
    {:target (. js/document (getElementById "app"))})

  (when (not (:updating @app-state))
    (swap! app-state assoc :updating true)
    (hook-to-reqanim update)) )
