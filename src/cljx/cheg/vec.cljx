(ns cheg.vec
  (:refer-clojure :exclude [max min]))

(defn add [ [ax ay] [bx by] ] [ (+ ax bx ) (+ ay by )])
(defn sub [ [ax ay] [bx by] ] [ (- ax bx ) (- ay by )])
(defn mul [ [ax ay] [bx by] ] [ (* ax bx ) (* ay by )])
(defn div [ [ax ay] [bx by] ] [ (/ ax bx ) (/ ay by )])
(defn vmod [ [ax ay] [bx by] ] [ (mod ax bx ) (mod ay by )])

(defn max [ [ax ay] [bx by] ]
  [(if (> ax bx) ax bx)
   (if (> ay by) ay by) ])

(defn min [ [ax ay] [bx by] ]
  [(if (< ax bx) ax bx)
   (if (< ay by) ay by) ])


(defn add-s [v s] (add v [s s]))
(defn sub-s [v s] (sub v [s s]))
(defn mul-s [v s] (mul v [s s]))
(defn div-s [v s] (div v [s s]))


