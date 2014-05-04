(ns clojirc.commands-test
  (:require [expectations :refer :all]
            [clojirc.commands :as cmd]
            [clojure.core.async :as async]))

;; Define a helper macro.
(defmacro expect-command
  [string command]
  `(let [channel# (clojure.core.async/chan)
         network# {:to-network channel#}]
     (expect
       ~string
       (do (~(first command) network# ~@(rest command))
         (clojure.core.async/<!! channel#)))))

;; Test all commands.

;; ADMIN command.
(expect-command
  "ADMIN tolsun.oulu.fi"
  (cmd/admin! "tolsun.oulu.fi"))
(expect-command
  "ADMIN syrk"
  (cmd/admin! "syrk"))

;; AWAY command.
(expect-command
  "AWAY"
  (cmd/away!))
(expect-command
  "AWAY :Gone to lunch. Back in 5"
  (cmd/away! "Gone to lunch. Back in 5"))

;; CONNECT command.
(expect-command
  "CONNECT tolsun.oulu.fi 6667"
  (cmd/connect! "tolsun.oulu.fi" 6667))

;; DIE command.
(expect-command
  "DIE"
  (cmd/die!))

;; ERROR command.
(expect-command
  "ERROR :Server *.fi already exists"
  (cmd/error! "Server *.fi already exists"))

;; INFO command.
(expect-command
  "INFO csd.bu.edu"
  (cmd/info! "csd.bu.edu"))
(expect-command
  "INFO Angel"
  (cmd/info! "Angel"))

;; INVITE command.
(expect-command
  "INVITE Wiz #Twilight_Zone"
  (cmd/invite! "Wiz" "#Twilight_Zone"))

;; ISON command.
(expect-command
  "ISON phone trillian WiZ jarlek Avalon Angel Monstah syrk"
  (cmd/ison! "phone" "trillian" "WiZ" "jarlek" "Avalon"
             "Angel" "Monstah" "syrk"))

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

;; WATCH command.
(expect-command
  "WATCH +Binky,+Kardeth,-Mikstrup,-Q"
  (cmd/watch! "+Binky" "+Kardeth" "-Mikstrup" "-Q"))

;; WHO command.
(expect-command
  "WHO *.fi"
  (cmd/who! "*.fi"))
(expect-command
  "WHO jto* o"
  (cmd/who! "jto*" "o"))

;; WHOIS command.
(expect-command
  "WHOIS wiz"
  (cmd/whois! "wiz"))
(expect-command
  "WHOIS eff.org trillian"
  (cmd/whois! "eff.org" "trillian"))
(expect-command
  "WHOIS eff.org wiz,trillian"
  (cmd/whois! "eff.org" "wiz" "trillian"))

;; WHOWAS command.
(expect-command
  "WHOWAS Wiz"
  (cmd/whowas! "Wiz"))
(expect-command
  "WHOWAS Mermaid 9"
  (cmd/whowas! "Mermaid" 9))
(expect-command
  "WHOWAS Trillian 1 *.edu"
  (cmd/whowas! "Trillian" 1 "*.edu"))
(expect-command
  "WHOWAS Binky,Binkster,Binksterer 5 srs.biz"
  (cmd/whowas! ["Binky" "Binkster" "Binksterer"] 5 "srs.biz"))
