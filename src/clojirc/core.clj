(ns clojirc.core
  (:require [clojure.core.async :as async]
            [clojure.java.io :refer [input-stream output-stream reader writer]]
            [clojure.string :as str]
            [instaparse.core :as insta])
  (:import (java.net Socket)))

;;;
;;; Private functions.
;;;

;; Parsing of raw IRC messages into a nice map format.

(def irc-parser
  (insta/parser (slurp "resources/irc-parser.ebnf")))

(defn parse-message
  [msg]
  (let [parsed (irc-parser msg)
        reduced (reduce (fn [m [e & [r]]]
                          (assoc m e r))
                        {} (irc-parser msg))]
    (when (insta/failure? parsed)
      (spit "failures.txt" msg :append true))
    (update-in reduced [:command]
               #(keyword (str/lower-case (or % "none"))))))

;; Listener functions.

(defn print-handler
  [message _]
  (clojure.pprint/pprint message))

(defn ping-handler
  [message to-network]
  (let [cmd (:command message)
        trail (:trailing message)]
    (when (= :ping cmd)
      (async/put! to-network (str "PONG :" trail)))))

;; Operations on a Network.

(defn- broadcast
  [message listeners to-network]
  (doseq [listener @listeners]
    (listener message to-network)))

;; Primitive socket operations.

(defn- socket-receive
  [socket]
  (.readLine (reader socket)))

(defn- socket-send
  [socket msg]
  (doto (writer socket)
    (.write (str msg "\r\n"))
    (.flush)))

(defn- socket-chan
  [socket]
  (let [channel (async/chan)]
    (async/go-loop []
      (when-let [msg (async/<! channel)]
        (socket-send @socket msg)
        (recur)))
    channel))

(defn- socket-connect
  [network]
  (let [state (atom :connecting)
        socket (promise)
        to-network (socket-chan socket)
        listeners (atom #{ping-handler})]
    (future
      (with-open [sock (Socket. (:host network) (:port network))]
        (.setSoTimeout sock 1000)
        (deliver socket sock)
        (reset! state :running)
        (while (= :running @state)
          (try
            (let [msg (-> sock
                          socket-receive)]
              (future
                (broadcast (parse-message msg)
                           listeners to-network)))
            (catch java.net.SocketTimeoutException e)
            (catch Exception e
              (.print-stack-trace e)
              (reset! state :exception))))
        (println "Socket closed")
        (reset! state :stopped)))
    (assoc network
      :state state
      :socket socket
      :to-network to-network
      :listeners listeners)))

;;;
;;; The public API.
;;;

;; Working on whole networks.

(defn shutdown! [network] (reset! (:state network) :stopping))

(defn login!
  [network user]
  (doto (:to-network network)
    (async/put! (str "NICK " (:nick user)))
    (async/put! (str "USER " (:nick user) " 0 * :" (:name user))))
  network)


(defn add-handler!
  [network f]
  (swap! (:listeners network) conj f))

(defn join-network
  [host port]
  (-> {:host host :port port}
      socket-connect))