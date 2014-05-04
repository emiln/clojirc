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

;; JOIN command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN #foobar"
    (do (cmd/join! network {"#foobar" nil})
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN &foo fubar"
    (do (cmd/join! network {"&foo" "fubar"})
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN #foo,&bar fubar"
    (do (cmd/join! network {"#foo" "fubar"
                            "&bar" nil})
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN #foo,#bar fubar,foobar"
    (do (cmd/join! network {"#foo" "fubar"
                            "#bar" "foobar"})
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN #foo,#bar"
    (do (cmd/join! network {"#foo" nil
                            "#bar" nil})
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "JOIN 0"
    (do (cmd/join! network 0)
      (async/<!! channel))))

;; KICK command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KICK &Melbourne Matthew"
    (do (cmd/kick! network "&Melbourne" "Matthew")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KICK #Finnish John :Speaking English"
    (do (cmd/kick! network "#Finnish" "John" "Speaking English")
      (async/<!! channel))))

;; KILL command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KILL PinkPrincess :Being a totes jerk"
    (do (cmd/kill! network "PinkPrincess" "Being a totes jerk")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KILL Mikstrup :Wow, this guy."
    (do (cmd/kill! network  "Mikstrup" "Wow, this guy.")
      (async/<!! channel))))

;; KNOCK command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KNOCK #dat2 :Let me in guys, pls."
    (do (cmd/knock! network "#dat2" "Let me in guys, pls.")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "KNOCK #clojure :Come on guys."
    (do (cmd/knock! network "#clojure" "Come on guys.")
      (async/<!! channel))))

;; LINKS command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "LINKS *.au"
    (do (cmd/links! network "*.au")
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "LINKS *.edu *.bu.edu"
    (do (cmd/links! network "*.edu" "*.bu.edu")
      (async/<!! channel))))

;; LIST command.
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "LIST"
    (do (cmd/list! network)
      (async/<!! channel))))
(let [channel (async/chan)
      network {:to-network channel}]
  (expect
    "LIST #twilight_zone,#42"
    (do (cmd/list! network ["#twilight_zone" "#42"])
      (async/<!! channel))))