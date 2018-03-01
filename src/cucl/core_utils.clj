(ns cucl.core-utils
  (:require
   [clj-time.core :as t]
   [clj-time.format :as f]
   [clojure.string :as str]
   [clojure.walk :refer [keywordize-keys]]
   [me.raynes.fs :as fs :refer [expand-home normalized]]
   [clojure.java.shell :refer [sh]]
   [clojure.pprint :refer [print-table pprint]]
   [clojure.reflect :refer [reflect]]
   [aero.core :refer [read-config]]))

(def ^:const home-dir (System/getProperty "user.home"))

(defn expand-and-normalized-path
  [filename]
  (-> filename
      fs/expand-home
      fs/normalized
      str))

(defn load-edn-config
  "Load the edn config from a given file."
  [config]
  (read-config (expand-and-normalized-path config)))

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

(defn keywordize-arguments
  "Convert list of arguments to hash map of keywords and values."
  [& args]
  (keywordize-keys (apply hash-map args)))

(defn filename-with-timestamp
  "Create a simple filename with a given timestamp. "
  [filename]
  (let [now (new java.util.Date)
        date-format "yyyyMMdd-hhmmss-SSSSSS"
        suffix-name (.format (java.text.SimpleDateFormat. date-format) now)]
    (format "%s-%s" filename suffix-name)))

(defn suppress-ansi
  "Suppress the ANSI color from the result of executing ssh command."
  [text]
  (-> text
      (str/replace (str \u001b "[0;30m") "")
      (str/replace (str \u001b "[1;30m") "")

      (str/replace (str \u001b "[0;31m") "")
      (str/replace (str \u001b "[1;31m") "")

      (str/replace (str \u001b "[0;32m") "")
      (str/replace (str \u001b "[1;32m") "")

      (str/replace (str \u001b "[0;33m") "")
      (str/replace (str \u001b "[1;33m") "")

      (str/replace (str \u001b "[0;34m") "")
      (str/replace (str \u001b "[1;34m") "")

      (str/replace (str \u001b "[0;35m") "")
      (str/replace (str \u001b "[1;35m") "")

      (str/replace (str \u001b "[0;36m") "")
      (str/replace (str \u001b "[1;36m") "")

      (str/replace (str \u001b "[0;37m") "")
      (str/replace (str \u001b "[1;37m") "")

      (str/replace (str \u001b "[0;38m") "")
      (str/replace (str \u001b "[1;38m") "")

      (str/replace (str \u001b "[0;39m") "")
      (str/replace (str \u001b "[1;39m") "")

      (str/replace (str \u001b "[0m") "")))

(defn get-extension
  "Extract the file extension from a given file object"
  [file-path]
  (subs (fs/extension file-path) 1))

(defn is-windows?
  "Check for the system type and return true if it is Windows based system."
  []
  (re-find #"windows" (clojure.string/lower-case (System/getProperty "os.name"))))

(defn is-linux?
  "Check for the system type and return true if it is Linux based system."
  []
  (re-find #"linux" (clojure.string/lower-case (System/getProperty "os.name"))))

(defn is-macos?
  "Check for the system type and return true if it is MacOS based system."
  []
  (re-find #"darwin" (clojure.string/lower-case (System/getProperty "os.name"))))

(defn find-binary
  "Locate a binary from the Unix/Linux PATH system."
  [binary-name]
  (if (or (is-linux?) (is-macos?))
    (let [{:keys [out exit err]} (clojure.java.shell/sh "which" binary-name)]
      (if (= exit 0)
        (clojure.string/trim-newline out)
        (throw (Exception. (format "Can't find %s in the PATH." binary-name)))))))

(defn show-members
  "Print the list of methods of a given object using reflection."
  [object]
  (print-table (sort-by :name (:members (reflect object)))))

;; From: https://gist.github.com/Sh4pe/eea52891dbeca5d0614d
(defn print-members [c]
  (->> (reflect c)
       :members
       (filter :return-type)
       (sort-by :name)
       (map #(select-keys % [:name :parameter-types :return-type]))
       (print-table)))
