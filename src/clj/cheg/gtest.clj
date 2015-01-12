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

(defprotocol IMachine
  (read-byte [_ ^long addr])
  (write-byte [_ ^long addr ^long v])
  (inc-cycles [_])
  (fetch-byte-from-pc [_])
  (fetch-word-from-pc [_])
  
  (fetch-instruction [_ ])
  (decode-instruction [_])
  (load-data-bus [_])
  (write-address-bus [_])
  (address-bus->pc [_]))

(defrecord Machine
  [^Cpu cpu
   ^clojure.lang.PersistentVector mem
   opcode-table]

  IMachine

  (read-byte [m addr ]
    (->>
      (:mem m)
      (nth m)
      (make-byte)))

  (write-byte [{:keys [mem] :as m} addr v]
    assoc m :mem (assoc mem addr v)) 

  (inc-cycles [{:keys [cpu ] :as m}]
    (assoc m :cpu (+ 1 (:cpu cycles))))

  (fetch-instruction [{:keys [cpu mem] :as m}]
    (let [{:keys [address-bus]} cpu
          op-hex (read-byte (:pc cpu))
          op-code (op-hex opcode-table)
          operand (get-operand op-code)
          op-size (get-operand-size op-code) ]
      (assoc m
             (assoc cpu
                    :op-hex op-hex
                    :op-code op-code
                    :operand operand
                    :pc (+ 1 op-size (:pc cpu))
                    :cycles (+ (:cycles cpu) 1 op-size)))))

  (decode-instruction [{:keys [cpu] :as m}]
    (let [])
    )
  )

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

(defn get-op-code [^Machine {:keys [cpu] :as m}]
  (get-word m (get-pc cpu)))

(defn get-operand-addr [^Machine {:keys [cpu] :as machine}]
  (make-word (inc (:pc cpu))))

(defn get-operand-word [^Machine {:keys [cpu] :as machine}]
  (get-word machine (get-operand-addr machine)))

(defn get-operand-byte [^Machine {:keys [cpu] :as machine}]
  (get-byte machine (get-operand-addr machine)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Addressing mode calculation
(defprotocol IOpCodeFactory
  (get-name [_])
  (make-op-table [_]))

(defprotocol IOpCodeImplementation
  (get-operand-size [_])
  (execute [_ ^Machine m]))

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

  (make-op-code [_ addr-mode-id]
    (let [addr-mode ()]
      (assert addr-mode)
      (reify
        IOpCodeImplementation
        (execute [_ ^Machine m]
          (func m addr-modes))))
    ))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn std-math-op [^Machine {:keys [cpu] :as m} func]
  (let [out-val (make-byte (func (:data-bus cpu)))
        overflowed (overflowed? in-val out-val)]
    (-> m
        (assoc :cpu  (-> cpu
                         (inc-cycles)
                         (set-data-bus out-val)
                         (set-c overflowed)
                         (set-z (= out-val 0))
                         (set-v overflowed)
                         (set-n (is-neg? out-val)))))))

(def opcodes
  [ 
   {:name       "INC"
     :addr-modes {:zero-page   0x36
                  :zero-page-x 0xf6
                  :absolute    0xee 
                  :absolute-x  0xfe }

     :func      (fn [m addr-mode]
                  (-> m
                      (fetch-instruction addr-mode)
                      (set-address-bus addr-mode)
                      (fetch-from-address-bus)
                      (std-math-op inc)
                      (write-to-address-bus)))}

   {:name "JMP"
    :addr-modes {:absolute 0x4c
                 :indirect-absolute 0x64 }
    :func       (fn [m addr-mode]
                  (-> m
                      (fetch-opcode addr-mode)
                      (load-address-bus addr-mode)
                      (address-bus->pc)))}

   ])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn r-immediate []
  )

(defn r-zero-page []
  )

(defn r-absolute  []
  (reify IAddrMode
    (get-addr [this m]
      (get-operand-word m))))

