(ns cheg.vec2)

(defrecord Vec2 [^float x ^float y])

(defn dot [^Vec2 {:keys [x y]} ^Vec2 b]
  (+ (* x (:x b)) (* y (:y b))))

(defn vec-sub
  [{:keys [x y]} b]
  (Vec2. (- x (:x b)) (- y (:y b)) ))

(defn scalar-sub
  [{:keys [x y]} b]
   (Vec2. (- x b) (- y b)))

(defn vec-add
  [{:keys [x y]} b]
  (Vec2. (+ x (:x b)) (+ y (:y b)) ))

(defn scalar-add
  [{:keys [x y]} b]
   (Vec2. (+ x b) (+ y b)))

(defn vec-mul
  [{:keys [x y]} b]
  (Vec2. (* x (:x b)) (* y (:y b)) ))

(defn scalar-mul
  [{:keys [x y]} b]
  (Vec2. (* x b) (* y b)))

(defn vec-div
  [{:keys [x y]} b]
  (Vec2. (/ x (:x b)) (/ y (:y b)) ))

(defn scalar-div
  [{:keys [x y]} b]
  (Vec2. (/ x b) (/ y b)))
