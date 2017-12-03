(defproject cucl "0.1.0"
  :description "Collection of useful Clojure libraries."
  :url "http://github.com/agilecreativity"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.5.6"]
            [lein-auto "0.1.3"]]
  :dependencies [[org.clojure/clojure "1.9.0-RC2"]
                 [clj-time "0.13.0"]
                 [me.raynes/fs "1.4.6"]
                 [clj-ssh "0.5.14"]
                 [easy-config "0.1.2"]])
