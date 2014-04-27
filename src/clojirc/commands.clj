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
  "Parameters: [ <target> ]

  The admin command is used to find information about the administrator
  of the given server, or current server if <target> parameter is
  omitted.  Each server MUST have the ability to forward ADMIN messages
  to other servers.

  Wildcards are allowed in the <target> parameter."
  [network & [target]]
  (network-send
    network
    (match [target]
      [nil] "ADMIN"
      [_] (format "ADMIN %s" target))))

(defn away!
  "Parameters: [ <message> ]

  With the AWAY command, clients can set an automatic reply string for
  any PRIVMSG commands directed at them (not to a channel they are on).
  The server sends an automatic reply to the client sending the PRIVMSG
  command.  The only replying server is the one to which the sending
  client is connected to.

  The AWAY command is used either with one parameter, to set an AWAY
  message, or with no parameters, to remove the AWAY message.

  Because of its high cost (memory and bandwidth wise), the AWAY
  message SHOULD only be used for client-server communication.  A
  server MAY choose to silently ignore AWAY messages received from
  other servers.  To update the away status of a client across servers,
  the user mode 'a' SHOULD be used instead."
  [network & [message]]
  (network-send
    network
    (match [message]
      [nil] "AWAY"
      [_] (format "AWAY %s" message))))

(defn cnotice!
  "Parameters: <nickname> <channel> <message>

  Sends a channel NOTICE message to <nickname> on <channel> that
  bypasses flood protection limits. The target nickname must be in the
  same channel as the client issuing the command, and the client must
  be a channel operator.

  Normally an IRC server will limit the number of different targets a
  client can send messages to within a certain time frame to prevent
  spammers or bots from mass-messaging users on the network, however
  this command can be used by channel operators to bypass that limit in
  their channel. For example, it is often used by help operators that
  may be communicating with a large number of users in a help channel
  at one time.

  This command is not formally defined in an RFC, but is in use by some
  IRC networks. Support is indicated in a RPL_ISUPPORT reply (numeric
  005) with the CNOTICE keyword"
  [network nickname channel message]
  (network-send
    network
    (format "CNOTICE %s %s :%s" nickname channel message)))

(defn cprivmsg!
  "Parameters: <nickname> <channel> <message>

  Sends a private message to <nickname> on <channel> that bypasses
  flood protection limits. The target nickname must be in the same
  channel as the client issuing the command, and the client must be a
  channel operator.

  Normally an IRC server will limit the number of different targets a
  client can send messages to within a certain time frame to prevent
  spammers or bots from mass-messaging users on the network, however
  this command can be used by channel operators to bypass that limit in
  their channel. For example, it is often used by help operators that
  may be communicating with a large number of users in a help channel
  at one time.

  This command is not formally defined in an RFC, but is in use by some
  IRC networks. Support is indicated in a RPL_ISUPPORT reply (numeric
  005) with the CPRIVMSG keyword"
  [network nickname channel message]
  (network-send
    network
    (format "CPRIVSMG %s %s :%s" nickname channel message)))

(defn connect!
  "Parameters: <target> <port> [ <remote> ]

  The CONNECT command can be used to request a server to try to
  establish a new connection to another server immediately.  CONNECT is
  a privileged command and SHOULD be available only to IRC Operators.
  If a <remote> server is given and its mask doesn't match name of the
  parsing server, the CONNECT attempt is sent to the first match of
  remote server. Otherwise the CONNECT attempt is made by the server
  processing the request.

  The server receiving a remote CONNECT command SHOULD generate a
  WALLOPS message describing the source and target of the request."
  [network target port & [remote]]
  (network-send
    network
    (match [remote]
      [nil] (format "CONNECT %s %s" target port)
      [_] (format "CONNECT %s %s %s" target port remote))))

(defn die!
  "Parameters: None

  An operator can use the DIE command to shutdown the server.  This
  message is optional since it may be viewed as a risk to allow
  arbitrary people to connect to a server as an operator and execute
  this command.

  The DIE command MUST always be fully processed by the server to which
  the sending client is connected and MUST NOT be passed onto other
  connected servers."
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