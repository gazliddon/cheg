(ns cheg.cpu
  (:require [cheg.emuutils :as U]))

;; CPU regs and flags
(defrecord Cpu
  [^long PC
   ^long A
   ^long X
   ^long Y
   ^long S
   C
   Z
   I
   D
   B
   V
   N
   ])

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

;; Flag getter / setters
(defn set-c [^Cpu cpu v]
  (assoc cpu :C v))

(defn set-z [^Cpu cpu v]
  (assoc cpu :Z v))

(defn set-i [^Cpu cpu v]
  (assoc cpu :I v))

(defn set-d [^Cpu cpu v]
  (assoc cpu :D v))

(defn set-b [^Cpu cpu v]
  (assoc cpu [:B] v))

(defn set-v [^Cpu cpu v]
  (assoc cpu :V v))

(defn set-n [^Cpu cpu v]
  (assoc cpu :N v))

(defn get-c [^Cpu cpu]
  (:C cpu ))

(defn get-z [^Cpu cpu]
  (:Z cpu ))

(defn get-i [^Cpu cpu]
  (:I cpu ))

(defn get-d [^Cpu cpu]
  (:D cpu ))

(defn get-b [^Cpu cpu]
  (:B cpu ))

(defn get-b [^Cpu cpu]
  (:N cpu ))

(defn get-pc [^Cpu cpu]
  (:PC cpu))

(defn set-pc [^Cpu cpu ^long v]
  (assoc cpu :PC (U/make-word v)))

(defn add-pc [^Cpu {:keys [PC] :as cpu} ^long v]
  (set-pc cpu (+ PC v)))

(defn get-carry-as-val [^Cpu {:keys [C]}]
  (if c 1 0))


