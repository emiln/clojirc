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
(expect-command
  "JOIN #foobar"
  (cmd/join! {"#foobar" nil}))
(expect-command
  "JOIN &foo fubar"
  (cmd/join! {"&foo" "fubar"}))
(expect-command
  "JOIN #foo,&bar fubar"
  (cmd/join! {"#foo" "fubar"
              "&bar" nil}))
(expect-command
  "JOIN #foo,#bar fubar,foobar"
  (cmd/join! {"#foo" "fubar"
              "#bar" "foobar"}))
(expect-command
  "JOIN #foo,#bar"
  (cmd/join! {"#foo" nil
              "#bar" nil}))
(expect-command
  "JOIN 0"
  (cmd/join! 0))

;; KICK command.
(expect-command
  "KICK &Melbourne Matthew"
  (cmd/kick! "&Melbourne" "Matthew"))
(expect-command
  "KICK #Finnish John :Speaking English"
  (cmd/kick! "#Finnish" "John" "Speaking English"))

;; KILL command.
(expect-command
  "KILL PinkPrincess :Being a totes jerk"
  (cmd/kill! "PinkPrincess" "Being a totes jerk"))
(expect-command
  "KILL Mikstrup :Wow, this guy."
  (cmd/kill! "Mikstrup" "Wow, this guy."))

;; KNOCK command.
(expect-command
  "KNOCK #dat2 :Let me in guys, pls."
  (cmd/knock! "#dat2" "Let me in guys, pls."))
(expect-command
  "KNOCK #clojure :Come on guys."
  (cmd/knock! "#clojure" "Come on guys."))

;; LINKS command.
(expect-command
  "LINKS *.au"
  (cmd/links! "*.au"))
(expect-command
  "LINKS *.edu *.bu.edu"
  (cmd/links! "*.edu" "*.bu.edu"))

;; LIST command.
(expect-command
  "LIST"
  (cmd/list!))
(expect-command
  "LIST #twilight_zone,#42"
  (cmd/list! ["#twilight_zone" "#42"]))

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
(expect-command
  "LINKS *.au"
  (cmd/links! "*.au"))
(expect-command
  "LINKS *.edu *.bu.edu"
  (cmd/links! "*.edu" "*.bu.edu"))

;; LIST command.
(expect-command
  "LIST"
  (cmd/list!))
(expect-command
  "LIST #twilight_zone,#42"
  (cmd/list! ["#twilight_zone" "#42"]))

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