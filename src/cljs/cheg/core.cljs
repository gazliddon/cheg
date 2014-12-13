(ns cheg.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put! <! >! chan]]
            [om.dom :as dom :include-macros true]
            [cheg.canvasrenderer :as renderer]
            [cheg.gfx :as gfx]
            [cheg.obj :as obj]
            [cheg.slider :as slider]
            [cheg.state :refer [app-state
                                toggle-pause-state
                                send-game-message
                                add-random-jumpy!
                                ]]
            [cheg.webutils :refer [hook-to-reqanim log]]
            ))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn kill-objs []
  (swap! app-state assoc-in [:game-state :objs] []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn draw-blob [ctx x y w h col]
  (doto ctx
    (aset "fillStyle" col)
    (.fillRect x y w h) ))

(defn draw-spr [ctx x y img]
  (let [w 100
        h 100]
  (doto ctx
    (.drawImage img x y w h))))

(defn draw-objs [ctx objs]
  (let [time-now (get-in @app-state [:game-state :time ])]
    (doseq [o objs]
      (let [{:keys [x y imgs ]} o
            img (obj/obj-get-frame o time-now)]
        (draw-spr ctx x y img)))  )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn render-all [ game-state surface ]
  (let
    [ width    (.-width surface)
      height   (.-height surface)
      objs     (:objs game-state) ]
    (doto 
      (.getContext surface "2d")
      (aset "fillStyle" "#880088")
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

(defn get-stats-str [{:keys [objs player time]}]
  (let [px (:x player)
        py (:y player)
        pstr (str "{ " px "," px " }")
        ]
    (str (count objs) " objs: player: " pstr " time:" time)))

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
      (let [surface (om/get-node owner "surface-ref") ]
        (om/set-state! owner :surface surface)
        (render-all game surface)))

    om/IDidUpdate
    (did-update [_ _ _]
      (render-all game (om/get-state owner :surface)) )

    om/IRender
    (render [_]
      (dom/canvas #js {:className "canv"
                       :width 400
                       :height 400
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
          (om/build pause-button game)
          )))))



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
        new-objs (obj/update-objs player objs)
        new-time (+ (/ 1 60) (:time game))
        ]
    (when-not (:paused game)
      (do
      (swap! app-state assoc-in [:game-state :time] new-time)
      (swap! app-state assoc-in [:game-state :objs] new-objs)   
        )
      )))

(defn handle-msg [m-to-f m]
  (let [func (m m-to-f)]
    (when func
      (println "Calling!")
      (func))))

(def game-msg-to-func
  {:toggle-pause toggle-pause-state
   :add-objs add-random-jumpy!
   :kill-objs kill-objs})

(def handle-game-msg (partial handle-msg game-msg-to-func))

(defn static-obj [ img-id x y ]
  (reify
    obj/ICreate
    (create [o ctx]
      (assoc
        o
        :img img-id
        :x x
        :y y))

  obj/IRender
  (render [{:keys [x y img]} ctx _]
          (obj/static-img ctx x y img))))

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
