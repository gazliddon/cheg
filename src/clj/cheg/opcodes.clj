(ns cheg.opcodes)

(def default-op-code
  {:name "FORGOT NAME"
   :write-to-mem false
   :addr-modes {}
   :op :not-implemented})

(def opcodes-base
  [ {:name "INC"
     :write-to-mem true
     :addr-modes {:zero-page   0x36
                  :zero-page-x 0xf6
                  :absolute    0xee 
                  :absolute-x  0xfe }
     :op :inc }

   {:name "JMP"
    :addr-modes {:absolute 0x4c
                 :indirect-absolute 0x64 }
    :op :jmp } ])

(def opcodes 
  (->> opcodes-base
       (map #(merge default-op-code %))
       (vec)))
