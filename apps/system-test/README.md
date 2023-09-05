# System test

This service will test the whole system.
New test suites and tests cases can be added using the TestSuite and TestCase classes.

# Usage

To be able to use this service it is required for the whole system to be running.
For that, you can use the `docker-compose` on the root of the project (more info on the root README.md)

On the `Test`class you can define the url for the ws-server, if it's being tested locally with `docker-compose`it's address should b e `localhost:8080`. 

