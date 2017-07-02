Groovy Resource Connector
====

Groovy connector allows to monitor and manage external components using Groovy scripts. It can be useful in following cases:

* SNAMP doesn't provide Resource Connector for the specific protocol out-of-the-box. Possible uses cases:
  * Parsing log data and exposing results as an attributes
  * Extracting records from databases and exposing results as attributes
  * Processing Web resources (or REST services) via HTTP (JSON, AtomPub and etc)
  * Listening message queues (or topics) via JMS and exposing processed messages as SNAMP notifications
* Customize existing Resource Connectors
  * Aggregate two or more attributes (sum, average, peak, percent) and expose result as an attribute
  * Aggregate notifications

> Groovy connector is based on Groovy 2.4

Each attribute, event, operation and health check should be declared in main script file using DSL extensions

## Connection String
Connection string specifies name of the main script and set of paths in URL format with Groovy scripts separated by symbol `;`. For example: `Main.groovy;file:/usr/local/snamp/groovy;file:/usr/local/snamp/scripts`

## Configuration parameters
JMX Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
groovy.warnings | String | No | Groovy warning level | `likely errors`
groovy.source.encoding | String | No | Encoding to be used when reading Groovy source files
groovy.classpath | String | No | Classpath using to find third-party JARs | `/usr/local/jars:/home/user/jars`
groovy.output.verbose | Boolean (`true` or `false`) | No | Turns verbose operation on or off | `true`
groovy.output.debug | Boolean | No | Turns debugging operation on or off | `false`
groovy.errors.tolerance | Integer | No | Sets the error tolerance, which is the number of non-fatal errors (per unit) that should be tolerated before compilation is aborted

All these parameters (including user-defined) will be visible as global variables within all the available scripts.

## Configuring attributes
Each attribute configured in Groovy Resource Connector has following configuration schema:
* `Name` - name of the attribute declared in the main script file
* There is no predefined configuration parameters.

## Configuring events
Each event configured in Groovy Resource Connector has following configuration schema:
* `Category` - name of the notification declared in the main script file
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
severity | String | No | Overrides severity level of the emitted notification | `warning`

## Configuring operations
Each operation configured in Groovy Resource Connector has following configuration schema:
* `Name` - name of the operation declared in the main script file
* There is no predefined configuration parameters.

## Scripting
Groovy Resource Connector provides following features for Groovy scripting:

