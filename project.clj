(defproject cucl "0.1.13"
  :description "Collection of useful Clojure libraries."
  :url "http://github.com/agilecreativity/cucl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-auto "0.1.4"]
            [lein-cloverage "1.2.1"]]
  :dependencies [[aero "1.1.6"]
                 [alembic "0.3.2"]
                 [camel-snake-kebab "0.4.2"]
                 [circleci/circleci.test "0.4.3"]
                 [clj-time "0.15.2"]
                 [lambdaisland/ansi "0.1.6"]
                 [clj-commons/fs "1.5.2"]
                 [org.clojure/clojure "1.10.2-alpha2"]]
  :aliases {"test"  ["run" "-m" "circleci.test/dir" :project/test-paths]
            "tests" ["run" "-m" "circleci.test"]
            "retest" ["run" "-m" "circleci.test.retest"]})
