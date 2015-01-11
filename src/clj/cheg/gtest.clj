(ns cheg.gtest
  (:require [ cheg.emuutils :refer [make-byte
                                    make-word
                                    get-lo-hi
                                    is-neg?
                                    overflowed?]]
            [ cheg.cpu :refer :all ]
            [ cheg.opcodes :as OP ])
  (:import [cheg.cpu Cpu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord Machine
  [^Cpu cpu
   ^clojure.lang.PersistentVector mem])

(defn make-machine []
  (let [cpu (mk-cpu)
        mem (vec (take 65536 (repeat 0)))
        machine (->Machine cpu mem)]
    machine))

(defn get-byte [^Machine {:keys [mem]} ^long addr]
  (make-byte  (nth mem (make-word  addr))))

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
        (set-byte (inc addr) lo))))

(defn get-operand-addr [^Machine {:keys [cpu] :as machine}]
  (make-word (inc (:pc cpu))))

(defn get-operand-word [^Machine {:keys [cpu] :as machine}]
  (get-word machine (get-operand-addr machine)))

(defn get-operand-byte [^Machine {:keys [cpu] :as machine}]
  (get-byte machine (get-operand-addr machine)))

;; Address generation helper routines
;; Assumes PC -> at to be executed op code
(defn get-regs [^Machine {:keys [cpu]}]
  [(:a cpu)
   (:x cpu)
   (:y cpu)])

(defn addr-immediate [^Machine m]
  (get-operand-addr m))

(defn addr-zp [^Machine m]
  (get-operand-byte m))

(defn addr-abs [^Machine {:keys [mem cpu] :as m}]
  (get-operand-word m))

(defn addr-abs-x [^Machine m]
  (let [ [a x y] (get-regs m)] 
    (make-word (+ (get-operand-word m) x))))

(defn addr-abs-y [^Machine m]
  (let [ [a x y] (get-regs m)] 
    (make-word (+ (get-operand-word m) y))))

(defn addr-indirect-x [^Machine m]
  (let [[a x y] (get-regs m)
        zp-addr (make-byte  (+ x  (get-operand-byte m))) ]
    (get-word m zp-addr)))

(defn addr-indirect-abs [^Machine m]
  (let [[a x y] (get-regs m)
        zp-addr (make-byte  (+ x  (get-operand-byte m))) ]
    (->> m
         (get-operand-word)
         (get-word m))))


(defn addr-indirect-y [^Machine m]
  (let [[a x y] (get-regs m)
        zp-addr (get-operand-byte m) ]
    (get-word m (get-word m  zp-addr))))

(defn get-value [^Machine m func]
  (get-byte m (func m)))

(defn set-value [ ^Machine m func v ]
  (set-byte m (func m) v))

(defn get-value-immediate [^Machine m]  (get-value m addr-immediate))
(defn get-value-zp [^Machine m]  (get-value m addr-zp))
(defn get-value-abs [^Machine m]  (get-value m addr-abs))
(defn get-value-abs-x [^Machine m]  (get-value m addr-abs-x))
(defn get-value-abs-y [^Machine m]  (get-value m addr-abs-y))
(defn get-value-indirect-x [^Machine m]  (get-value m addr-indirect-x))
(defn get-value-indirect-y [^Machine m]  (get-value m addr-indirect-y))

(defn set-value-immediate [^Machine m ^long v]  (set-value m addr-immediate v))
(defn set-value-zp [^Machine m ^long v]  (set-value m addr-zp v))
(defn set-value-abs [^Machine m ^long v]  (set-value m addr-abs v))
(defn set-value-abs-x [^Machine m ^long v]  (set-value m addr-abs-x v))
(defn set-value-abs-y [^Machine m ^long v]  (set-value m addr-abs-y v))
(defn set-value-indirect-x [^Machine m ^long v]  (set-value m addr-indirect-x v))
(defn set-value-indirect-y [^Machine m ^long v]  (set-value m addr-indirect-y v))