(defn r-absolute-x  []
  )

(defn r-absolute-y  []
  (reify IAddrMode
    (get-addr [this {:keys [cpu] :as m}]
      (->>(get-addr r-absolute m) 
                    (+ (:y cpu))))))

(defn r-zero-page-indirect [pre-add pos-add]
  (reify IAddrMode
    (get-addr [this {:keys [cpu] :as m}]
      (->>
        (get-operand-byte m) 
        (+ pre-add)
        (get-word  m)
        (+ pos-add)))))

(defn r-zero-page-x  []
  (reify IAddrMode
    (get-addr [this {:keys [cpu] :as m}]
      (get-addr (r-zero-page-indirect (:x cpu) 0)))))

(defn r-zero-page-y  []
  (reify IAddrMode
    (get-addr [this {:keys [cpu] :as m}]
      (get-addr (r-zero-page-indirect 0 (:y cpu))))))

(defn r-indirect-absolute  []
  (reify IAddrMode
    (get-addr [this m]
      (->>
        (get-operand-word m)
        (get-word m)))))


(defn last-operand [^Machine m ^long post-add]
  (+ post-add 
     (get-in m [:cpu :operand])))

(defn indirect-last-operand [{:keys [cpu] :as m} ^long pre-add ^long post-add]
  (let [addr (:operand cpu)]
    (get-byte m (+ pos-add addr) )))

(def mode-to-addr-calc-func
  {:immediate    (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-byte-from-pc m))
                   (get-addr [_ m]
                     (last-operand m 0)))

   :zero-page    (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-byte-from-pc m))
                   (get-addr [_ m]
                     (indirect-last-operand  m 0 0)))

   :absolute     (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-word-from-pc m))
                   (get-addr [_ m]
                     (last-operand m 0)))

   :absolute-x   (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-word-from-pc m))
                   (get-addr [_ {:keys [cpu] :as m}]
                     (last-operand m (:x cpu))))

   :absolute-y   (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-word-from-pc m))
                   (get-addr [_ {:keys [cpu] :as m}]
                     (last-operand (:y cpu))))


   :zero-page-x  (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-byte-from-pc m))
                   (get-addr [_ {:keys [cpu] :as m}]
                     (indirect-last-operand m (:x cpu) 0)))      

   :zero-page-y  (reify IAddrMode
                   (get-operand [_ m]
                     (fetch-byte-from-pc m))
                   (get-addr [_ {:keys [cpu] :as m}]
                     (indirect-last-operand m 0 (:y cpu) )))

   :indirect-absolute  (reify IAddrMode
                         (get-operand [_ m]
                           (fetch-byte-from-pc m))
                         (get-addr [_ m]
                           (indirect-last-operand m 0 0)))})

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
;; I need for each opcode into a big fn table keyed by the opcode hex number



(defn addr-func-to-addr-rec [f]
  (reify
    IAddrMode


    )
  {:getter  (fn [^Machine m] (get-value m f))
   :setter  (fn [^Machine m ^long v] (set-value m f v)) })

(def mode-to-addr-calc-func
  {:immediate          (addr-func-to-addr-rec addr-immediate )
   :zero-page          (addr-func-to-addr-rec addr-zp )
   :absolute           (addr-func-to-addr-rec addr-abs )
   :absolute-x         (addr-func-to-addr-rec addr-abs-x )
   :absolute-y         (addr-func-to-addr-rec addr-abs-y )
   :zero-page-x        (addr-func-to-addr-rec addr-indirect-x )
   :zero-page-y        (addr-func-to-addr-rec addr-indirect-y )
   :indirect-absolute  (addr-func-to-addr-rec addr-indirect-abs )})

(defn mk-vec [n elem]
  (->> elem (repeat) (take n) (vec)))

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

(def cpu (:cpu m))
(def pc (get-pc cpu))
(get-op-code m)
(decode-instrucion m)




