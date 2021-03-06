(defproject cheg "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "target/generated/clj" "target/generated/cljx"]

  :test-paths ["spec/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [ring "1.3.2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring/ring-defaults "0.1.2"]
                 [compojure "1.2.0"]
                 [enlive "1.1.5"]
                 [om "0.7.3"]
                 [environ "1.0.0"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [speclj "3.1.0"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "cheg.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs" "target/generated/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles { :dev {:source-paths ["env/dev/clj"]

                   :dependencies [[figwheel "0.1.6-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.1.3"]
                                  [weasel "0.4.2"]
                                  [speclj "3.1.0"]
                                  [leiningen "2.5.0"] ]

                   :repl-options {:init-ns cheg.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]
                                  }

                   :plugins [[lein-figwheel "0.1.6-SNAPSHOT"]
                             [com.keminglabs/cljx "0.4.0" :exclusions [org.clojure/clojure]]
                             ]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :cljsbuild {:builds
                               {:app
                                {:source-paths ["env/dev/cljs"]}

                                :dev {:source-paths ["src/cljs"  "spec/cljs"]
                                      :compiler {:output-to     "resources/public/js/app_spec.js"
                                                 :output-dir    "resources/public/js/spec"
                                                 :source-map    "resources/public/js/spec.js.map"
                                                 :preamble      ["react/react.min.js"]
                                                 :externs       ["react/externs/react.js"]
                                                 :optimizations :whitespace
                                                 :pretty-print  false}
                                      :notify-command ["phantomjs"  "bin/speclj" "resources/public/js/app_spec.js"]}}}


                   :test-commands {"spec" ["phantomjs" "bin/speclj" "resources/public/js/app_spec.js"]}

                   :hooks [cljx.hooks]


                   :cljx {:builds [{:source-paths ["src/cljx"]
                                    :output-path "target/generated/clj"
                                    :rules :clj}
                                   {:source-paths ["src/cljx"]
                                    :output-path "target/generated/cljs"
                                    :rules :cljs}]}
                   }

             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [cljx.hooks leiningen.cljsbuild  ]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
