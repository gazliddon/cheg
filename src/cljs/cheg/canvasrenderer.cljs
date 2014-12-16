( ns cheg.canvasrenderer
  (:require [cheg.gfx :as gfx]
            [cheg.obj :as obj]
            ))

(defn canvas-renderer [surface]
  (let [ctx    (.getContext surface "2d")
        width  (.-width surface)
        height (.-height surface) ]

    (reify
      obj/IRenderContext
      (clear [_ col]
        (doto ctx
          (aset "fillStyle" col)
          (.fillRect 0 0 1280 720)))  

      (static-img [_ x y img]
        (let [i (gfx/get-img img)
              iw (.-width i)
              ih (.-height i)]
          (.drawImage ctx i x y iw ih))))))


