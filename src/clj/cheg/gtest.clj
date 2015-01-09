(ns cheg.gtest)


;; CPU regs and flags
(defrecord Cpu
  [^long pc
  ^long a
  ^long x
  ^long y
  ^long s

  C
  Z
  I
  D
  B
  V
  N ])


;; Make a cpu convinience func

(defn mk-cpu []
  (->Cpu
    0
    0
    0
    0
    0xff
    false
    false
    false
    false
    false
    false
    false
    ))

(defrecord Machine
  [^Cpu cpu
   ^clojure.lang.PersistentVector mem])

;; Memory access
(defn make-byte [v]
  (bit-and v 0xff))

(defn make-word [v]
  (bit-and v 0xffff) )

(defn get-byte [^Machine {:keys [mem]} ^long addr]
  (make-byte  (nth mem (make-word  addr))))

(defn get-lo-hi [^long v]
  [(bit-and 0xff v)
   (bit-and 0xff (/ v 0x100)) ])

(defn get-word [^Machine {:keys [mem] :as machine} ^long addr]
  (let [ lo (get-byte machine addr )
         hi (get-byte machine (inc addr)) ]
    (+ lo  (* 0x100 hi))))

(defn set-byte [^Machine {:keys [mem] :as machine} ^long addr ^long v]
  (assoc machine :mem (assoc (make-word addr ) (make-byte  v))))

(defn set-word [^Machine machine ^long addr ^long v]
  (let [[lo hi] (get-lo-hi v)]
    (-> machine
        (set-byte addr lo)
        (set-byte (inc addr) lo))
    )
  )

(defn get-word-operard [^Machine {:keys [cpu] :as machine}]
  (get-word machine (inc (:pc cpu))))

(defn get-byte-operand [^Machine {:keys [cpu] :as machine}]
  (get-byte machine (inc (:pc cpu))))

;; Flag getter / setters
(defn set-c [ ^Machine m  v ]
  (assoc-in m [:cpu :C] v))

(defn set-z [ ^Machine m  v ]
  (assoc-in m [:cpu :Z] v))

(defn set-i [ ^Machine m  v ]
  (assoc-in m [:cpu :I] v))

(defn set-d [ ^Machine m  v ]
  (assoc-in m [:cpu :D] v))

(defn set-b [ ^Machine m  v ]
  (assoc-in m [:cpu :B] v))

(defn set-v [ ^Machine m  v ]
  (assoc-in m [:cpu :V] v))

(defn set-n [ ^Machine m  v ]
  (assoc-in m [:cpu :N] v))


(defn get-c [ ^Machine m ]
  (get-in m [:cpu :C] ))

(defn get-z [ ^Machine m  ]
  (get-in m [:cpu :Z] ))

(defn get-i [ ^Machine m  ]
  (get-in m [:cpu :I] ))

(defn get-d [ ^Machine m  ]
  (get-in m [:cpu :D] ))

(defn get-b [ ^Machine m  ]
  (get-in m [:cpu :B] ))

(defn get-b [ ^Machine m  ]
  (get-in m [:cpu :N] ))

(defn get-pc [^Machine {:keys [cpu]}]
  (:pc cpu))

(defn set-pc [^Machine machine ^long v]
  (assoc-in machine [:cpu :pc] (make-word v)))

(defn add-pc [^Machine machine ^long v]
  (set-pc machine (+ (get-pc machine) v)))



(defrecord Prog [^long start-address
                 ^clojure.lang.PersistentVector mem])

;; Setting / getting vectorz
(def IRQ-VEC 0xfffe)
(def NMI-VEC 0xfffc)
(def BRK-VEC 0xfffa)

(defn get-irq-vec [^Machine machine] (get-word machine IRQ-VEC))
(defn get-nmi-vec [^Machine machine] (get-word machine NMI-VEC))
(defn get-brk-vec [^Machine machine] (get-word machine BRK-VEC))

(defn set-irq-vec [^Machine machine v] (set-word machine IRQ-VEC v))
(defn set-nmi-vec [^Machine machine v] (set-word machine NMI-VEC v))
(defn set-brk-vec [^Machine machine v] (set-word machine BRK-VEC v))


(defn set-pc [^Machine machine addr]
         (assoc-in machine [:cpu :pc] addr) )

(defn load-prog [^Machine machine ^Prog {:keys [start-address mem]}]
  (let [machine-mem (:mem machine)
        slice-0 (take start-address machine-mem)
        slice-1 (drop (+ start-address (count mem) )machine-mem)
        new-mem (concat slice-0 mem slice-1) ]

    (assoc machine :mem (vec new-mem)))
  )

(defn load-prog-set-pc [^Machine machine ^Prog {:keys [start-address] :as prog}]
  (-> machine
      (load-prog prog)
      (set-pc start-address)))

(defn make-machine []
  (let [cpu (mk-cpu)
        mem (vec (take 65536 (repeat 0)))
        machine (->Machine cpu mem)]
    machine))

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

(defmulti opcode (fn [^long op ^Machine machine ^long operand] [op]))

(defmethod opcode 0xee
  [_ {:keys [mem cpu] :as machine } operand]
  (let [val (get-word machine operand)
        new-val (make-byte (inc val ))
        overflow (overflowed? val new-val) ]

    (-> machine
        (set-c overflow)
        (set-z (= new-val 0))
        (set-v overflow)
        (set-n (is-neg? new-val))
        (set-word operand new-val))))


(def opcodes
  { 0xee {:mode :absolute
          :cycles 4
          :mnemomic "INC"
          :operand-size :word
          :func (fn [m o] (o 0xee m o)) }
   })

(defn get-op-code [^Machine m]
  (get-byte m (get-pc m)))

(defn get-operand [^Machine m {:keys [operand-size]} ]
  (case :operand-size
    :word [(get-word-operard m) 2] 
    :byte [(get-byte-operand m) 1] 
    (assert false)))

;; Execute instructions
(defn decode-instrucion [^Machine {:keys [cpu mem] :as m} ]
  (let [op-rec (get-op-code m)
        [operand size] (get-operand m op-rec ) ]
    (println "HERE I AM")
    (-> m
        (add-pc size)
        ((:func op-rec ) operand))))

(defn print-word [^Machine m ^long addr]
  (println (get-word m addr)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Let's test it!

(def prog
  (->Prog
    0x4000
    [0xee 0x00 0x40    
     0x20 0x00 0x10]))

(-> (make-machine)
    (load-prog-set-pc prog)
    (decode-instrucion)
    (print-word 0x1000))

