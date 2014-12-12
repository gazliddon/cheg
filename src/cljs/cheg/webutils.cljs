(ns cheg.webutils)

; Hook this function to requestAnimationFrame
(defn hook-to-reqanim [f]
  (f)
  (js/requestAnimationFrame
    #(hook-to-reqanim f)))

; Log to console
(defn log [a]
  (.log js/console a) )
