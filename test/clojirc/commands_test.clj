(ns clojirc.commands-test
  (:require [expectations :refer :all]
            [clojirc.commands :as cmd]
            [clojure.core.async :as async]))

;; Test all commands.

;; ADMIN command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "ADMIN tolsun.oulu.fi"
    (do (cmd/admin! network "tolsun.oulu.fi")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "ADMIN syrk"
    (do (cmd/admin! network "syrk")
      (async/<!! channel))))

;; AWAY command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "AWAY"
    (do (cmd/away! network)
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "AWAY :Gone to lunch. Back in 5"
    (do (cmd/away! network "Gone to lunch. Back in 5")
      (async/<!! channel))))

;; CONNECT command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "CONNECT tolsun.oulu.fi 6667"
    (do (cmd/connect! network "tolsun.oulu.fi" 6667)
      (async/<!! channel))))

;; DIE command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "DIE"
    (do (cmd/die! network)
      (async/<!! channel))))

;; ERROR command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "ERROR :Server *.fi already exists"
    (do (cmd/error! network "Server *.fi already exists")
      (async/<!! channel))))

;; INFO command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "INFO csd.bu.edu"
    (do (cmd/info! network "csd.bu.edu")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "INFO Angel"
    (do (cmd/info! network "Angel")
      (async/<!! channel))))

;; INVITE command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "INVITE Wiz #Twilight_Zone"
    (do (cmd/invite! network "Wiz" "#Twilight_Zone")
      (async/<!! channel))))

;; ISON command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "ISON phone trillian WiZ jarlek Avalon Angel Monstah syrk"
    (do (cmd/ison! network "phone" "trillian" "WiZ" "jarlek" "Avalon"
                   "Angel" "Monstah" "syrk")
      (async/<!! channel))))