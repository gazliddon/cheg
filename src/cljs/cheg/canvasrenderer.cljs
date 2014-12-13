( ns cheg.canvasrenderer
  (:require [cheg.gfx :as gfx]
            [cheg.obj :as obj]
            ))

(enable-console-print!)

(defn canvas-renderer [surface]
  (reify
      obj/IRenderContext
      (clear [_ col]
        (let [ctx (.getContext surface "2d")
              width (.-width surface)
              height (.-height surface)]
        (doto ctx
          (aset "fillStyle" col)
          (.fillRect 0 0 width height))))  

      (static-img [_ x y img]
        (let [ctx (.getContext surface "2d")
              img-obj ( gfx/get-img img )]
          (.drawImage ctx img x y 100 100)))
      ))
