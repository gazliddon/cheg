(ns cheg.core
  (:require [om.core :as om :include-macros true]
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
  (let [w 10 h 10]
    (doseq [o objs]
      (let [{:keys [x y col]} o ]
        (draw-blob ctx x y w h col)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-vels [ {:keys [x y xv yv] :as obj} ]
  (assoc
    obj
    :x (+ x xv) :y (+ y yv)))

(defn home [cx cy {:keys [x y xv yv] :as obj} ]
  (let [ scalefn (fn [p pv cp] (+ pv (* (- p cp) 0.1))) ]
    (assoc
      obj
      :xv (scalefn x xv cx)
      :yv (scalefn y yv cy))))

(defn update-objs [player objs]
  (let [ {:keys [px py]} player ]
    (vec 
      (->> objs
           (map update-vels)
           (map (partial home px py) )))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-state [{:keys [objs
                            player]} state]
  (-> state
      (assoc :objs #(update-objs player %))))

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

(defn canvas [ app owner ]
  (reify
    om/IWillMount
    (will-mount [_]
      (js/setInterval
        (fn [] (om/update! update-state)) 100)
      )

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

(defonce app-state
  (atom
    {
     :title "Chucky Egg Online"
     :center-x 100
     :center-y 100

     :player {
              :x 100
              :v 100
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
