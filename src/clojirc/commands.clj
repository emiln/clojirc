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
      [_] (format "AWAY :%s" message))))

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

(defn encap!
  "Parameters: <source> <destination> <subcommand> [ <parameters> ]
  
  This command is for use by servers to encapsulate commands so that 
  they will propagate across hub servers not yet updated to support 
  them, and indicates the subcommand and its parameters should be 
  passed unaltered to the destination, where it will be unencapsulated 
  and parsed. This facilitates implementation of new features without a 
  need to restart all servers before they are usable across the network."
  [network source destination subcommand & parameters]
  (network-send
    network
    (match [parameters]
           [nil] (format ":%s ENCAP %s %s" source destination subcommand)
           [_] (format ":%s ENCAP %s %s %s" source destination subcommand (clojure.string/join " " parameters)))))

(defn error!
  "Parameters: <error message>

  The ERROR command is for use by servers when reporting a serious or
  fatal error to its peers.  It may also be sent from one server to
  another but MUST NOT be accepted from any normal unknown clients.

  Only an ERROR message SHOULD be used for reporting errors which occur
  with a server-to-server link.  An ERROR message is sent to the server
  at the other end (which reports it to appropriate local users and
  logs) and to appropriate local users and logs.  It is not to be
  passed onto any other servers by a server if it is received from a
  server.

  The ERROR message is also used before terminating a client
  connection.

  When a server sends a received ERROR message to its operators, the
  message SHOULD be encapsulated inside a NOTICE message, indicating
  that the client was not responsible for the error."
  [network error-message]
  (network-send
    network
    (format "ERROR :%s" error-message)))

(defn help!
  "Parameters: None
  
  Requests the server help file.

  This command is not formally defined in an RFC, but is in use by most 
  major IRC daemons."
  [network]
  (network-send
    network
    "HELP"))

(defn info!
  "Parameters: [ <target> ]

  The INFO command is REQUIRED to return information describing the
  server: its version, when it was compiled, the patchlevel, when it
  was started, and any other miscellaneous information which may be
  considered to be relevant.

  Wildcards are allowed in the <target> parameter."
  [network & [target]]
  (network-send
    network
    (match [target]
           [nil] "INFO"
           [_] (format "INFO %s" target))))

(defn invite!
  "Parameters: <nickname> <channel>

  The INVITE command is used to invite a user to a channel.  The
  parameter <nickname> is the nickname of the person to be invited to
  the target channel <channel>.  There is no requirement that the
  channel the target user is being invited to must exist or be a valid
  channel.  However, if the channel exists, only members of the channel
  are allowed to invite other users.  When the channel has invite-only
  flag set, only channel operators may issue INVITE command."
  [network nickname channel]
  (network-send
    network
    (format "INVITE %s %s" nickname channel)))

(defn ison!
  "Parameters: <nickname> *( SPACE <nickname> )

  The ISON command was implemented to provide a quick and efficient
  means to get a response about whether a given nickname was currently
  on IRC. ISON only takes one (1) type of parameter: a space-separated
  list of nicks.  For each nickname in the list that is present, the
  server adds that to its reply string.  Thus the reply string may
  return empty (none of the given nicks are present), an exact copy of
  the parameter string (all of them present) or any other subset of the
  set of nicks given in the parameter.  The only limit on the number of
  nicks that may be checked is that the combined length MUST NOT be too
  large as to cause the server to chop it off so it fits in 512
  characters.

  ISON is only processed by the server local to the client sending the
  command and thus not passed onto other servers for further
  processing."
  [network nickname & more-nicknames]
  (network-send
    network
    (match [more-nicknames]
           [nil] (format "ISON %s" nickname)
           [_] (format "ISON %s %s" nickname (clojure.string/join " " more-nicknames)))))

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

(defn watch!
  "Parameters: *( ( \"+\" / \"-\" ) <nickname>)
  
  Adds or removes a user to a client's server-side friends list. More
  than one nickname may be specified in a space-separated list, each
  item prefixed with a \"+\" or \"-\" to designate whether it is being
  added or removed. Sending the command with no parameters returns the
  entries in the client's friends list.

  This command is not formally defined in an RFC, but is supported by
  most major IRC daemons. Support is indicated in a RPL_ISUPPORT reply
  (numeric 005) with the WATCH keyword and the maximum number of
  entries a client may have in its friends list."
  [network nickname & more-nicknames]
  (network-send
    network
    (match [more-nicknames]
      [nil] (format "WATCH %s" nickname)
      [_] (format "WATCH %s,%s" nickname (str/join "," more-nicknames)))))

(defn who!
  "Parameters: [ <mask> [ \"o\" ] ]

  The WHO command is used by a client to generate a query which returns
  a list of information which 'matches' the <mask> parameter given by
  the client.  In the absence of the <mask> parameter, all visible
  (users who aren't invisible (user mode +i) and who don't have a
  common channel with the requesting client) are listed.  The same
  result can be achieved by using a <mask> of \"0\" or any wildcard which
  will end up matching every visible user.

  The <mask> passed to WHO is matched against users' host, server, real
  name and nickname if the channel <mask> cannot be found.
  
  If the \"o\" parameter is passed only operators are returned according
  to the <mask> supplied."
  [network mask & [flag]]
  (network-send
    network
    (match [flag]
      ["o"] (format "WHO %s o" mask)
      [_] (format "WHO %s" mask))))

(defn whois!
  "Parameters: [ <target> ] <mask> *( \",\" <mask> )

  This command is used to query information about particular user.
  The server will answer this command with several numeric messages
  indicating different statuses of each user which matches the mask (if
  you are entitled to see them).  If no wildcard is present in the
  <mask>, any information about that nick which you are allowed to see
  is presented.

  If the <target> parameter is specified, it sends the query to a
  specific server.  It is useful if you want to know how long the user
  in question has been idle as only local server (i.e., the server the
  user is directly connected to) knows that information, while
  everything else is globally known.

  Wildcards are allowed in the <target> parameter."
  [network target-or-mask & masks]
  (network-send
    network
    (match [target-or-mask masks]
      [_ nil] (format "WHOIS %s" target-or-mask)
      [(target :guard #(re-find #"\." %)) _]
      (format "WHOIS %s %s" target (str/join "," masks))
      [mask _] (format "WHOIS %s" (str/join "," (cons mask masks))))))

(defn whowas!
  "Parameters: <nickname> *( \",\" <nickname> ) [ <count> [ <target> ] ]

  Whowas asks for information about a nickname which no longer exists.
  This may either be due to a nickname change or the user leaving IRC.
  In response to this query, the server searches through its nickname
  history, looking for any nicks which are lexically the same (no wild
  card matching here).  The history is searched backward, returning the
  most recent entry first.  If there are multiple entries, up to
  <count> replies will be returned (or all of them if no <count>
  parameter is given).  If a non-positive number is passed as being
  <count>, then a full search is done.

  Wildcards are allowed in the <target> parameter."
  [network nickname-or-nicknames & [count target]]
  (network-send
    network
    (->> [(if (coll? nickname-or-nicknames)
            (str/join "," nickname-or-nicknames)
            nickname-or-nicknames)
          count target]
      (filter identity)
      (str/join " ")
      (format "WHOWAS %s"))))