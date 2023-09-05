# Messages worker

This service is responsible for maintaining the websocket connection with the user's client and to send any new message
to the clients,
this new messages are received through a kafka pub/sub system, upon receiving the message the service needs to fetch the
actual message content
on mongodb.
Every instance of the service should contain a websocket connection by channel and by user, every time a new user ->
channel connects
it updates the subscribed topics on kafka, where the topic is the channel name, so everytime a new message for that
channel
is published every consumer that has a user for that channel/topic will receive a copy of that message.

It was designed in order to be able to be scaled depending on the load of users/clients connected.

When this service start up, it registers himself on the list of available messages-worker's (stored on a redis db),
everytime a new user/client connects to the service, it updates its own ranking in order to distribute the load with
other messages-worker's.

