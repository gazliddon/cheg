(ns cheg.vec)

(def zipvecs (partial map vector))

(defn mk-vec-op [f]
  (let [func (fn [[a b]] (f a b)) ]
    (fn [a b]
      (mapv func (zipvecs a b)))))

(defn mk-vec-scalar-op [f]
  (fn [a b]
    ((mk-vec-op f) a (repeat b))))

(def add (mk-vec-op +))
(def sub (mk-vec-op -))
(def mul (mk-vec-op *))
(def div (mk-vec-op /))

(def add-s (mk-vec-scalar-op +))
(def sub-s (mk-vec-scalar-op -))
(def mul-s (mk-vec-scalar-op *))
(def div-s (mk-vec-scalar-op /))


