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
(defprotocol IAddrMode
  (calculate-address [_ ^Machine m]))

(defprotocol IMemory
  (read-byte [_ ^long addr])
  (write-byte [_ ^long addr])
  (read-word [_ ^long addr])
  (write-word [_ ^long addr]) )

(defprotocol IOpCodeFactory
  (get-name [_])
  (get-addr-modes [_])
  (make-func [_ addr-mode]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-vec [n elem]
  (->> elem (repeat) (take n) (vec)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord Machine
  [^Cpu cpu
   ^clojure.lang.PersistentVector mem
   opcode-table]

  IMemory
  (read-byte [_ ^long addr]
    (nth (make-word addr) mem))

  (write-byte [this ^long addr ^long val]
    (let [addr (make-word addr)
          val (make-byte val)
          mem (:mem this)]
    (assoc this
           :mem (assoc mem addr val))))

  (read-word [this ^long addr]
    (let [l (read-byte this addr)
          h (read-byte this (inc addr))]
      (+ l  (* h 256))))

  (write-word [{:keys [mem] :as this} ^long addr val]
    (-> this
        (write-byte addr (bit-and val 0xff))
        (write-byte (inc addr) (/ val 0x100)))))

(defn read-op-code [^Machine {:keys [cpu] :as m}]
  (read-byte m (:PC cpu)))

(defn get-operand-addr [^Machine {:keys [cpu] :as machine}]
  (make-word (inc (:PC cpu))))

(defn read-operand-word [^Machine m]
  (read-word m (get-operand-addr machine)))

(defn read-operand-byte [^Machine m]
  (read-byte m (get-operand-addr machine)))

(defn make-machine []
  (let [cpu (mk-cpu)
        mem (vec (take 65536 (repeat 0)))
        machine (->Machine cpu mem)]
    machine))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Addressing mode calculation

(defrecord OpCodeImplementation [op-code-hex]
  IOpCodeImplementation
  (execute [_ m ]
    (func m addr-mode)))

(defrecord OpCodeFactory [name addr-modes func hex]
  IOpCodeFactory

  (get-name [_] name)

  (make-op-code-table [_]

    (let [addr-modes (map addr-mode-id->addr-mode (keys addr-modes) )
          maker (fn [addr-mode]
                  )
          ]
      )
    )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-op-code [^Machine m]
  (get-byte m (get-pc m)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setting / getting vectorz
(def IRQ-VEC 0xfffe)
(def NMI-VEC 0xfffc)
(def BRK-VEC 0xfffa)

(defn get-irq-vec [^Machine m] (read-word m IRQ-VEC))
(defn get-nmi-vec [^Machine m] (read-word m NMI-VEC))
(defn get-brk-vec [^Machine m] (read-word m BRK-VEC))

(defn set-irq-vec [^Machine m v] (write-word m IRQ-VEC v))
(defn set-nmi-vec [^Machine m v] (write-word m NMI-VEC v))
(defn set-brk-vec [^Machine m v] (write-word m BRK-VEC v))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ALU stuff I guese
;; I need for each opcode into a big fn table keyed by the opcode hex number
(def mode-to-addr-calc-func
  {:immediate    (reify IAddrMode
                   (calculate-address [_ ^Machine m ]
                     (read-operand-byte m)))

   :zero-page    (reify IAddrMode
                   (calculate-address [_ ^Machine {:keys [mem cpu]} ]
                     (->> (read-operand-byte m)
                          (read-word m))))

   :absolute     (reify IAddrMode
                   (calculate-address [_ ^Machine {:keys [mem cpu]} ]
                     (read-operand-byte m)))

   :absolute-x   (reify IAddrMode
                   (calculate-address [_ ^Machine m ]
                     (->> (read-operand-word m)
                          (+ (:X cpu))
                          (make-word))))

   :absolute-y   (reify IAddrMode
                   (calculate-address [_ ^Machine m ]
                     (->> (read-operand-word m)
                          (+ (:Y cpu))
                          (make-word))))

   :zero-page-x  (reify IAddrMode
                   (calculate-address [_ ^Machine m ]
                     (assert false)))      

   :zero-page-y  (reify IAddrMode
                   (calculate-address [_ ^Machine m ]
                     (assert false)))

   :indirect-absolute  (reify IAddrMode
                         (calculate-address [_ ^Machine m ]
                           (assert false)))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-opcode-implementations [^IOpCodeFactory opcode]
  (let [addr-modes (get-addr-modes opcode)
        tups (map #([%1 ((%1 %2) mode-to-addr-calc-func ) ])) (keys addr-modes) ]
    tups))

(defn add-opcode-functions [opcode-tab ^IOpCodeFactory opcode]
  (let [tups (get-opcode-implementations opcode)]
    (reduce (fn [t [hx func]] (assoc t hx func)) opcode-tab tups)))

(defn make-op-code-tab [opcode-factories]
  (let [ret-tab (mk-vec 256 nil) ]
    (reduce (fn [t v] (add-opcode-functions t v)) ret-tab opcode-factories)))

(def opcodes-facories
  [ (reify
     IOpCodeFactory
     (get-name [_] "INC")

     (get-addr-modes [_]
       {0x36 :zero-page   
        0xf6 :zero-page-x 
        0xee :absolute    
        0xfe :absolute-x})

     (make-func [_ addr-mode]
       (fn [^Machine m]
         (assert false))))

   (reify
     IOpCodeFactory
     (get-name [_] "JMP")

     (get-addr-modes [_]
       {0x4c :absolute
        0x64 :indirect-absolute})
     (make-func [_ addr-mode]
       (fn [^Machine m]
         (assert false))))
   ])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defrecord Prg [^long start-address
                ^clojure.lang.PersistentVector mem])

(defn load-prg [^Machine machine ^Prg {:keys [start-address mem]}]
  (let [machine-mem (:mem machine)
        slice-0 (take start-address machine-mem)
        slice-1 (drop (+ start-address (count mem) )machine-mem) ]

    (assoc machine :mem (vec (concat slice-0 mem slice-1)))))

(defn load-prg-set-pc [^Machine {:keys [mem cpu] :as m} ^Prg {:keys [start-address] :as prog}]
  (let [new-cpu (set-pc cpu start-address)]
    (-> m
        (load-prg prog)
        (assoc :cpu new-cpu)
        )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Let's test it!

(def prg
  (->Prg
    0x4000
    [0xee 0x00 0x40    
     0x4c 0x00 0x10]))

(def m
  (-> (make-machine)
      (load-prg-set-pc prg)
      ) )




