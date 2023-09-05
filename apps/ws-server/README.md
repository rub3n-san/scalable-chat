# WS server

This service is responsible for:

* creating channels
* create users
* create/store chats
* returning chats by channel
* returning the messages-worker where the client should websocket connect to
* publish a new message for consumers (kafka)

Through the GET `/connect/{channel}` HTTP request the client gets a websocket url
to which it can connect to a messages-worker, this messages-worker is selected based
on the load of the available messages-worker, this load is based on the number os users/clients
connected to those messages-worker's, this ranking is stored on a redis database.
The GET `/connect/{channel}` HTTP request can also return a first snapshot of the latest chat of the
specified channel.

The POST `/chat/{channel}` HTTP request, should be the way on how the clients post new messages/chats
this way, there should be only one way flow on how the clients that create new messages and thus
assure consistency of the timeline of the chats.

> For testing purposes new channels and users will be created if they do not exist on the db.