(ns cucl.core-utils
  (:require
   [clj-time.core :as t]
   [clj-time.format :as f]
   [clojure.edn :as edn :refer [read-string]]
   [clojure.string :as str]
   [easy-config.core :as ez]
   [me.raynes.fs :as fs :refer [expand-home normalized]]))

(def ^:const home-dir (System/getProperty "user.home"))

(defn expand-and-normalized-path
  [filename]
  (-> filename
      fs/expand-home
      fs/normalized
      str))

(defn load-edn-config
  "Load the edn config from a given file."
  [config-file]
  (read-string (slurp (expand-and-normalized-path config-file))))

(defn filter-non-nil
  "Filter out the maps value that are not nil"
  [item]
  (into {} (filter val item)))

(defn parse-int
  "Parse a string to a number when possible"
  [number-string]
  (. Integer parseInt number-string))

(defn str-to-int
  "Convert string input to a number if it is not already an integer."
  [number]
  (if (integer? number)
    number
    (parse-int number)))

(defn map-vals
  "Given a function and a map, returns the map resulting from applying
  the function to each value.

  e.g. (map-vals inc {:a 1 :b 2 :c 3}) ;;=> {:a 2, :b 3, :c 4}
  "
  [f m]
  (zipmap (keys m) (map f (vals m))))

(defn map-keys
  "Given a function and a map, returns the map resulting from applying
  the function to each key.

  e.g. (map-keys name {:a 1 :b 2 :c 3}) ;;=> {\"a\" 1, \"b\" 2, \"c\" 3}
  "
  [f m]
  (zipmap (map f (keys m)) (vals m)))

(defn custom-date-format
  "Reformat the result from the database in the preferred format."
  [date-time]
  (f/unparse (f/with-zone
               (:date-time-no-ms f/formatters)
               ;; (:date-hour-minute-second f/formatters)
               (t/time-zone-for-id "America/New_York"))
             date-time))

(defn pretty-format
  [arg]
  (cond
    ;; If it is a date then format it like a date
    (= org.joda.time.DateTime (type arg))
    (custom-date-format arg)

    ;; Return the normal str function
    :else (str arg)))

(defn quote-fn
  "Quote input if they are all numbers."
  [text]
  (if (re-find #"^\d+?$" (.trim text))
    identity
    nil))

(defn resources-path
  "Return config file path from resources directory."
  [config]
  (str/join
   (java.io.File/separator)
   (list (System/getProperty "user.dir")
         "resources"
         config)))

(defn slurp-file
  "Read content of the file that understand `~` for HOME in Unix/Linux system."
  [input-file]
  (slurp (expand-and-normalized-path input-file)))
