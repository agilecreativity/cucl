(defproject cucl "0.1.9"
  :description "Collection of useful Clojure libraries."
  :url "http://github.com/agilecreativity/cucl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.6.4"]
            [lein-auto "0.1.3"]
            [lein-cloverage "1.0.13"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.1"]
                 [circleci/circleci.test "0.4.2"]
                 [akvo/fs "20180904-152732.6dad3934"]
                 [lambdaisland/ansi "0.1.6"]
                 [aero "1.1.3"]
                 [alembic "0.3.2"]]
  :aliases {"test"  ["run" "-m" "circleci.test/dir" :project/test-paths]
            "tests" ["run" "-m" "circleci.test"]
            "retest" ["run" "-m" "circleci.test.retest"]})
