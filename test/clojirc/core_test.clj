(ns clojirc.core-test
  (:require [expectations :refer :all]
            [clojirc.core :refer [irc-parser]]
            [instaparse.core :refer [failure?]]))

;; Test the parser.

;; NOTICE from server.
(expect
  (complement failure?)
  (irc-parser "NOTICE AUTH :*** Looking up your hostname..."))

;; Response to NICK.
(expect
  (complement failure?)
  (irc-parser ":kornbluth.freenode.net 433 * Paul :Nickname is already in use."))

;; A PING challenge.
(expect
  (complement failure?)
  (irc-parser "PING :kornbluth.freenode.net"))

;; A different sort of PING challenge.
(expect
  (complement failure?)
  (irc-parser "PING :1294010420"))

;; A join message.
(expect
  (complement failure?)
  (irc-parser ":hejvenner!~hejvenner@193.27.44.5 JOIN #dat2"))

;; Premature attempt to join a channel.
(expect
  (complement failure?)
  (irc-parser ":port80b.se.quakenet.org 451 *  :Register first."))