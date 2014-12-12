(ns cheg.obj)

(def obj-needs [:x :y :xv :yv ])

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

(defn update-objs [player objs]
  (let [px (:x player)
        py (:y player)
        homefn (fn [o] (obj-home-on-pos px py o))]
  (->> objs
       (mapv homefn)
       (mapv obj-add-vels))))


(def cols
  ["yellow"
   "red"
   "orange"
   "green"
   "blue"
   "white"
   "purple"])

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; ;; Utils / needs own file
(defn rand-range [low hi]
  (+ low  (rand (- hi low))))

(defn mkobj [x y]
  { :x x :y y :xv (rand-range -9 9) :yv (rand-range -9 9) :col (rand-nth cols)})

; (defn add-obj [x y]
;   (let [obj        (mkobj x y)
;         objs       (get-in @app-state [:game-state :objs])
;         new-objs   (into objs [obj]) ]
  
;   (swap! app-state assoc-in [:game-state :objs] new-objs))
;   "done")

; (defn add-rand-objs [n]
;   (dotimes [_ n]
;     (add-obj (rand 100) (rand 100))))

; (defn kill-objs []
;   (swap! app-state assoc-in [:game-state :objs] []))
