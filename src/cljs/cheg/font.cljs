(ns cheg.font)


(def defaults {:align :left :pos [0 0]})

(defn get-char-record [font-data c]
  (get (:table font-data) c (:default font-data)))

(defn print-from-record [mode r]
  )

(defn print-char [mode c]
  (print-from-record (get-char-record (:font mode) c)))


(defn init-mode [mode]
  (merge (assoc mode
           :dims [0 0])
         defaults))

(defn print-str [mode str]
  (let [mode (init-mode mode)]
    (reduce print-char mode str)))
