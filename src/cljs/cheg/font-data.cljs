(ns cheg.font-data)

; (def subatomic
;   :face "Subatomic Screen"
;   :size 40
;   :bold 0
;   :italic 0
;   :charset ""
;   :unicode 0
;   :stretchH 100
;   :smooth 1
;   :aa 1
;   :padding [0,0,0,0]
;   :spacing [0,0]
;   :lineHeight 54 :base 41 :scaleW 448 :scaleH 384 :pages 1 :packed 0
;   :file "Font.png"
;   :count 95
;   :table {32 {  :x 226 :y 38  :width 0  :height 0  :xoffset -2 :yoffset 41 :xadvance 27 :page 0 :chnl 0 }
;           33 {  :x 361 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           34 {  :x 316 :y 340 :width 44 :height 16 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           35 {  :x 316 :y 303 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           36 {  :x 316 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           37 {  :x 361 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           38 {  :x 316 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 54 :page 0 :chnl 0 }
;           39 {  :x 181 :y 363 :width 44 :height 16 :xoffset -2 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           40 {  :x 1   :y 154 :width 44 :height 50 :xoffset -1 :yoffset -1 :xadvance 27 :page 0 :chnl 0 }
;           41 {  :x 1   :y 103 :width 44 :height 50 :xoffset -2 :yoffset -1 :xadvance 27 :page 0 :chnl 0 }
;           42 {  :x 271 :y 340 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 52 :page 0 :chnl 0 }
;           43 {  :x 271 :y 303 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           44 {  :x 91  :y 362 :width 44 :height 16 :xoffset -2 :yoffset 33 :xadvance 20 :page 0 :chnl 0 }
;           45 {  :x 181 :y 32  :width 44 :height 10 :xoffset -2 :yoffset 19 :xadvance 47 :page 0 :chnl 0 }
;           46 {  :x 136 :y 32  :width 44 :height 10 :xoffset -2 :yoffset 33 :xadvance 20 :page 0 :chnl 0 }
;           47 {  :x 271 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           48 {  :x 271 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           49 {  :x 361 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 27 :page 0 :chnl 0 }
;           50 {  :x 316 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           51 {  :x 271 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           52 {  :x 226 :y 340 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           53 {  :x 226 :y 303 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           54 {  :x 226 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           55 {  :x 226 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           56 {  :x 226 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           57 {  :x 361 :y 155 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           58 {  :x 316 :y 155 :width 44 :height 22 :xoffset -2 :yoffset 21 :xadvance 20 :page 0 :chnl 0 }
;           59 {  :x 271 :y 155 :width 44 :height 30 :xoffset -2 :yoffset 19 :xadvance 20 :page 0 :chnl 0 }
;           60 {  :x 226 :y 155 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           61 {  :x 181 :y 340 :width 44 :height 22 :xoffset -2 :yoffset 14 :xadvance 47 :page 0 :chnl 0 }
;           62 {  :x 181 :y 303 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           63 {  :x 181 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 40 :page 0 :chnl 0 }
;           64 {  :x 181 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           65 {  :x 181 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           66 {  :x 181 :y 155 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           67 {  :x 361 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           68 {  :x 316 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           69 {  :x 271 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 40 :page 0 :chnl 0 }
;           70 {  :x 226 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           71 {  :x 1   :y 205 :width 44 :height 48 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           72 {  :x 181 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           73 {  :x 361 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           74 {  :x 316 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           75 {  :x 271 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           76 {  :x 226 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 40 :page 0 :chnl 0 }
;           77 {  :x 181 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           78 {  :x 136 :y 340 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           79 {  :x 136 :y 303 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           80 {  :x 136 :y 266 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           81 {  :x 1   :y 52  :width 44 :height 50 :xoffset -2 :yoffset 6  :xadvance 47 :page 0 :chnl 0 }
;           82 {  :x 136 :y 229 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           83 {  :x 136 :y 192 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           84 {  :x 136 :y 155 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           85 {  :x 136 :y 118 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           86 {  :x 136 :y 81  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           87 {  :x 361 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           88 {  :x 316 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           89 {  :x 271 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           90 {  :x 226 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           91 {  :x 181 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 27 :page 0 :chnl 0 }
;           92 {  :x 136 :y 44  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 47 :page 0 :chnl 0 }
;           93 {  :x 91  :y 325 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 27 :page 0 :chnl 0 }
;           94 {  :x 91  :y 308 :width 44 :height 16 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           95 {  :x 46  :y 372 :width 44 :height 10 :xoffset -2 :yoffset 33 :xadvance 47 :page 0 :chnl 0 }
;           96 {  :x 91  :y 291 :width 44 :height 16 :xoffset -2 :yoffset 7  :xadvance 27 :page 0 :chnl 0 }
;           97 {  :x 91  :y 260 :width 44 :height 30 :xoffset -2 :yoffset 12 :xadvance 40 :page 0 :chnl 0 }
;           98 {  :x 91  :y 223 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 43 :page 0 :chnl 0 }
;           99 {  :x 91  :y 192 :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 34 :page 0 :chnl 0 }
;           100 { :x 91  :y 155 :width 44 :height 36 :xoffset -2 :yoffset 6  :xadvance 40 :page 0 :chnl 0 }
;           101 { :x 91  :y 124 :width 44 :height 30 :xoffset -2 :yoffset 12 :xadvance 40 :page 0 :chnl 0 }
;           102 { :x 91  :y 87  :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           103 { :x 91  :y 44  :width 44 :height 42 :xoffset -2 :yoffset 14 :xadvance 40 :page 0 :chnl 0 }
;           104 { :x 361 :y 1   :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 40 :page 0 :chnl 0 }
;           105 { :x 316 :y 1   :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           106 { :x 1   :y 1   :width 44 :height 50 :xoffset -2 :yoffset 6  :xadvance 27 :page 0 :chnl 0 }
;           107 { :x 271 :y 1   :width 44 :height 36 :xoffset -2 :yoffset 6  :xadvance 34 :page 0 :chnl 0 }
;           108 { :x 226 :y 1   :width 44 :height 36 :xoffset -1 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           109 { :x 181 :y 1   :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 47 :page 0 :chnl 0 }
;           110 { :x 136 :y 1   :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 40 :page 0 :chnl 0 }
;           111 { :x 46  :y 341 :width 44 :height 30 :xoffset -2 :yoffset 12 :xadvance 40 :page 0 :chnl 0 }
;           112 { :x 91  :y 1   :width 44 :height 42 :xoffset -2 :yoffset 14 :xadvance 40 :page 0 :chnl 0 }
;           113 { :x 46  :y 298 :width 44 :height 42 :xoffset -2 :yoffset 14 :xadvance 40 :page 0 :chnl 0 }
;           114 { :x 46  :y 267 :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 34 :page 0 :chnl 0 }
;           115 { :x 46  :y 236 :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 40 :page 0 :chnl 0 }
;           116 { :x 46  :y 199 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 27 :page 0 :chnl 0 }
;           117 { :x 46  :y 168 :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 40 :page 0 :chnl 0 }
;           118 { :x 46  :y 137 :width 44 :height 30 :xoffset -1 :yoffset 12 :xadvance 40 :page 0 :chnl 0 }
;           119 { :x 46  :y 106 :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 47 :page 0 :chnl 0 }
;           120 { :x 46  :y 75  :width 44 :height 30 :xoffset -2 :yoffset 13 :xadvance 34 :page 0 :chnl 0 }
;           121 { :x 46  :y 32  :width 44 :height 42 :xoffset -2 :yoffset 14 :xadvance 40 :page 0 :chnl 0 }
;           122 { :x 46  :y 1   :width 44 :height 30 :xoffset -2 :yoffset 12 :xadvance 40 :page 0 :chnl 0 }
;           123 { :x 1   :y 345 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           124 { :x 1   :y 308 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 20 :page 0 :chnl 0 }
;           125 { :x 1   :y 271 :width 44 :height 36 :xoffset -2 :yoffset 7  :xadvance 34 :page 0 :chnl 0 }
;           126 { :x 1   :y 254 :width 44 :height 16 :xoffset -2 :yoffset 7  :xadvance 40 :page 0 :chnl 0 } })