(defn get-op-code [^Machine m]
  (get-byte m (get-pc m)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setting / getting vectorz
(def IRQ-VEC 0xfffe)
(def NMI-VEC 0xfffc)
(def BRK-VEC 0xfffa)

(defn get-irq-vec [^Machine m] (get-word m IRQ-VEC))
(defn get-nmi-vec [^Machine m] (get-word m NMI-VEC))
(defn get-brk-vec [^Machine m] (get-word m BRK-VEC))

(defn set-irq-vec [^Machine m v] (set-word m IRQ-VEC v))
(defn set-nmi-vec [^Machine m v] (set-word m NMI-VEC v))
(defn set-brk-vec [^Machine m v] (set-word m BRK-VEC v))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ALU stuff I guese
(defn op-ret [^long v ^Cpu cpu]
  [v cpu])

(defn std-math-op [^Cpu cpu ^long in-val func]
  (let [out-val (make-byte (func in-val))
        overflowed (overflowed? in-val out-val)]
    (op-ret
      out-val
      (-> cpu
          (set-c overflowed)
          (set-z (= out-val 0))
          (set-v overflowed)
          (set-n (is-neg? out-val))))))

(defmulti operation (fn [op-id ^Cpu cpu ^long in-val]))

(defmethod operation :jmp [_ cpu in-val]
  (op-ret in-val (assoc cpu :pc in-val)))

(defmethod operation :inc [_ cpu in-val]
  (std-math-op cpu in-val inc) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mash together alu + address generation to construct all of the stuff
;; I need for each opcode into a big fn table keyed by the opcode hex number
(defn addr-func-to-addr-rec [f]
  {:getter  (fn [^Machine m] (get-value m f))
   :setter  (fn [^Machine m ^long v] (set-value m f v)) })

(def mode-to-addr-calc-func
  {:immediate         (addr-func-to-addr-rec addr-immediate )
   :zero-page         (addr-func-to-addr-rec addr-zp )
   :absolute          (addr-func-to-addr-rec addr-abs )
   :absolute-x        (addr-func-to-addr-rec addr-abs-x )
   :absolute-y        (addr-func-to-addr-rec addr-abs-y )
   :indirect-x        (addr-func-to-addr-rec addr-indirect-x )
   :indirect-y        (addr-func-to-addr-rec addr-indirect-y )
   :indirect-absolute (addr-func-to-addr-rec addr-indirect-abs )})

(def default-jmp-tab (take 256 (repeat identity)))

(defn mk-operation [write-to-mem {:keys [getter setter]} op]
  (fn [^Machine {:keys [cpu] :as m}]
    (let [in-val (getter m)
          [out-val new-m] (operation op cpu in-val)]
      (if write-to-mem
        (setter new-m out-val)
        new-m))))

(defn mk-operations [{:keys [write-to-mem op addr-modes]}]
  (map
    (fn [amode]
      [(amode addr-modes)
       (mk-operation write-to-mem (amode mode-to-addr-calc-func) op)  ])
    (keys addr-modes)))

(defn add-op-func [tab [hex opfunc]]
  (assoc tab hex opfunc))

(defn reduce-op-funcs [tab op-code-rec]
  (reduce add-op-func tab (mk-operations op-code-rec)))

(defn mk-opcode-jmp-tab [opcode-tab]
  (reduce reduce-op-funcs default-jmp-tab opcode-tab))

(def op-code-jmp-tab (mk-opcode-jmp-tab OP/opcodes))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord Prog [^long start-address
                 ^clojure.lang.PersistentVector mem])

(defn load-prog [^Machine machine ^Prog {:keys [start-address mem]}]
  (let [machine-mem (:mem machine)
        slice-0 (take start-address machine-mem)
        slice-1 (drop (+ start-address (count mem) )machine-mem) ]

    (assoc machine :mem (vec (concat slice-0 mem slice-1)))))

(defn load-prog-set-pc [^Machine {:keys [mem cpu] :as m} ^Prog {:keys [start-address] :as prog}]
  (-> m
      (load-prog prog)
      (assoc-in :cpu (set-pc cpu start-address))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Let's test it!

; (def prog
;   (->Prog
;     0x4000
;     [0xee 0x00 0x40    
;      0x4c 0x00 0x10]))

; (-> (make-machine)
;     (load-prog-set-pc prog)
;     (decode-instrucion)
;     )

