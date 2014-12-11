(ns leiningen.foo
  (:require [watchtower.core :as W]
            ))

; (start-watch [{:path "/scr/clj"
;                :event-types [:create :modify :delete]
;                :bootstrap (fn [path] (println "Starting to watch " path))
;                :callback (fn [event filename] (println event filename))
;                :options {:recursive true}}])


(defn nowt [])
(defn foo [project & args]
  (do
    (println "Starting to watch it 2!")
    (W/watcher ["src/clj/tester2/"]
               (W/rate 50)
               (W/on-change #(println "Files have changed! " %)))
    (while true
      (Thread/sleep 1000)
      )

    ))



