(ns clojirc.commands
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.string :as str]))

(defn- network-send
  [network message]
  (async/put! (:to-network network)
              message)
  network)

;; Standard slash commands.

(defn admin!
  [network & [target]]
  (network-send
    network
    (match [target]
      [nil] "ADMIN"
      [_] (format "ADMIN %s" target))))

(defn away!
  [network & [message]]
  (network-send
    network
    (match [message]
      [nil] "AWAY"
      [_] (format "AWAY %s" message))))

(defn cnotice!
  [network nickname channel message]
  (network-send
    network
    (format "CNOTICE %s %s :%s" nickname channel message)))

(defn cprivmsg!
  [network nickname channel message]
  (network-send
    network
    (format "CPRIVSMG %s %s :%s" nickname channel message)))

(defn connect!
  [network target port & [remote]]
  (network-send
    network
    (match [remote]
      [nil] (format "CONNECT %s %s" target port)
      [_] (format "CONNECT %s %s %s" target port remote))))

(defn die!
  [network]
  (network-send
    network
    "DIE"))

(defn join!
  [network chan & [pass]]
  (network-send
    network
    (match [pass]
      [nil] (format "JOIN %s" chan)
      [_] (format "JOIN %s %s" chan pass))))

(defn message!
  [network receiver message]
  (network-send
    network
    (format "PRIVMSG %s :%s" receiver message)))

(defn nick!
  [network nick]
  (network-send
    network
    (format "NICK %s")))

(defn quit!
  [network & [message]]
  (network-send
    network
    (match [message]
      [nil] "QUIT"
      [msg] (format "QUIT :%s" msg))))