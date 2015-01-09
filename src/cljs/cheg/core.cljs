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
    [cheg.spr :as spr]  
    [cheg.state :as ST] )
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
  (let [ game (:game-state @ST/app-state) ]
    (when-not (:paused game)
      (swap! ST/app-state assoc :game-state (update-game game (/ 1 60))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn kill-objs! []
  (swap! ST/app-state assoc-in [:game-state :objs] []))

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

(defn slider [{:keys [min-val max-val on-change value ref]} owner]
  (reify
    om/IRenderState
    (render-state [this _]
      (dom/input #js {:type "range"
                      :ref  ref
                      :min min-val
                      :max max-val
                      :value value
                      :onChange (fn [_]
                                  (let [this (om/get-node owner ref)
                                        value (js/parseInt (.-value this))]
                                    (on-change value)))})
      )))

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
                               :action ST/toggle-pause-state }))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some player test stuff
(def time-step (/ 1 60))
(def duration 2)
(def samples (/ duration time-step))
(def pos [500 600])
(def vel [0 -2000])

(def my-seq (take samples  (map #(* % time-step) (range))))

(def player-record
  {:pos pos
   :vel vel
   :start-time 0
   :max-vel [100 100]})

(defn get-renderable [t p-record-base]
  (let [pos-record (player/accelerate p-record-base t)
        [x y] (:pos pos-record) ]
    (-> pos-record
        (assoc :x x :y y)
        (spr/mkspr)
        )))

(defn jump-render [ {:keys [pos] :as o} ]
  (let [acc [(get-in @ST/app-state [:game-state :ptest :xacc])
             (get-in @ST/app-state [:game-state :ptest :yacc]) ]
        p-record-base (assoc player-record :acceleration acc)]
    (map #(get-renderable %1 p-record-base) my-seq)))

(defn pjump [] (jump-render player-record))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-stats-state [{:keys [objs player game-time ptest]}]
  (let 
    [rfunc (or (:renderable player) (fn [_] {:x 0 :y 0}))
     {:keys [pos]} (rfunc game-time)
      pstr (str "vel " (:vel player) " "
                  "state " (:state player) " " (:xacc ptest))]
    {:text (str (count objs) " objs: player: " pstr )
     :game-time game-time
     :xacc (:xacc ptest)
     :yacc (:yacc ptest)}))

(defn game-slider [ref val min max & args]
  (om/build slider
            {:ref ref
             :min-val min
             :max-val max
             :value val
             :on-change (fn [v]
                          (ST/send-game-message! :set-state args val))}))

(defn stats [ game owner ]
  (reify
    om/IInitState
    (init-state [_] (get-stats-state game))

    om/IWillReceiveProps
    (will-receive-props [ this next-props ]
      (om/update-state! owner (fn [_]( get-stats-state next-props)) ))

    om/IRenderState
    (render-state [ _ {:keys [text xacc yacc]} ]
      (dom/div nil
               (dom/p nil text)
               (game-slider "yacc" yacc 0 5000 :game-state :ptest :yacc)
               (game-slider "xacc" xacc 0 5000 :game-state :ptest :xacc)))))

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
    (spr/mkspr (assoc spr :imgs [img] :x x :y y))))

(def titles-sprs
  (mapv tospr titles))

;; Flow is:
;;   - render    : creates the canvas element with a reference we can get later
;;   - did-mount : called after dom elements created
;;                 sets the comp state to have surface and a renderer
;;                 setting the state causes did-update to be called
;;   - did-update  render everything - also triggered when @ST/app-state :gamestate is updated
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
        (render-obj-list renderer (pjump) 0)

        (let [player-renderable (player/get-renderable player game-time )]
          (render-obj-list renderer [player-renderable] game-time))))

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
                       :action (fn [] (ST/send-game-message! :add-objs )) })

            (om/build action-button
                      {:text "Create Player"
                       :action (fn [] (ST/send-game-message! :game-event :create )) })

            (om/build action-button
                      {:text "kill objs"
                       :action (fn [] (ST/send-game-message! :kill-objs )) })

            (om/build pause-button game)))))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)))))

(defn ptest-change! [my-key value]
  (reset! ST/app-state (assoc-in @ST/app-state [:game-state :ptest my-key] value)))

(defn set-state! [my-key value]
  (reset! ST/app-state (assoc-in @ST/app-state my-key value)))

(def game-msg-to-func
  {:key-press    ckeys/process-key-event 
   :add-objs     ST/add-random-jumpy!
   :kill-objs    kill-objs!
   :ptest-change ptest-change!
   :set-state    set-state!
   :game-event   ST/handle-game-event })

(defn unhandled-message [args]
  (println (str "unhandled message " args) ))

(defn handle-msg [m-to-f [m v]]
  (let [func (get m-to-f m unhandled-message)]
    (apply func v)))


(defn ^:export main []
  (om/root
    page
    ST/app-state
    {:target (. js/document (getElementById "app"))})

  (go-loop
    []
    (let [game (:game-state @ST/app-state)
          msg (<! (:messages game)) ]
      (handle-msg game-msg-to-func msg )
      (recur)))
  )

(defn bind-keys-callback []
    (events/listen
      js/window
      EventType/KEYPRESS (fn [ev] (ST/send-game-message! :key-press ev))))

(defonce keys-callback (bind-keys-callback))

