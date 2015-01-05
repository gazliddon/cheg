(ns cheg.vec2overloads
  (:require
    [cheg.vec2 ] ))

; ;; Multi method dispatch for op overloading
; (derive cheg.vec2.Vec2   ::vec2 )
; (derive java.lang.Double ::float)
; (derive java.lang.Long   ::int)

; (defmulti !+ (fn [x y] [(class x) (class y)]))
; (defmulti !- (fn [x y] [(class x) (class y)]))
; (defmulti !* (fn [x y] [(class x) (class y)]))
; ; (defmulti !/ (fn [x y] [(class x) (class y)]))

; (defmethod !+ [::vec2 ::vec2]  [a b] (vec-add a b))
; (defmethod !+ [::vec2 ::float] [a b] (scalar-add a b))
; (defmethod !+ [::vec2 ::int]   [a b] (scalar-add a b))

; (defmethod !- [::vec2 ::vec2]  [a b] (vec-sub a b))
; (defmethod !- [::vec2 ::float] [a b] (scalar-sub a b))
; (defmethod !- [::vec2 ::int]   [a b] (scalar-sub a b))

; (defmethod !* [::vec2 ::vec2]  [a b] (vec-mul a b))
; (defmethod !* [::vec2 ::float] [a b] (scalar-mul a b))
; (defmethod !* [::vec2 ::int]   [a b] (scalar-mul a b))

; ; (defmethod !\ [::vec2 ::vec2]  [a b] (vec-div a b))
; ; (defmethod !\ [::vec2 ::float] [a b] (scalar-div a b))
; ; (defmethod !\ [::vec2 ::int]   [a b] (scalar-div a b))

