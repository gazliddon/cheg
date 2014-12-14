(ns cheg.gfx)

(defn create-img [str]
  (let [img (.createElement js/document "img")]
    (.setAttribute img "src" str)
    img))

(defn mk-img [path]
  (create-img (str "MrJumpy/" path ".png")))

(def -imgs
  { :flap-f1 (mk-img "MrJumpy/Flap-f1"  )
    :flap-f2 (mk-img "MrJumpy/Flap-f2"  )
    :flap-f3 (mk-img "MrJumpy/Flap-f3"  )
    :flap-f4 (mk-img "MrJumpy/Flap-f4"  )

    :jump-f1 (mk-img "MrJumpy/Jump-f1"  )

    :run-f1  (mk-img "MrJumpy/Run-f1"   )
    :run-f2  (mk-img "MrJumpy/Run-f1"   )
    :run-f3  (mk-img "MrJumpy/Run-f1"   )
    :logo (mk-img "Logo/Logo")
   })

(defn mk-anim [v]
  (mapv #(% -imgs) v))

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





