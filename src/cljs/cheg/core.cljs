(ns cheg.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put! <! >! chan]]
            [om.dom :as dom :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn log [  a]
  (.log js/console a) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn draw-blob [ctx x y w h col]
  (doto ctx
    (aset "fillStyle" col)
    (.moveTo x y)
    (.fillRect x y w h) ))

(defn draw-objs [ctx objs]
  (let [w 10 h 20]
    (doseq [o objs]
      (let [{:keys [x y col]} o ]
        (draw-blob ctx x y w h col)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn obj-add-vels [ {:keys [x y xv yv] :as obj} ]
  (assoc
    obj
    :x (+ x xv) :y (+ y yv)))

(defn obj-home-on-pos [cx cy {:keys [x y xv yv] :as obj} ]
  (let [ scalefn (fn [p pv cp] (+ pv (* (- p cp) 0.001))) ]
    (assoc
      obj
      :xv (scalefn x xv cx)
      :yv (scalefn y yv cy))))




(defn update-objs [player objs]
  (let [ {:keys [x y]} player ]
    (->> objs
         (mapv obj-add-vels)
         (mapv (partial obj-home-on-pos x y) ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn om-update-objs [app]
  (let [{:keys [player objs]} app ]
    (update-objs player objs)) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn render-all [ app owner node-ref ]
  (let

    [ surface  (om/get-node owner node-ref)
      width    (.-width surface)
      height   (.-height surface)
      objs     (:objs app) ]

    (doto
      (.getContext surface "2d")
      (aset "fillStyle" "grey")
      (.fillRect 0 0 width height)
      (draw-objs objs))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn header [ app owner ]
  (reify
    om/IRender
    (render [ this ]
      (dom/h1 nil (:title app)))))


(defn anim-loop [comm]
  (let [fun (fn [] (do
                    (js/requestAnimationFrame
                      #(anim-loop comm)
                      100
                      )
                    ))]
    (go 
      (>! comm :anim))
    (fun)))

(defn canvas [ app owner ]
  (reify
    om/IWillMount
    (will-mount [_]
      (log "will mount ")
      (let [comm (chan)]
        (go (while true
              (let [str (<! comm) ]
                (log str)
                )))
        (anim-loop comm)))

    om/IDidMount
    (did-mount [_]
      (render-all app owner "surface-ref"))

    om/IDidUpdate
    (did-update [_ _ _]
      (render-all app owner "surface-ref"))

    om/IRender
    (render [_]
      (dom/canvas #js {:className "canv"
                       :id        "surface"
                       :ref       "surface-ref" }))))

(defn container [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        #js {:className "container"}
        (om/build header app)
        (om/build canvas app)
        (dom/p nil "Bottom")))))

(defn page [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div
        nil
        (dom/h1 nil (:text app))
        (om/build container app)))))


(def obj {:x 30 :y 20 :yv 1 :xv -1 :col "yellow"})

(def objs2 [
            {:x 30 :y 20 :xv 0.1 :yv 0.5 :col "yellow"}
            {:x 31 :y 30 :xv 0.2 :yv 0.6 :col "blue"}
            {:x 35 :y 10 :xv 0.3 :yv 0.7 :col "black"}
            {:x 30 :y 90 :xv 0.4 :yv 0.8 :col "white"}
            ])

(defonce app-state
  (atom
    {
     :title "CHEGG"

     :player {
              :x 100
              :y 100
              :xv 0
              :yv 0
              }

     :objs [
            {:x 30 :y 20 :yv 0 :xv 0 :col "yellow"}
            {:x 31 :y 30 :yv 0 :xv 0 :col "blue"}
            {:x 35 :y 10 :yv 0 :xv 0 :col "black"}
            {:x 30 :y 90 :yv 0 :xv 0 :col "white"}
            ] } ))


(defn main []
  (om/root
    page
    app-state
    {:target (. js/document (getElementById "app"))}))
