(ns clojirc.commands
  (:require [clojure.core.async :as async]
            [clojure.core.match :refer [match]]
            [clojure.string :as str]))

(defn- network-send
  [network message]
  (async/put! (:to-network network)
              message)
  network)

(defn- coll-elem-or-nil [coll] 
  (cond 
    (string? coll) coll
    (coll? coll) (str/join "," coll)
    :else nil))
  
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
  "Parameters: {<channel> [ <key> ]} / 0
  The JOIN command is used by a user to request to start listening to
  the specific channel.  Servers MUST be able to parse arguments in the
  form of a list of target, but SHOULD NOT use lists when sending JOIN
  messages to clients.

  Once a user has joined a channel, he receives information about
  all commands his server receives affecting the channel.  This
  includes JOIN, MODE, KICK, PART, QUIT and of course PRIVMSG/NOTICE.
  This allows channel members to keep track of the other channel
  members, as well as channel modes.

  If a JOIN is successful, the user receives a JOIN message as
  confirmation and is then sent the channel's topic (using RPL_TOPIC) and
  the list of users who are on the channel (using RPL_NAMREPLY), which
  MUST include the user joining.

  Note that this message accepts a special argument (0), which is
  a special request to leave all channels the user is currently a member
  of.  The server will process this message as if the user had sent
  a PART command (See Section 3.2.2) for each channel he is a member
  of."
  [network chan-pass-map]
  (network-send
    network
    (if (map? chan-pass-map)
      (let [chans (map key (reverse (sort-by val chan-pass-map)))
            passes (filter identity (map val (reverse (sort-by val chan-pass-map))))]
        (match [(first passes)]
               [nil] (format "JOIN %s" (str/join "," chans))
               [_] (format "JOIN %s %s" (str/join "," chans) (str/join "," passes))))
      (format "JOIN %s" chan-pass-map))))

(defn kick!
  "Parameters: <channel> *( \",\" <channel> ) <user> *( \",\" <user> )
               [<comment>]

  The KICK command can be used to request the forced removal of a user
  from a channel.  It causes the <user> to PART from the <channel> by
  force.  For the message to be syntactically correct, there MUST be
  either one channel parameter and multiple user parameter, or as many
  channel parameters as there are user parameters.  If a \"comment\" is
  given, this will be sent instead of the default message, the nickname
  of the user issuing the KICK.

  The server MUST NOT send KICK messages with multiple channels or
  users to clients.  This is necessarily to maintain backward
  compatibility with old client software."
  [network channels users & [message]]
  (network-send
    network
    (match [message]
           [nil] (format "KICK %s %s" (coll-elem-or-nil channels) (coll-elem-or-nil users))
           [_] (format "KICK %s %s :%s" (coll-elem-or-nil channels) (coll-elem-or-nil users) message))))

(defn kill!
  "Parameters: <nickname> <comment>

  The KILL command is used to cause a client-server connection to be
  closed by the server which has the actual connection.  Servers
  generate KILL messages on nickname collisions.  It MAY also be
  available available to users who have the operator status.

  Clients which have automatic reconnect algorithms effectively make
  this command useless since the disconnection is only brief.  It does
  however break the flow of data and can be used to stop large amounts
  of 'flooding' from abusive users or accidents.  Abusive users usually
  don't care as they will reconnect promptly and resume their abusive
  behaviour.  To prevent this command from being abused, any user may
  elect to receive KILL messages generated for others to keep an 'eye'
  on would be trouble spots.

  In an arena where nicknames are REQUIRED to be globally unique at all
  times, KILL messages are sent whenever 'duplicates' are detected
  (that is an attempt to register two users with the same nickname) in
  the hope that both of them will disappear and only 1 reappear.

  When a client is removed as the result of a KILL message, the server
  SHOULD add the nickname to the list of unavailable nicknames in an
  attempt to avoid clients to reuse this name immediately which is
  usually the pattern of abusive behaviour often leading to useless
  \"KILL loops\".  See the \"IRC Server Protocol\" document [IRC-SERVER]
  for more information on this procedure.

  The comment given MUST reflect the actual reason for the KILL.  For
  server-generated KILLs it usually is made up of details concerning
  the origins of the two conflicting nicknames.  For users it is left
  up to them to provide an adequate reason to satisfy others who see
  it.  To prevent/discourage fake KILLs from being generated to hide
  the identify of the KILLer, the comment also shows a 'kill-path'
  which is updated by each server it passes through, each prepending
  its name to the path."
  [network nickname comment]
  (network-send
    network
    (format "KILL %s :%s" nickname comment)))

