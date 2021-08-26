(defproject cucl "0.1.14"
  :description "Collection of useful Clojure libraries."
  :url "http://github.com/agilecreativity/cucl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.8.0"]
            [lein-cloverage "1.2.2"]]
  :dependencies [[aero "1.1.6"]
                 [camel-snake-kebab "0.4.2"]
                 [circleci/circleci.test "0.5.0"]
                 [clj-time "0.15.2"]
                 [lambdaisland/ansi "0.1.6"]
                 [clj-commons/fs "1.6.307"]
                 [org.clojure/clojure "1.10.3" :scope "provided"]]
  :sign-releases false
  :aliases {"test"  ["run" "-m" "circleci.test/dir" :project/test-paths]
            "tests" ["run" "-m" "circleci.test"]
            "retest" ["run" "-m" "circleci.test.retest"]})
