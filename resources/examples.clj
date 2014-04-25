;; Require the IRC library.
(require '[irclj.core :as irc])
(require '[clojure.core.async :as async])

;; Connect to a network and save its input/output channels.
(def network (irc/join-network "irc.quakenet.org" 6667))

;; Supply login information to the network.
(irc/login! network {:nick "thebestnick" :user "A quality user"})

;; Join a channel.
(irc/join! network "#dat2")

;; Add a handler to reply to all messages you see.
(irc/add-handler!
  network
  (fn [msg to-network]
    (when (= (:command msg) :privmsg)
      (let [{:keys [trailing params command prefix]} msg]
        (async/put! to-network
                    (str "Good point, " prefix "."))))))