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
   [aero.core :refer [read-config]]
   [lambdaisland.ansi :refer [next-csi]]
   [alembic.still :refer [distill lein]]))

(def ^:const home-dir (System/getProperty "user.home"))

(defn expand-and-normalized-path
  [filename]
  (-> filename
      fs/expand-home
      fs/normalized
      str))

(def expand-path expand-and-normalized-path)

(defn load-edn-config
  "Load the edn config from a given file."
  [config]
  (read-config (expand-path config)))

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

(defn gen-timestamp
  "Generate the simple timestamp base on a simple date-format"
  ([]
   (gen-timestamp "yyyyMMdd-hhmmss-SSSSSS"))
  ([date-format]
   (let [now (new java.util.Date)]
     (.format (java.text.SimpleDateFormat. date-format) now))))

(defn filename-with-timestamp
  "Create a simple filename with a given timestamp. "
  [filename]
  (format "%s-%s" filename (gen-timestamp)))

;; https://en.wikipedia.org/wiki/ANSI_escape_code#CSI_sequences
(defn remove-ansi
  "Remove CSI sequences from a given text."
  [text]
  (if-let [result (next-csi (if text text ""))]
    (last result)
    text))

;; TODO: deprecated this and use remove-ansi instead?
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

(defn show-methods
  "Print the method of a given Java class.

  Examples:
  (show-methods java.util.UUID) ;; see your REPL
  (show-methods java.lang.String)"
  [clazz]
  (let [declared-methods (seq (:declaredMethods (bean clazz)))
        methods (map #(.toString %) declared-methods)]
    (doseq [m methods]
      (println m))
    methods))

(defn show-members
  "Print the list of methods of a given object using reflection.
  Examples:
  (show-members \"hello\") ;;
  (show-members java.util.UUID) ;;=> show result as table
  (show-members java.util.UUID :pretty-print? true) ;;=> pretty-print the result
  " ;;
  [object & {:keys [pretty-print?]}]
  (let [result (sort-by :name (:members (reflect object)))]
    ;; print it out to the REPL
    (if pretty-print?
      (clojure.pprint/pprint result)
      (print-table result))
    ;; and return the result so we can see it in our editor
    result))

;; From: https://gist.github.com/Sh4pe/eea52891dbeca5d0614d
(defn print-members [c]
  (->> (reflect c)
       :members
       (filter :return-type)
       (sort-by :name)
       (map #(select-keys % [:name :parameter-types :return-type]))
       (print-table)))

(defn list-files
  "List files from a given directory matching specific patterns.
  Example Usage:
  (list-file \"~/projects/awesome\" {:patterns \"*.{clj,md}\"})
  (list-file \"~/projects/awesome\" {:patterns \"*.*\"})"
  [base-dir & [{:keys [patterns]}]]
  (let [grammar-matcher (.getPathMatcher
                         (java.nio.file.FileSystems/getDefault)
                         (str "glob:" patterns))]
    (->> (expand-and-normalized-path base-dir)
         clojure.java.io/file
         file-seq
         (filter #(.isFile %))
         (filter #(.matches grammar-matcher (.getFileName (.toPath %))))
         (mapv #(.getAbsolutePath %)))))

(defn remove-nil
  "Remove nil value from a given map.

  Example:
  (remove-nil {:a 1 :b nil :c 2}) ;;=> {:a 1, :c 2}"
  [input-map]
  (reduce (fn [m [k v]]
            (if (nil? v)
              m
              (assoc m k v)))
          {}
          input-map))

#_ (remove-nil {:a 1 :b nil :c 2})

(defn add-project-dependency
  "Add project dependency at runtime via alembic.

  Example:
  (add-project-dependency :hara/io.file \"3.0.5\")
  (add-project-dependency \"hara/io.file\" \"3.0.5\")
  (add-project-dependency '[hara/io.file \"3.0.5\"])
  ;; Then require it normally
  (require '[hara.io.file :as hf])"
  ([dep-vector]
   (let [[lib-name lib-version] dep-vector]
     (add-project-dependency lib-name lib-version)))
  ([lib-name lib-version]
   (let [dep-name (symbol lib-name)
         dep-version (name lib-version)]
     (alembic.still/distill [dep-name dep-version]))))

(comment
  ;; Example Session
  (add-project-dependency :hara/io.file "3.0.5")
  (add-project-dependency "hara/io.file" "3.0.5")
  (add-project-dependency '[hara/io.file "3.0.5"])
  (require '[hara.io.file :as hf])
  (hf/list "."))
