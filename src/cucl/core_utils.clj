(ns cucl.core-utils
  (:require
   [clojure.edn :as edn]
   [me.raynes.fs :as fs]
   [clj-ssh.cli]
   [clj-ssh.ssh :as ssh]
   [clj-ssh.agent :as agt]
   [clojure.string :as str]
   [easy-config.core :as ez]))

(defn expand-and-normalized-path
  [filename]
  (-> filename
      fs/expand-home
      fs/normalized))

(defn load-edn-config
  "Load the edn config from a given file."
  [config-file]
  (edn/read-string (slurp (expand-and-normalized-path config-file))))

(def ^:const home-dir (System/getProperty "user.home"))

#_
(defn ssh-key-path
  "The path the private key usually `~/.ssh/id_rsa`"
  ([]
   (ssh-key-path home-dir))
  ([base-dir]
   (ssh-key-path base-dir (str/join
                           (java.io.File/separator)
                           (list ".ssh"
                                 "id_rsa"))))
  ([base-dir file-path]
   (str (expand-and-normalized-path (str base-dir (java.io.File/separator) file-path)))))

(defn- custom-private-key-path
  "Return the full path to the private key.
  Normally '~/.ssh/id_rsa'"
  ([]
   (custom-private-key-path "id_rsa"))
  ([ssh-private-key]
   (ssh-key-path "~/.ssh" ssh-private-key)))

(defn session-config
  "Create configuration maps from a given edn file."
  [config-file]
  (let [config-map (load-edn-config config-file)
        {:keys [ssh-user ssh-host ssh-port ssh-private-key]} config-map
        private-key-path (custom-private-key-path ssh-private-key)]
    {:ssh-user ssh-user
     :ssh-port ssh-port
     :ssh-host ssh-host
     :private-key-path private-key-path
     }))

(defn create-session
  "Create and return the ssh connection based on the given config mapping.
  Default to QA configuration."
  [config-file]
  (let [{:keys [ssh-user
                ssh-host
                ssh-port
                private-key-path]} (session-config config-file)]
    (let [agt (ssh/ssh-agent {:use-system-ssh-agent false})]
      (ssh/add-identity agt {:user ssh-user
                             :private-key-path private-key-path
                             :port ssh-port})
      (let [session (ssh/session agt
                                 ssh-host
                                 {:strict-host-key-checking :no
                                  :port ssh-port})]
        session))))

(defn ssh-execute
  "Run the command on the remote server via the ssh wrapper."
  [config-file command]
  (let [session (create-session config-file)]
    (ssh/with-connection session
      (println (format "Input command : %s" command))
      (let [result (ssh/ssh session {:in command})]
        (if (= 0 (:exit result))
          (println (result :out))
          (println (format "Problem running the command '%s' \n exit-code: %s"
                           command (:exit result))))))))

(defn ssh-execute-session
  "Run the command on the remote server using the active session"
  [session command]
  (ssh/with-connection session
    (println (format "Input command : %s" command))
    (let [result (ssh/ssh session {:in command})]
      (if (= 0 (:exit result))
        (println (result :out))
        (println (format "Problem running the command '%s' \n exit-code: %s"
                         command (:exit result)))))))

;; TODO: make code more generic for general usage
(defn scp-execute
  "Execute the scp command using the given session."
  [session]
  (ssh/with-connection session
    (let [channel (ssh/ssh-sftp session)]
      (ssh/with-channel-connection channel
        (ssh/sftp channel {} :cd "/opt/home/bchoomnuan")
        (ssh/sftp channel {} :put "/Users/bchoomnuan/hello" "hello")))
    ;; Can we re-use the session?
    (ssh-execute-session session "ls -alt && pwd && chmod +x ./hello && ./hello")))

#_
;; Note: this is working code!
(let [session (create-session "~/Dropbox/sba/sba-app-qa-read-write.edn")]
  (scp-execute session))
