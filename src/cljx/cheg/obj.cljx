(ns cheg.obj)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Protocols
(defprotocol IRenderContext
  (clear [ctx col])
  (static-img [ctx x y img]))

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

(defn obj-home-on-pos [cx cy {:keys [x y xv yv] :as obj} ]
  (let [ scalefn (fn [p pv cp] (+ pv (* (- p cp) 0.001))) ]
    (assoc
      obj
      :xv (scalefn x xv cx)
      :yv (scalefn y yv cy))))

(defn obj-add-vels [ {:keys [x y xv yv] :as obj} ]
  (assoc
    obj
    :x (+ x xv) :y (+ y yv)))

(defn obj-home-on-pos [cx cy {:keys [x y xv yv] :as obj} ]
  (let [ scalefn (fn [p pv cp] (+ pv (* (- cp p) 0.001))) ]
    (assoc
      obj
      :xv (scalefn x xv cx)
      :yv (scalefn y yv cy))))

(defn obj-get-frame [{:keys [ start-time imgs ]} time-now ]
  (let [speed 0.1
        tm-passed (- start-time time-now)
        idx (/ tm-passed speed)
        idx-mod (mod idx (count imgs)) ]
    (nth imgs idx-mod)))

(defn update-objs [{:keys [x y]} objs]
  (let [ homefn (fn [o] (obj-home-on-pos x y o))]
  (->> objs
       (mapv homefn)
       (mapv obj-add-vels))))

(defn create-obj [obj]
  (if (satisfies? ICreate obj)
    (create obj)
    obj))

(defn render-objs [render-ctx objs]
  (when (satisfies? IRenderContext render-ctx)
    (doseq [ o (filter #(satisfies? IRender %) objs)]
      (render o render-ctx 0))))

(defn build-obj [rfobj]
  (-> base-obj
      (create-obj)))

