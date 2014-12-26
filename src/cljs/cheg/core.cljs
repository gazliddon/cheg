(ns cheg.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require

    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]

    [goog.events :as events]
    [cljs.core.async :refer [put! <! >! chan]]


    [cheg.canvasrenderer :as renderer]
    [cheg.keys :as ckeys]
    [cheg.gfx :as gfx]
    [cheg.vec :as vec]
    [cheg.obj :as obj]

    [cheg.player :as player]


    [cheg.state :refer [app-state
                        toggle-pause-state
                        send-game-message!
                        add-random-jumpy!
                        mkspr
                        ]] )
  (:import [goog.events EventType]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some scr size values
(def wscale 1)
(def scr-width (* wscale 1280))
(def scr-height (* wscale 720))
(def scr-dims [scr-width scr-height])
(def neg-scr-dims (vec/sub [0 0] scr-dims))
(def get-scr-pos (partial gfx/get-offset neg-scr-dims))

(defn get-pos-perc [perc-pos] (vec/mul perc-pos scr-dims))
(def bg-col "#60b0ff")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-game [{:keys [game-time objs] :as game} dt]
  (let [new-time (+ game-time dt)]
    (assoc
      game
      :objs (obj/update-objs objs new-time)
      :game-time new-time)))

(defn update! []
  (let [ game (:game-state @app-state) ]
    (when-not (:paused game)
      (swap! app-state assoc :game-state (update-game game (/ 1 60))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn kill-objs! []
  (swap! app-state assoc-in [:game-state :objs] []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn header [ app owner ]
  (reify
    om/IRender
    (render [ this ]
      (dom/h1 nil (:title app)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn action-button [app owner]
  (reify 
    om/IRenderState
    (render-state [this _]
      (dom/button #js {:onClick (:action app)} (:text app))))  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-paused-text [b]
  (if b
    "unpause"
    "pause"))

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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-stats-state [{:keys [objs player game-time]}]
  (let [px (:x player)
        py (:y player)
        pstr (str "{ " px "," px " }") ]
    {:text (str (count objs) " objs: player: " pstr )
     :game-time game-time }))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn render-obj-list [r objs time-now]
  (doseq [{:keys [render ] :as o } objs]
    (render r o time-now)))

(defn strip-render [r {:keys [xv x y imgs spr-handle] :as o} time-now]
  (let [pos [x y]
        id (obj/obj-get-frame o time-now)
        [w h] (gfx/get-img-dims id)
        offset (gfx/get-img-offset id spr-handle)
        time-offset (vec/mul [xv 0] [ time-now time-now])
        with-offsets (vec/add pos (vec/add time-offset offset))
        max-idx  (+ 1  (/ scr-width w))

        wrap-x (* max-idx w) ]

    (doseq [ix (range max-idx) ]
      (let [ [nx ny] (vec/add with-offsets [(* ix w ) 0] ) ]
        (obj/static-img r (- (mod nx wrap-x) w)  ny id)
        )))
  )


(defn parralax [xv pos-perc img spr-handle])

(def titles
  [{:xv -85  :pos [0 -0.05] :img :sky1        :spr-handle :top-left    :render strip-render}
   {:xv -73  :pos [0 0.12]  :img :sky2        :spr-handle :top-left    :render strip-render}
   {:xv -100 :pos [0 0.98]  :img :background2 :spr-handle :bottom-left :render strip-render}
   {:xv -150 :pos [0 1.02]  :img :background1 :spr-handle :bottom-left :render strip-render}
   {:xv 0    :pos [0.5 0.5] :img :logo        :spr-handle :centered } ] )

(defn tospr [{:keys [img pos xv] :as spr}]
  (let [[x y] (get-pos-perc  pos) ]
    (mkspr (assoc spr :imgs [img] :x x :y y))))

(def titles-sprs
  (mapv tospr titles))



;; Flow is:
;;   - render    : creates the canvas element with a reference we can get later
;;   - did-mount : called after dom elements created
;;                 sets the comp state to have surface and a renderer
;;                 setting the state causes did-update to be called
;;   - did-update  render everything - also triggered when @app-state :gamestate is updated

(defn got-a-key-event [ev]
  (send-game-message! :keypress ev))

(defn canvas [ game owner ]
  (reify
    om/IDidMount
    (did-mount [_]
      (om/update-state!
        owner
        (fn [_]
          {:renderer  (renderer/canvas-renderer (om/get-node owner "surface-ref")) })))

    om/IWillUpdate
    (will-update [_ {:keys [objs game-time player]} _]
      (let [ renderer (om/get-state owner :renderer) ]
        (obj/clear renderer bg-col)
        (render-obj-list renderer titles-sprs game-time)
        (render-obj-list renderer objs game-time)

        (let [player-renderable (player/get-renderable game-time player)]
          (render-obj-list
            renderer
            [player-renderable]
            game-time))))

    om/IDidUpdate
    (did-update [_ {:keys [objs game-time]} _]
      (update!))

    om/IRender
    (render [_]
      (dom/canvas #js {:className "canv"
                       :width     scr-width
                       :height    scr-height
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
          (dom/div
            nil
            (om/build canvas game))

          (dom/div
            nil
            (om/build stats game)
            (om/build action-button
                      {:text "Add objs"
                       :action (fn [] (send-game-message! :add-objs )) })

            (om/build action-button
                      {:text "Create Player"
                       :action (fn [] (send-game-message! :game-event :create )) })

            (om/build action-button
                      {:text "kill objs"
                       :action (fn [] (send-game-message! :kill-objs )) })

            (om/build pause-button game)))))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)))))

(defn handle-msg [m-to-f [m v]]
  (let [func (m m-to-f)]
    (when func
      (apply func v))))


(def key-to-message
  { \w :joypad-up
    \s :joypad-down
    \a :joypad-left
    \d :joypad-right
    \l :button })

;; Call this is we get a key down event
;; translate to a state machine event and and
;; send to game

(defn got-a-key [{:keys [key-code]}]
  (let [key-char (char key-code)
        event (get key-to-message key-char)]
    (send-game-message! :game-event event)))

(def game-msg-to-func
  {:toggle-pause toggle-pause-state 
   :keypress     got-a-key
   :add-objs     add-random-jumpy!
   :kill-objs    kill-objs!} )


(defn title-message-handler []
  { :start-game (fn [])
    :leaderboards (fn [])
    :about (fn []) }
  )

(def handle-game-msg (partial handle-msg game-msg-to-func))

(defn ^:export main []
  (om/root
    page
    app-state
    {:target (. js/document (getElementById "app"))})

  ;; Only hook up the key listener if we're running for the first time
  (when-not (:done-init @app-state)
      (events/listen js/window EventType/KEYPRESS #(got-a-key-event (ckeys/translate-key-event %1)))
      (swap! app-state assoc :done-init true))

  (go-loop
    []
    (let [game (:game-state @app-state)
          msg (<! (:messages game)) ]
      (handle-game-msg msg)
      (recur)))
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














