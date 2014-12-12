(ns cheg.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put! <! >! chan]]
            [om.dom :as dom :include-macros true]
            [cheg.obj :as objs]
            [cheg.state :refer [app-state
                                toggle-pause-state
                                send-game-message]]
            [cheg.webutils :refer [hook-to-reqanim log]]
            ))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cols
  ["yellow"
   "red"
   "orange"
   "green"
   "blue"
   "white"
   "purple"])

(defn rand-range [low hi]
  (+ low  (rand (- hi low))))

(defn mkobj [x y]
  { :x x :y y :xv (rand-range -9 9) :yv (rand-range -9 9) :col (rand-nth cols)})

(defn add-obj [x y]
  (let [obj        (mkobj x y)
        objs       (get-in @app-state [:game-state :objs])
        new-objs   (into objs [obj]) ]
  
  (swap! app-state assoc-in [:game-state :objs] new-objs))
  "done")

(defn add-rand-objs [n]
  (dotimes [_ n]
    (add-obj (rand 100) (rand 100))))

(defn kill-objs []
  (swap! app-state assoc-in [:game-state :objs] []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn draw-blob [ctx x y w h col]
  (doto ctx
    (aset "fillStyle" col)
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


(defn get-paused-text [b]
  (if b
    "unpause"
    "pause"))


(defn action-button [app owner]
  (reify 
    om/IRenderState
    (render-state [this _]
      (dom/button #js {:onClick (:action app)} (:text app))))  )

(defn pause-button [app owner]
  (reify 
    om/IInitState
    (init-state [_]
      {:paused (:paused app)})

    om/IWillReceiveProps
    (will-receive-props [ this  {:keys [paused]}]
      (om/set-state! owner :paused paused))

    om/IRenderState
    (render-state [this {:keys [ paused ]}]
      (om/build action-button {:text (get-paused-text paused)
                               :action toggle-pause-state }))))

(defn get-stats-str [{:keys [objs player]}]
  (let [px (:x player)
        py (:y player)
        pstr (str "{ " px "," px " }")
        ]
    (str "There are " (count objs) " objs, player is at " pstr)))

(defn stats [ game owner ]
  (reify
    om/IInitState
    (init-state [_]
      {:text (get-stats-str game)})

    om/IWillReceiveProps
    (will-receive-props [ this next-props ]
      (om/set-state! owner :text (get-stats-str next-props)))

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
          (om/build action-button {:text "Add objs"
                                   :action (fn [] (send-game-message :add-objs))
                                   })
          (om/build action-button {:text "kill objs"
                                   :action (fn [] (send-game-message :kill-objs))
                                   })
          (om/build pause-button game))))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)
        ))))



(defn update []
  (let [game     (:game-state @app-state)
        player   (:player game)
        objs     (:objs game)
        new-objs (objs/update-objs player objs) ]
    (when-not (:paused game)
      (swap! app-state assoc-in [:game-state :objs] new-objs))))


(defn handle-msg [m-to-f m]
  (let [func (m m-to-f)]
    (when func
      (println "Calling!")
      (func))))

(def game-msg-to-func
  {:toggle-pause toggle-pause-state
   :add-objs #(add-rand-objs 100)
   :kill-objs kill-objs})

(def handle-game-msg (partial handle-msg game-msg-to-func))

(defn main []

  (om/root
    page
    app-state
    {:target (. js/document (getElementById "app"))})

  (when-not (:updating @app-state)
    (swap! app-state assoc :updating true)
    (hook-to-reqanim update))

  (go-loop
    []
    (let [game (:game-state @app-state)
          msg (<! (:messages game)) ]
      (handle-game-msg msg)
      (recur)))
  
  ;; Main message handler
  ; (go-loop [] 
  ;          (let [msg (<! (:messages @app-state))]
  ;            (println msg))
  ;          (recur))
  )
