Groovy Resource Connector
====

Groovy connector allows to monitor and manage your IT resources using Groovy scripts. It can be useful in following cases:
* SNAMP doesn't provide Resource Connector for the specific protocol out-of-the-box. Possible uses cases:
  * Parsing log data and exposing results as an attributes
  * Extracting records from databases and exposing results as attributes
  * Processing Web resources (or REST services) via HTTP (JSON, AtomPub and etc)
  * Listening message queues (or topics) via JMS and exposing processed messages as SNAMP notifications
* Customize existing Resource Connectors
  * Aggregate two or more attributes (sum, average, peak, percent) and expose result as an attribute
  * Aggregate notifications

> Groovy connector is based on Groovy 2.4.3

The connector has following architecture:
* Each attribute must be represented as a separated Groovy script. The attribute name will be interpreter as a script file name. For example, the logic for attribute `Memory` must be placed into `Memory.groovy` file
* Each event must be represented as a separated Groovy script. The event category will be interpreted as a script file name. For example, the logic for event `Error` must be placed into `Error.groovy` file
* Optionally, you may write initialization script that is used to initialize instance of the Managed Resource Connector
* Each instance of the Groovy Connector provides a sandbox for Groovy scripts. So, initialization script will be executed for each instance of the connector. You can't share objects between instances of Groovy Connector

## Connection String
Connection string specifies set of paths with Groovy scripts. You may specify more than one path using OS-specific path separator symbol:
* `:` for Linux
* `;` for Windows

For example, `/usr/local/snamp/groovy:/usr/local/snamp/scripts`

Path is used to find initialization, attribute and event scripts.

## Configuration parameters
JMX Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
initScript | String | No | Name of the initialization script file | `init.groovy`
groovy.warnings | String | No | Groovy warning level | `likely errors`
groovy.source.encoding | String | No | Encoding to be used when reading Groovy source files
groovy.classpath | String | No | Classpath using to find third-party JARs | `/usr/local/jars:/home/user/jars`
groovy.output.verbose | Boolean (`true` or `false`) | No | Turns verbose operation on or off | `true`
groovy.output.debug | Boolean | No | Turns debugging operation on or off | `false`
groovy.errors.tolerance | Integer | No | Sets the error tolerance, which is the number of non-fatal errors (per unit) that should be tolerated before compilation is aborted

All these parameters (including user-defined) will be visible as global variables within all the available scripts.

## Configuring attributes
Each attribute configured in Groovy Resource Connector has following configuration schema:
* `Name` - name of script file without `.groovy` file extension. The file must exists in the paths specified by connection string
* There is no predefined configuration parameters. But all user-defined configuration parameters will be visible as global variables in the attribute script only.

For more information see **Programming attributes** section.

## Configuring events
Each event configured in Groovy Resource Connector has following configuration schema:
* `Category` - name of script file without `.groovy` file extension. The file must exists in the paths specified by connection string
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
severity | String | No | Overrides severity level of the emitted notification | `warning`

All user-defined configuration parameters will be visible as global variables in event script only.

