(ns cheg.gfx
  (:require [cheg.vec :as vec]) )

(defn get-dims [i]
  [(.-width i) (.-height i)])

(def offsets 
  {:top-left      [0.0 0.0]
   :top-middle    [0.5 0.0]
   :top-right     [1.0 0.0]
   :middle-left   [0.0 0.5]
   :centered      [0.5 0.5]
   :middle-right  [1.0 0.5]
   :bottom-left   [0.0 1.0]
   :bottom-middle [0.5 1.0]
   :bottom-right  [1.0 1.0] }
  )

(defn get-offset [dims mode]
  (let [scale (get offsets mode [0 0])
        offset (vec/mul scale dims)]
    (vec/sub [0 0] offset)))


(defn create-img [id]
  )

(defn mk-img [id]
  (. js/document (getElementById id)))

(def -imgs
  {
   :flap-f1     (mk-img  "flap-f1"  )
   :flap-f2     (mk-img  "flap-f2"  )
   :flap-f3     (mk-img  "flap-f3"  )
   :flap-f4     (mk-img  "flap-f4"  )

   :jump-f1     (mk-img  "jump-f1"  )

   :run-f1      (mk-img  "run-f1"   )
   :run-f2      (mk-img  "run-f2"   )
   :run-f3      (mk-img  "run-f3"   )

   :logo        (mk-img  "logo")

   :pickup      (mk-img  "pickup")
   :platform1   (mk-img  "platform1")
   :platform2   (mk-img  "platform2")
   :platform3   (mk-img  "platform3")

   :background1 (mk-img  "background1") 
   :background2 (mk-img  "background2") 
   :sky1        (mk-img  "sky1")
   :sky2        (mk-img  "sky2") 
   })

(def -anims 
  { :flap [:flap-f1 :flap-f2 :flap-f3 :flap-f4]
    :jump [:jump-f1]
    :run  [:run-f1 :run-f2 :run-f3]
    })

(defn get-img [id]
  (id -imgs))

(defn get-anim [id]
  (id -anims))

(defn get-rand-anim []
  (get-anim (rand-nth (keys -anims))))

(defn get-img-dims [id]
  (get-dims  (get-img id)))

(defn get-img-offset [id mode]
  (get-offset (get-dims (get-img id) ) mode))

