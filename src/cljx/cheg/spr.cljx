(ns cheg.spr
  (:require 
    [cheg.gfx :as gfx]
    [cheg.obj :as obj]
    [cheg.vec :as vec]))

(defn spr-default-behaviour [o t] o)

(defn spr-default-render [r {:keys [x y spr-handle] :as o} time-now]
  (let [id (obj/obj-get-frame o time-now)
        offset (gfx/get-img-offset id spr-handle)
        [nx ny] (vec/add [x y] offset) ]
      (obj/static-img r nx ny id)
    )
  )

(def spr-defaults {:start-time 0
                   :x 0 :y 0 :xv 0 :yv 0
                   :imgs [:flap-f1]
                   :behaviour spr-default-render
                   :render spr-default-render
                   :spr-handle :top-left
                   })

(defn mkspr [i]
  (let [vals (merge spr-defaults i)]
   vals))