(defn knock!
  "Parameters: <channel> [ <message> ]

  Sends a NOTICE to an invitation-only <channel> with an optional 
  <message>, requesting an invite.

  This command is not formally defined by an RFC, but is supported by 
  most major IRC daemons. Support is indicated in a RPL_ISUPPORT reply 
  (numeric 005) with the KNOCK keyword."
  [network channel & [message]]
  (network-send
    network
    (match [message]
           [nil] (format "KNOCK %s" channel)
           [_] (format "KNOCK %s :%s" channel message))))

(defn links!
  "Parameters: [ [ <remote server> ] <server mask> ]

  With LINKS, a user can list all servernames, which are known by the
  server answering the query.  The returned list of servers MUST match
  the mask, or if no mask is given, the full list is returned.

  If <remote server> is given in addition to <server mask>, the LINKS
  command is forwarded to the first server found that matches that name
  (if any), and that server is then required to answer the query."
  [network & [remote mask]]
  (network-send
    network
    (str/join " " (filter identity ["LINKS" remote mask]))))

(defn list!
  "Parameters: [ <channel> *( \",\" <channel> ) [ <target> ] ]

  The list command is used to list channels and their topics.  If the
  <channel> parameter is used, only the status of that channel is
  displayed.

  If the <target> parameter is specified, the request is forwarded to
  that server which will generate the reply.

  Wildcards are allowed in the <target> parameter."
  [network & [channels target]]
  (network-send
    network
    (str/join " " (filter identity ["LIST" (coll-elem-or-nil channels) target]))))

(defn lusers!
  "Parameters: [ <mask> [ <target> ] ]

  The LUSERS command is used to get statistics about the size of the
  IRC network.  If no parameter is given, the reply will be about the
  whole net.  If a <mask> is specified, then the reply will only
  
  concern the part of the network formed by the servers matching the
  mask.  Finally, if the <target> parameter is specified, the request
  is forwarded to that server which will generate the reply.

  Wildcards are allowed in the <target> parameter."
  [network & [mask target]]
  (network-send
    network
    (str/join " " (filter identity ["LUSERS" mask target]))))

(defn mode!
  "Parameters: <channel> *( ( \"-\" / \"+\" ) *<modes> *<modeparams> )

  The MODE command is provided so that users may query and change the
  characteristics of a channel.  For more details on available modes
  and their uses, see \"Internet Relay Chat: Channel Management\" [IRC-
  CHAN].  Note that there is a maximum limit of three (3) changes per
  command for modes that take a parameter."
  [network channel & parameters]
  (network-send
    network
    (str/join " " (filter identity ["MODE" channel (str/join " " parameters)]))))

(defn motd!
  "Parameters: [ <target> ]

  The MOTD command is used to get the \"Message Of The Day\" of the given
  server, or current server if <target> is omitted.

  Wildcards are allowed in the <target> parameter."
  [network & [target]]
  (network-send
    network
    (str/join " " (filter identity ["MOTD" target]))))

(defn names!
  "Parameters: [ <channel> *( \",\" <channel> ) [ <target> ] ]

  By using the NAMES command, a user can list all nicknames that are
  visible to him. For more details on what is visible and what is not,
  see \"Internet Relay Chat: Channel Management\" [IRC-CHAN].  The
  <channel> parameter specifies which channel(s) to return information
  about.  There is no error reply for bad channel names.

  If no <channel> parameter is given, a list of all channels and their
  occupants is returned.  At the end of this list, a list of users who
  are visible but either not on any channel or not on a visible channel
  are listed as being on `channel' \"*\".

  If the <target> parameter is specified, the request is forwarded to
  that server which will generate the reply.

  Wildcards are allowed in the <target> parameter."
  [network & [channels target]]
  (network-send
    network
    (str/join " " (filter identity ["NAMES" (coll-elem-or-nil channels) target]))))

(defn namesx!
  "Parameters: None
  
  Instructs the server to send names in an RPL_NAMES reply prefixed 
  with their respective channel status. For example:
  
  With NAMESX

    :irc.server.net 353 Phyre = #SomeChannel :@+WiZ
  
  Without NAMESX

    :irc.server.net 353 Phyre = #SomeChannel :WiZ
  
  This command can ONLY be used if the NAMESX keyword is returned in
  an RPL_ISUPPORT (numeric 005) reply. It may also be combined with 
  the UHNAMES command.
  
  This command is not formally defined in an RFC, but is recognized 
  by most major IRC daemons."
  [network]
  (network-send
    network
    "PROTOCTL NAMESX"))

(defn nick!
  "Parameters: <nickname>

  NICK command is used to give user a nickname or change the existing
  one."
  [network nick]
  (network-send
    network
    (format "NICK %s" nick)))

(defn message!
  [network receiver message]
  (network-send
    network
    (format "PRIVMSG %s :%s" receiver message)))

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
