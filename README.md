# clojirc

A Clojure library designed to easily communicate with IRC servers.

## Usage

The following sample connects to a network, logs in, and sends a message to a
channel every few seconds:

```clojure
(require '[clojirc.core :as irc])
(require '[clojirc.commands :as cmd])
(require '[clojure.core.async :as async])

;; Join a network.
(def network (irc/join-network "irc.quakenet.org" 6667))

;; Make your bot known to the network.
(irc/login! network {:nick "BestBotRUS" :name "This is a quality bot, guys."})

;; Join a channel.
(cmd/join! network "#dat2")

;; Send a message. People will want to get acquainted with your great bot.
(cmd/message! network "#dat2" "Hello, friends.")

;; Send a message every few seconds to keep the channel entertained.
(async/go-loop []
  (async/<! (async/timeout (+ 10000 (rand-int 10000))))
  (cmd/message! network "#dat2" "I am still here, guys!")
  (recur))
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