* Simple DSL extensions of Groovy language
* Accessing to attributes and notifications of any other connected managed resources
* Full [Grape](http://www.groovy-lang.org/Grape) support so you can use any Groovy module or Java library published in Maven repository

Each instance of the Groovy Resource Connector has isolated sandbox with its own Java class loader used for Groovy scripts.

All configuration parameters specified at the resource-level will be visible for all scripts. For example, you have configured `initScript` and `customParam` parameters. The value of these parameters can be obtained as follows:
```groovy
println customParam
```

> Read **SNAMP Management Information Model** before you continue

Groovy connector provides following DSL extensions accessible from any type of scripts:
* Global variables:
  * `resourceName` - contains the name of the managed resource as it specified in the SNAMP configuration
  * `activeClusterNode` - boolean read-only variable indicating that the code is executed in active node of cluster
  * `logger` - represents standard Java logger
  * `resources` - a set of all resources registered in SNAMP
* Global constants
  * `INT8` - represents `int8` type from SNAMP Management Information Model
  * `INT16`- represents `int16` type from SNAMP Management Information Model
  * `INT32`- represents `int32` type from SNAMP Management Information Model
  * `INT64`- represents `int64` type from SNAMP Management Information Model
  * `BOOL` - represents `bool` type from SNAMP Management Model
  * `FLOAT32`- represents `float32` type from SNAMP Management Information Model
  * `FLOAT64`- represents `float64` type from SNAMP Management Information Model
  * `BIGINT`- represents `bigint` type from SNAMP Management Information Model
  * `BIGDECIMAL`- represents `bigdecimal` type from SNAMP Management Information Model
  * `CHAR`- represents `char` type from SNAMP Management Information Model
  * `INT16`- represents `int16` type from SNAMP Management Information Model
  * `STRING`- represents `string` type from SNAMP Management Information Model
  * `OBJECTNAME`- represents `objectname` type from SNAMP Management Information Model
  * `DATETIME`- represents `datetime` type from SNAMP Management Information Model
* Complex type builders:
  * `ArrayType ARRAY(OpenType elementType)` - construct array type
  * `CompositeType DICTIONARY(String typeName, String typeDescription, Map items)` - construct dictionary type
  * `TabularType TABLE(String typeName, String typeDescription, Map[] columns)` - construct table type
* Data type converters:
  * `Map asDictionary(CompositeData dict)` - convert JMX composite data (dictionary in SNAMP terminology) to Map (key/value pairs). This method can be used in `setValue` function to convert input value to Map
  * `CompositeData asDictionary(Map dict)` - convert key/values pairs to JMX composite data (dictionary). This method can be used in `getValue` function to convert input value to CompositeData
  * `Map[] asTable(TabularData table)` - convert JMX tabular data (table) to a collection of rows. This method can be used in `setValue` function to convert input value to rows
  * `TabularData asTable(Map[] rows)` - convert a collection of rows into JMX tabular data (table in SNAMP terminology). This method can be used in `getValue` function to convert input value to TabularData
* Accessing to other connected managed resources:
  * `Object getResourceAttribute(String resourceName, String attributeName)` - read attribute of the connected managed resource
  * `Dictionary getResourceAttributeInfo(String resourceName, String attributeName)` - read configuration parameters of the attribute
  * `void setResourceAttribute(String resourceName, String attributeName, Object value)` - write attribute of the connected managed resource
  * `Dictionary getResourceNotificationInfo(String resourceName, String notifType)` - read configuration parameters of the event
  * `void addNotificationListener(String resourceName, NotificationListener listener)` - subscribe to the event of the managed resource
  * `void addNotificationListener(String resourceName, NotificationListener listener, NotificationFilter filter, Objects handback)` - subscribe to the event of the managed resource using additional options, such as filter and handback object
  * `void removeNotificationListener(String resourceName, NotificationListener listener)` - remove subscription to the event of the managed resource
  * `ManagedResourceConfiguration getResourceConfiguration(String resourceName)` - read configuration of the connected managed resource
* Inter-script communication
  * `Communicator getCommunicator(String sessionName)` - get or create a new communication session. If SNAMP is installed in clustered configuration then this communicator can send message to other nodes in the cluster
* Raising events
  * `emit(String category, String message)` - emits notification from connector
  * `emit(Notification n)` - emits notifications from connector
* Other functions
  * `Job createTimer(Closure task, long period)` - creates new timer that periodically executes specified task
  * `Job schedule(Closure task, long period)` - execute the specified task periodically in the background.

Following example demonstrates reading attribute of the connected managed resource:

```groovy
println resourceName
def value = getResourceAttribute 'app-server', 'freeMemory'
if(value < 100) error 'Not enough memory'
```

Following example demonstrates subscribing on the event within the connected managed resource:

```groovy
import javax.management.NotificationListener

NotificationListener listener = { notif, hndback -> println notif.message }

addNotificationListener 'app-server', listener
```

Working with timers:

```groovy
def timer = createTimer({ println 'Tick' }, 300)  //will print 'Tick' every 300 milliseconds
timer.run() //start printing
timer.close() //stop printing
```
the code above is equivalent to
```groovy
def timer = schedule({ println 'Tick' }, 300)
timer.close()
```

Get configuration of the connected resource:

```groovy
def config = getResourceConfiguration 'app-server'

println config.connectionString
println config.connectionType
println config.parameters.socketTimeout //socketTimeout is the name of configuration property
```

Simple messaging using communicator:

```groovy
def communicator = getCommunicator 'test-communicator'
communicator.addMessageListener({ msg -> println msg}, MessageType.SIGNAL)
```
in other script file
```groovy
def communicator = getCommunicator 'test-communicator'
communicator.sendSignal 'Hello, world!'
```

Synchronous messaging using communicator:

```groovy
//script1.groovy
communicator = getCommunicator 'test-communicator'
def listen(message){
    communicator.sendMessage('pong', MessageType.RESPONSE, message.messageID)
}
communicator.addMessageListener(this.&listen, MessageType.REQUEST)

//script2.groovy
communicator = getCommunicator 'test-communicator'
def response = communicator.sendRequest('ping', {msg -> msg.payload}, 2000)  //2 seconds for response timeout
println response  //pong
```

### Programming attributes
Attribute declaration consists of the following parts:

* Attribute name
* Attribute type specification
* Attribute reader
* Attribute writer (optional)
* Attribute description (optional)

Skeleton of attribute declaration:
```groovy
attribute {
    name "<attribute-name>"
    description "<human-readable description>"  //optional
    type <type>
    get <getter>
    set <setter>  //optional
}
```

Example:
```groovy
attribute {
    name "DummyAttribute"
    type INT32
    get {return 42}
    set {value -> println value}
}
```

### Programming events
Event declaration consists of the following parts:

* Event category
* Event description (optional)

Skeleton of event declaration:
```groovy
event {
    name <event-category>
    description "<human-readable description>"  //optional
}
```

Example:
```groovy
event {
    name "GroovyEvent"
}
```

### Programming operations
Operation declaration consists of the following parts:

* Operation name
* Return type specification
* Format parameters
* Description (optional)
* Implementation

Skeleton of operation declaration:
```groovy
operation {
    name "<operation-name>"
    description "<human-readable description>"  //optional
    parameter "<param-name>", <param-type>  //zero or more parameters
    ...
    returns <type>
    implementation <implementation code>
}
```

Example:
```groovy
operation {
    name "CustomOperation"
    description "Test operation"
    parameter "x", INT64
    parameter "y", INT64
    returns INT64
    implementation {x, y -> x + y}
}
```

### Programming health checks
Health check can be implemented using the following declaration:
```groovy
import com.bytex.snamp.connector.health.HealthStatus

protected HealthStatus getStatus(){
  new OkStatus()
}

```

If implementation of this method is not provided then connector always return OK status.

## Information Model Mapping
This section describes mapping between Groovy data types and SNAMP Management Information Model

Groovy Data Type | Management Information Model | Example
---- | ---- | ----
**byte** | int8
**short** | int16
**int** | int32 | 42i
**long** | int64 | 42L
**float** | float32 | 3.14F
**double** | float64 | 3.14D
BigInteger | bigint | 42G
BigDecimal | bigdecimal | 42.1G
**char** | char | 'a'
String | string | "Hello, world!"
**boolean** | bool | _true_
Date | datetime | `new Date()`
javax.management.ObjectName | objectname | `new ObjectName('type=Foo')`
javax.management.openmbean.CompositeData | Dictionary | `asDictionary(key1: 67L, key2: true)`
javax.management.openmbean.TabularData | Table | `asTable([[column1: 6, column2: false], [column1: 7, column2: true]])`

## Clustering
`emitNotification` can cause duplication of notifications and receiving side in clustered environment. SNAMP doesn't provide automatic resolution of this issue for Groovy Connector. Guard `emitNotification` call with `if(activeClusterNode)` condition.
