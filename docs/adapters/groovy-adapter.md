Groovy Resource Adapter
====
Groovy Resource Adapter allows to integrate any third-party management software with SNAMP using Groovy script. The script program has access to all management information provided by connected managed resources. It is very useful is the following cases:
* Applying complex analysis on metrics
* Applying validation rules on attributes
* Processing notifications
* Sending alerts via e-mail or SMS to engineers
* Storing metrics and notifications into external database

## Configuration Parameters
Groovy Resource Adapter recognizes the following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
scriptFile | string | Yes | The name of the boot script | `Adapter.groovy`
scriptPath | string | Yes | A collection of paths with groovy scripts | `/usr/local/snamp/groovy:/usr/local/snamp/scripts`

You may specify more than one path in `scriptPath` parameter using OS-specific path separator symbol:
* `:` for Linux
* `;` for Windows

Any other user-defined configuration property will be visible inside from Groovy script as a global variable.

## Scripting
Groovy Resource Adapter provides the following features for Groovy scripting:
* Simple DSL extensions of Groovy language
* Accessing to attributes and notifications of all connected managed resources
* Full [Grape](http://www.groovy-lang.org/Grape) support so you can use any Groovy module or Java library published in Maven repository

Each instance of the Groovy Resource Adapter has isolated sandbox with its own Java class loader used for Groovy scripts.

### Global variables
All configuration parameters specified at adapter-level will be visible to all scripts. For example, you have configured `scriptFile` and `customParam` parameters. The value of these parameters can be obtained as follows:
```groovy
println scriptFile
println customParam
```

Other useful predefined global variables:

Name | Type | Description
---- | ---- | ----
hostedResources | Set of strings | A collection of connected managed resources

### Special functions
Special functions that you can declare in your script:

Declaration | Description
---- | ----
void close() | Called by SNAMP automatically when the resources acquired by instance of the adapter should be released
def handleNotification(metadata, notif) | Catches notification emitted by one of the connected managed resources. This function is calling asynchronously

The following example shows how to handle a notification:
```groovy
def handleNotification(metadata, notif){
  def source = notif.source
  def message = notif.message
  def category = metadata.notificationCategory
  println "[${source}](${category}): ${message}"
}
```

### Predefined functions
Predefined functions allows you to interact with connected managed resources, log events, communicate with other scripts and etc.

### Logging API:

Function | Description
---- | ----
error(String msg) | Reports about error
warning(String msg) | Reports about warning
info(String msg) | Emits informational message
debug(String msg) | Emits debug message
fine(String msg) | Emits trace message

### Access to Managed Resources

Function | Description
---- | ----
Set&lt;String&gt; getResourceAttributes(String resourceName) | Gets resource attributes
Set&lt;String&gt; getResourceEvents(String resourceName) | Gets resource events
Object getAttributeValue(String resourceName, String attributeName) | Gets value of the attribute
setAttributeValue(String resourceName, String attributeName, Object value) | Sets value of the attribute
ManagedResourceConnectorClient getManagedResource(String resourceName) | Gets a reference to the connected managed resource
void releaseManagedResource(ManagedResourceConnectorClient resource) | Releases a reference to the connected managed resource
Collection&lt;MBeanAttributeInfo&gt; getAttributes(String resourceName) | Gets metadata of resource attributes
Collection&lt;MBeanNotificationInfo&gt; getNotifications(String resourceName) | Gets metadata of resource events
void processAttributes(Closure action) | Processes all attributes sequentially. May be used for map/reduce.
void processEvents(Closure action) | Processes all events sequentially

Read attribute value using resoure client:
```groovy
def resource = getManagedResource 'java-app-server'
def memory = resource.getAttribute 'memory'
releaseManagedResource resource
```

Read all attributes:
```groovy
processAttributes({resourceName, metadata, value -> println "${metadata.name} = ${value}" })
```

Read all events:
```groovy
processEvents({resourceName, metadata -> println "${metadata.notifTypes}"})
```

### Miscellaneous API

Function | Description
---- | ----
Communicator getCommunicator(String name) | Gets or creates communication session. The communicator is very useful for inter-script lightweight communication
MessageListener asListener(Closure action) | Wraps Groovy closure into communication message listener
Timer createTimer(Closure action, long period) | Creates a new timer that execute the specified task periodically
Timer schedule(Closure action, long period) | Execute the specified task periodically in the background

Working with timers:
```groovy
def timer = createTimer({ println 'Tick' }, 300)  //will print 'Tick' every 300 milliseconds
timer.run() //start printing
timer.close() //stop printing
//the code above is equivalent to
def timer = schedule({ println 'Tick' }, 300)
timer.close()
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

### Real-time analysis
Groovy Resource Adapter provides two declarative analyzers:
* Attributes analyzer used to analyze attribute values in periodically manner
* Events analyzer used to analyze notifications on-the-fly

The following example demonstrates how to enable periodic collection and analysis of attributes:
```groovy
def analyzer = attributesAnalyzer 4000 //creates analyzer for attributes with 4 sec check period
with analyzer{
  //filter by configuration parameter and actual attribute value
  select "(type=adminDefined)" when {value -> value > 10} then {println it} failure {println it}
  //fallback condition
  select "(type=*)" when {value -> true} then {println it}
}
analyzer.run()  //execute analysis

void close(){
  analyzer.close()  //it is recommended to release all resources associated with analyzer
}
```
_RFC 1960_-based filter used to filter attributes by its configuration parameters.

The following example demonstrates how to enable events analyzer
```groovy
def analyzer = eventsAnalyzer()
with analyzer{
  select "(severity=warning)" when {notif -> notif.source == "resource"} then {metadata, notif -> println notif.message}
}
```
