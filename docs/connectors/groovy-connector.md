Groovy Resource Connector
====

This connector allows to monitor and manage your IT resources using Groovy scripts. It is very useful in the following cases:
* SNAMP doesn't provide Resource Connector for the specific protocol out-of-the-box. The possible uses cases:
    * Parsing log data and exposing results as an attributes
    * Extracting records from databases and exposing results as an attributes
    * Processing Web resources (or REST services) via HTTP
    * Listening message queues via JMS and exposing processed messages as SNAMP notifications
* Customize existing Resource Connectors
    * Aggregate two or more attributes (sum, average, peak, percent) and expose result as an attribute
    * Aggregate notifications