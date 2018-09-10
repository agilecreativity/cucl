(defproject cucl "0.1.5"
  :description "Collection of useful Clojure libraries."
  :url "http://github.com/agilecreativity/cucl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.5.7"]
            [lein-auto "0.1.3"]
            [lein-cloverage "1.0.11"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-time "0.14.4"]
                 [circleci/circleci.test "0.4.1"]
                 [me.raynes/fs "1.4.6"]
                 [lambdaisland/ansi "0.1.4"]
                 [aero "1.1.3"]]
  :aliases {"test"  ["run" "-m" "circleci.test/dir" :project/test-paths]
            "tests" ["run" "-m" "circleci.test"]
            "retest" ["run" "-m" "circleci.test.retest"]})
