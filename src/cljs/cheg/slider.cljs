(ns cheg.slider
  )

; (defn handle-command-event [app owner value]
  
;   )

; (defn handle-mouse-event [app owner value]
;   )


; (defn slider [app owner]
;   (reify
;     om/IInitState
;     (init-state [_]
;       {:mouse-chan (chan)
;        })

;     om/IWillMount
;     (will-mount [_]
;       (let [mouse-chan ( om/get-state owner :mouse-chan)
;             command-chan (om/get-state owner :command-chan)]
;         (go
;           (loop []
;             (let [msg  (<! mouse-chan)]
;               (handle-mouse-event app owner msg)
;               )
;             (recur)
;             ))
;         ))

;     om/IDidMount
;     (did-mount [_]
;       (let [elem-ref (om/get-node owner "elem-ref")
;             mouse-chan (om/get-state owner :mouse-chan) ]
;         (doseq [ etype ["mousemove" "mousedown" "mouseup"] ]
;           (events/listen elem-ref etype #(put! mouse-chan %))))) 

;     om/IRenderState
;     (render-state [this _]
;       (dom/div #js {:ref "elem-ref"
;                    :style #js {:background "green"
;                                :width 100
;                                :height 20}})) 

;     ))
