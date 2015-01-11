(ns cheg.emuutils)

(defn make-byte [v]
  (bit-and v 0xff))

(defn make-word [v]
  (bit-and v 0xffff) )

(defn get-lo-hi [^long v]
  [(bit-and 0xff v)
   (bit-and 0xff (/ v 0x100)) ])

(defn is-neg? [v]
  (bit-test v 7))

(defn is-pos? [v]
  (not (is-neg? v)))

(defn pos-to-neg? [old-val new-val]
  (and (is-neg? old-val)
       (is-pos? new-val)))

(defn neg-to-pos? [old-val new-val]
  (pos-to-neg? new-val old-val))

(defn overflowed? [old-val new-val]
  (or (neg-to-pos? old-val new-val)
      (pos-to-neg? old-val new-val)))

