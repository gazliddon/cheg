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
                                add-static-img!
                                ]]
            [cheg.webutils :refer [hook-to-reqanim log]]
            ))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def zipvecs (partial map vector))

(defn mk-vec-op [f]
  (let [func (fn [[a b]] (f a b)) ]
    (fn [a b]
      (mapv func (zipvecs a b)))))

(defn mk-vec-scalar-op [f]
  (fn [a b]
    ((mk-vec-op f) a (repeat b))))

(def vec-add (mk-vec-op +))
(def vec-sub (mk-vec-op +))
(def vec-mul (mk-vec-op *))
(def vec-div (mk-vec-op /))

(def vec-add-s (mk-vec-scalar-op +))
(def vec-sub-s (mk-vec-scalar-op +))
(def vec-mul-s (mk-vec-scalar-op *))
(def vec-div-s (mk-vec-scalar-op /))

(defn peturb-obj [{:keys [x y xv yv] :as o} xy]
  (let [[op ov] (mapv o [obj/get-pos obj/get-vel])
        vadd (vec-mul-s  (vec-sub op xy) 0.1)
        new-v (vec-add vadd ov)]
    (obj/set-vel o new-v)))

(defn peturb-objs [xy]
  (mapv #(peturb-obj % xv) (get-in @app-state [:game-state :objs])))

(defn kill-objs! []
  (swap! app-state assoc-in [:game-state :objs] []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

(defn get-stats-state [{:keys [objs player time]}]
  (let [px (:x player)
        py (:y player)
        pstr (str "{ " px "," px " }") ]
    {:text (str (count objs) " objs: player: " pstr )
     :time time }))

(defn stats [ game owner ]
  (reify
    om/IInitState
    (init-state [_] (get-stats-state game))

    om/IWillReceiveProps
    (will-receive-props [ this next-props ]
      (om/update-state! owner (fn [_]( get-stats-state next-props)) ))

    om/IRenderState
    (render-state [ _ state ]
      (dom/p nil (:text state)))))


;; Flow is:
;;   - render    : creates the canvas element with a reference we can get later
;;   - did-mount : called after dom elements created
;;                 sets the comp state to have surface and a renderer
;;                 setting the state causes did-update to be called
;;   - did-update  render everything - also triggered when @app-state :gamestate is updated

(defn render-objs [r objs time-now]
  (do
    (obj/clear r "blue")
    (doseq [{:keys [x y imgs start-time]} objs]
      (let [id (obj/get-frame imgs start-time time-now)]
      (obj/static-img r x y id)))))

(defn canvas [ game owner ]
  (reify
    om/IDidMount
    (did-mount [_]
      (om/update-state!
        owner
        (fn [_]
          {:renderer  (renderer/canvas-renderer (om/get-node owner "surface-ref"))
           })))

    om/IDidUpdate
    (did-update [_ {:keys [objs time]} _]
      (let [ renderer (om/get-state owner :renderer) ]
        (render-objs renderer objs time)))

    om/IRender
    (render [_]
      (dom/canvas #js {:className "canv"
                       :width     800
                       :height    400
                       :id        "surface"
                       :ref       "surface-ref" } ))))

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
          (om/build action-button
                    {:text "Add objs"
                     :action (fn [] (send-game-message :add-objs nil)) })

          (om/build action-button
                    {:text "kill objs"
                     :action (fn [] (send-game-message :kill-objs 1)) })

          (om/build pause-button game)
          )))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)))))

(defn update! []
  (let [game     (:game-state @app-state)
        player   (:player game)
        objs     (:objs game)
        new-objs (obj/update-objs player objs)
        new-time (+ (/ 1 60) (:time game)) ]
    (when-not (:paused game)
      (reset!
        app-state
        (-> @app-state
            (assoc-in [:game-state :time] new-time)
            (assoc-in [:game-state :objs] new-objs) )))))

(defn handle-msg [m-to-f [m v]]
  (let [func (m m-to-f)]
    (when func
      (func v))))

(def game-msg-to-func
  {:toggle-pause (fn [_] (toggle-pause-state)) 
   :add-objs     (fn [_] (add-random-jumpy!))
   :kill-objs    (fn [_] (kill-objs!))} )

(def handle-game-msg (partial handle-msg game-msg-to-func))

(defn main []
  (om/root
    page
    app-state
    {:target (. js/document (getElementById "app"))})

  (when-not (:updating @app-state)

    (add-static-img! 0 0 :logo )


    
    (swap! app-state assoc :updating true)
    (hook-to-reqanim update!))

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


; The game is a series of one dimensional entities in the time axis
; each entity has a position and a dimension (2d, t and variation?)

; At anypoint in time in an entity record you can transform an entity into
; a visual representation by invoking it's function
; vs = ef(e, t)
; the representation can be nil

(defprotocol ITimeEntity
  (create [_ t])
  (appears? [_ lifetime])
  (draw [_ ctx t]))
















