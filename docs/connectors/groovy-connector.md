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

> Groovy connector built on top of Groovy 2.4.3

Groovy Resource Connector provides the following features for Groovy scripting:
* Simple DSL extensions of Groovy language
* Accessing to attributes and notifications of any other connected managed resources
* Full [Grape](http://www.groovy-lang.org/Grape) support so you can use any Groovy module or Java library published in Maven repository

The connector has the following architecture:
* Each attribute must be represented as a separated Groovy script. The attribute name will be interpreter as a script file name. For example, the logic for attribute `Memory` must be placed into `Memory.groovy` file
* Each event must be represented as a separated Groovy script. The event category will be interpreted as a script file name. For example, the logic for event `Error` must be placed into `Error.groovy` file
* Optionally, you may write initialization script that is used to initialize instance of the Managed Resource Connector
* Each instance of the Groovy Connector provides a sandbox for Groovy scripts. So, initialization script will be executed for each instance of the connector. You can't share objects between instances of Groovy Connector

## Connection String
Connection string specifies a set of paths with Groovy scripts. You may specify more than one path using OS-specific path separator symbol:
* `:` for Linux
* `;` for Windows

For example, `/usr/local/snamp/groovy:/usr/local/snamp/scripts`

The path is used to find initialization, attribute and event scripts.

## Configuration parameters
JMX Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
initScript | String | No | The name of the initialization script file | `init.groovy`
groovy.warnings | String | No | Groovy warning level | `likely errors`
groovy.source.encoding | String | No | The encoding to be used when reading Groovy source files
groovy.classpath | String | No | Classpath using to find third-party JARs | `/usr/local/jars:/home/user/jars`
groovy.output.verbose | Boolean (`true` or `false`) | No | Turns verbose operation on or off | `true`
groovy.output.debug | Boolean | No | Turns debugging operation on or off | `false`
groovy.errors.tolerance | Integer | No | Sets the error tolerance, which is the number of non-fatal errors (per unit) that should be tolerated before compilation is aborted

All these parameters (including user-defined) will be visible as global variables in all scripts.

## Configuring attributes

## Configuring events
