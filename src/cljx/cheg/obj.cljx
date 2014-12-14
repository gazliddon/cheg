(ns cheg.obj)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Protocols
(defprotocol IRenderContext
  (clear [this col])
  (static-img [this x y img]))



(defprotocol ICreate
  (create [obj ctx]))

(defprotocol IRender
  (render [obj ctx time-now]))

(defprotocol IUpdate
  (update [obj time-delta]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def base-obj
  {:x 0 :y 0 :xv 0 :yv 0 })

(def obj-needs (keys base-obj))

(defn has-all-these? [obj needs]
  (not (some false? (map #(contains? obj %) needs))))

(defn is-obj? [obj]
  (has-all-these? obj obj-needs))

(defn has-pos? [obj]
  (has-all-these? obj [:x :y]))

(defn has-vel? [obj]
  (has-all-these? obj [:xv :yv]))


(defn get-items [o korks]
  (mapv korks o))

(defn get-vel [o]
  (get-items o [:xv :yv]))

(defn set-vel [o [xv yv]]
  (assoc o :xv xv :yv yv))

(defn get-pos [o]
  (get-items o [:x :y]))

(defn obj-home-on-pos [cx cy {:keys [x y xv yv] :as obj} ]
  (let [ scalefn (fn [p pv cp] (+ pv (* (- p cp) 0.001))) ]
    (assoc
      obj
      :xv (scalefn x xv cx)
      :yv (scalefn y yv cy))))

(defn obj-add-vels [ {:keys [x y xv yv] :as obj} game-time ]
  (assoc
    obj
    :x (+ x xv) :y (+ y yv)))

(defn mod-nth [col idx]
  (nth col (mod idx (count col))))

(defn get-obj-time [{:keys [start-time]} game-time]
  (- game-time start-time))

(defn get-frame [imgs anim-time ]
  (let [speed 0.1
        idx (/ anim-time speed) ]
    (mod-nth imgs idx)))


(defn obj-get-frame [{:keys [imgs] :as o} game-time ]
  (let [speed 0.1
        idx (/ (get-obj-time o game-time) speed)
        idx-mod (mod idx (count imgs)) ]
    (nth imgs idx-mod)))

(defn update-obj [ {:keys [behaviour start-time] :as o} game-time ]
  (let [otime (get-obj-time o game-time)]
  (-> o
      (behaviour otime)
      (obj-add-vels otime))))

(defn update-objs [ objs game-time]
  (mapv #(update-obj % game-time) objs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some obejct behaviours