## Scripting
Groovy Resource Connector provides following features for Groovy scripting:
* Simple DSL extensions of Groovy language
* Accessing to attributes and notifications of any other connected managed resources
* Full [Grape](http://www.groovy-lang.org/Grape) support so you can use any Groovy module or Java library published in Maven repository

Each instance of the Groovy Resource Connector has isolated sandbox with its own Java class loader used for Groovy scripts.

All configuration parameters specified at the resource-level will be visible for all scripts. For example, you have configured `initScript` and `customParam` parameters. The value of these parameters can be obtained as follows:
```groovy
println initScript
println customParam
```

Groovy connector provides following DSL extensions accessible from any type of scripts:
* Global variables:
  * `resourceName` - contains the name of the managed resource as it specified in the SNAMP configuration. This variable is not available in the discovery mode
* Logging subrouties (these routines written on top of OSGi logging infrastructure)
  * `void error(String message)` - report about error
  * `void warning(String message)` - report warning
  * `void info(String message)` - report information
  * `void fine(String message)` - report trace
  * `void debug(String message)` - report debugging information
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
  * `Communicator getCommunicator(String sessionName)` - get or create a new communication session
  * `MessageListener asListener(Closure listener)` - wraps Groovy closure into communication message listener
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
//the code above is equivalent to
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
communicator.register(asListener { msg -> println msg})
//in other script file
def communicator = getCommunicator 'test-communicator'
communicator.post 'Hello, world!'
```

Synchronous messaging using communicator:
```groovy
//script1.groovy
communicator = getCommunicator 'test-communicator'
def listen(message){
    communicator.post('pong')
}

communicator.register(asListener this.&listen)
//script2.groovy
communicator = getCommunicator 'test-communicator'
def response = communicator.post('ping', 2000)  //2 seconds for response timeout
println response  //pong
```

> Read **SNAMP Management Information Model** before you continue

### Initialization script
Initialization script used to initialize instance of the connector. Name of the initialization script must be specified in the managed resource configuration explicitly. If it is not specified then Groovy Connector doesn't perform any extra initialization activities.

As the best practice, initialization script can be used for declaring references to third-party modules and libraries:
```groovy
@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.3')
@GrabConfig(initContextClassLoader = true)
import groovy.json.JsonSlurper
```

> Note that `@GrabConfig(initContextClassLoader = true)` must be used in conjunction with every `@Grab` annotation

Initialization script supports additional DSL extensions:
* Global variables:
  * `discovery` - determines whether initialization script is in discovery mode
* Discovery services
  * `void attribute(String name, Map parameters)` - declares new attribute with the specified name and default configuration parameters. This declaration will be displayed in the SNAMP Management Console while discovering the available attributes. The functionality of the attribute doesn't depend on this declaration
  * `void event(String category, Map parameters)` - declares new event with the specified category and default configuration parameters. This declaration will be displayed in the SNAMP Management Console while discovering the available events. The functionality of the event doesn't depend on this declaration

Special functions that can be declared in the script:
* `void close()` - called by SNAMP automatically when the resources acquired by the instance of the connector should be released

Following example demonstrates simple initialization script:
```groovy
@Grab(group = 'org.codehaus.groovy', module = 'groovy-json', version = '2.4.3')
@GrabConfig(initContextClassLoader = true)
import groovy.json.JsonSlurper

attribute 'DummyAttribute', []
attribute 'MemoryAttrubute', [precision: 'MB']
```

Example of initialization script with special functions:
```groovy
connection = createConnection() //createConnection is not a part of DSL. It is just example

void close(){
  connection.close()
}
```

### Programming attributes
Attribute script consists of the following parts:
* Attribute type specification
* Attribute initialization
* Attribute reader
* Attribute writer

All configuration parameters assigned to the attribute in SNAMP configuration will be visible as global variables in the attribute script.

Attribute script supports additional DSL extensions:
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
* Functions:
  * `void type(OpenType t)` - define attribute type
  * `ArrayType ARRAY(OpenType elementType)` - construct array type
  * `CompositeType DICTIONARY(String typeName, String typeDescription, Map items)` - construct dictionary type
  * `TabularType TABLE(String typeName, String typeDescription, Map[] columns)` - construct table type
  * `boolean isReadable()` - determines whether the attribute is readable. This method returns `true` if function `getValue` is defined in the script
  * `boolean isWritable()` - determines whether the attribute is writable. This method returns `true` if function `getValue` is defined in the script
* Data type converters:
  * `Map asDictionary(CompositeData dict)` - convert JMX composite data (dictionary in SNAMP terminology) to Map (key/value pairs). This method can be used in `setValue` function to convert input value to Map
  * `CompositeData asDictionary(Map dict)` - convert key/values pairs to JMX composite data (dictionary). This method can be used in `getValue` function to convert input value to CompositeData
  * `Map[] asTable(TabularData table)` - convert JMX tabular data (table) to a collection of rows. This method can be used in `setValue` function to convert input value to rows
  * `TabularData asTable(Map[] rows)` - convert a collection of rows into JMX tabular data (table in SNAMP terminology). This method can be used in `getValue` function to convert input value to TabularData

Special functions that can be declared in the script:
* `Object getValue()` - called by SNAMP automatically to read attribute value. If this function is not specified then the attribute is write-only
* `Object setValue(Object value)` - called by SNAMP automatically to write attribute value. If this function is not specified then the attribute is read-only
* `void close()` - called by SNAMP automatically when the resources acquired by instance of the attribute should be released

Following code describes skeleton of the attribute script:
```groovy
type INT64 //attribute type specification

//initialization code
println 'Initialized'

//attribute value reader. This declaration is optional
def getValue(){
    return 42L
}

//attribute value writer. This declaration is optional
def setValue(value){
  println value
}

//this declaration is optional
void close(){
  //cleanup code
}

```

Attribute of tabular data type (table):
```groovy
type TABLE('GroovyTable', 'desc', [column1: [type: INT32, description: 'descr', indexed: true], column2: [type: BOOL, description: 'descr']])

def getValue(){
    asTable([[column1: 6, column2: false], [column1: 7, column2: true]])
}

def setValue(value){
    println asTable(value)
}

```

Attribute of dictionary type:
```groovy
type DICTIONARY('GroovyType', 'GroovyDescr', [key1: [type: INT64, description: 'descr'], key2: [type: BOOL, description: 'descr']])

def getValue(){
    asDictionary(key1: 67L, key2: true)
}

def setValue(value){
    println asDictionary(value)
}
```

### Programming events
Event script consists of the following parts:
* Event initialization
* Event emitting

All configuration parameters assigned to the event in SNAMP configuration will be visible as global variables in the event script.

Event script supports additional DSL extensions:
* Functions:
  * `void emitNotification(String message)` - emit outgoing notification with the specified human-readable message
  * `void emitNotification(String messagem, Object userData)` - emit outgoing notification with the specified human-readable message and additional payload

Special functions that can be declared in the script:
  * `void close()` - called by SNAMP automatically when the resources acquired by instance of the event should be released

Following example demonstrates reading attribute from another connected resource and send its value as a notification payload in periodic manner:
```groovy
def emitter = {
  def value = getResourceAttribute 'app-server', 'freeMemory'
  emitNotification 'Amount of free memory', value
}

def timer = createTimer emitter, 1000
timer.run()

void close(){
  timer.close()
}
```

Note that notification payload must have the type supported by Management Information Model

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
