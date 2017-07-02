Groovy Gateway
====
Groovy Gateway allows to integrate any third-party management software with SNAMP using Groovy script. The script program has access to all management information provided by connected managed resources. It can be useful in the following cases:
* Applying complex analysis on metrics
* Applying validation rules on attributes
* Processing notifications
* Sending alerts via e-mail or SMS to engineers
* Storing metrics and notifications into external database

## Configuration Parameters
Groovy Gateway recognizes following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
scriptFile | string | Yes | Name of the boot script | `Gateway.groovy`
scriptPath | string | Yes | Collection of semicolon-separated URLs with groovy scripts | `file:/usr/local/snamp/groovy;file:/usr/local/snamp/scripts`

Any other user-defined configuration property will be visible inside from Groovy script as a global variable.

## Scripting
Groovy Gateway provides following features for Groovy scripting:
* DSL extensions of Groovy language
* Accessing attributes and notifications of all connected managed resources
* Full [Grape](http://www.groovy-lang.org/Grape) support so you can use any Groovy module or Java library published in Maven repository

Each instance of the Groovy Gateway has isolated sandbox with its own Java class loader used for Groovy scripts.

### API
Special functions that you can declare in your script:

Declaration | Description
---- | ----
void close() | Called by SNAMP automatically when the resources acquired by instance of the gateway should be released
def handleNotification(metadata, notif) | Catches notification emitted by one of the connected managed resources. This function is being called asynchronously

The following example shows how to handle a notification:
```groovy
def handleNotification(metadata, notif){
  def source = notif.source
  def message = notif.message
  def category = metadata.notificationCategory
  println "[${source}](${category}): ${message}"
}
```

#### Global variables
All configuration parameters specified at gateway-level will be visible to all scripts. For example, you have configured `scriptFile` and `customParam` parameters. Value of these parameters can be obtained as follows:
```groovy
println scriptFile
println customParam
```

Other useful predefined global variables:

Name | Type | Description
---- | ---- | ----
resources | Groovy object | Root object that exposes access to all connected resources. That is a root of all DSL extensions
logger | java.util.logging.Logger | Logger object

#### Logging

Function | Description
---- | ----
lo

#### Batch processing

Function | Description
---- | ----
void processAttributes(Closure action) | Processes all attributes sequentially. May be used for map/reduce.
void processEvents(Closure action) | Processes all events sequentially
Object eventsAnalyzer() | Creates new instance of real-time analyzer for all incoming notifications
Object attributesAnalyzer(long checkPeriod) | Creates new instance of real-time analyzer for all attributes

Read all attributes:
```groovy
processAttributes({resourceName, metadata, value -> println "${metadata.name} = ${value}" })
```

Read all events:
```groovy
processEvents({resourceName, metadata -> println "${metadata.notifTypes}"})
```

The following example demonstrates how to enable periodic collection and analysis of attributes:
```groovy
def analyzer = attributesAnalyzer 4000 //creates analyzer for attributes with 4 sec check period
analyzer.with {
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

Following example demonstrates how to enable events analyzer
```groovy
def analyzer = eventsAnalyzer()
analyzer.with {
  select "(severity=warning)" when {notif -> notif.source == "resource"} then {metadata, notif -> println notif.message}
}

def handleNotification(metadata, notif){
  analyzer.handleNotification(metadata, notif)
}
```

#### Miscellaneous

Function | Description
---- | ----
Communicator getCommunicator(String name) | Gets or creates communication session. The communicator can be useful for inter-script lightweight communication
MessageListener asListener(Closure action) | Wraps Groovy closure into communication message listener
Repeater createTimer(Closure action, long period) | Creates a new timer that execute the specified task periodically
Repeater schedule(Closure action, long period) | Executes specified task periodically in the background

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

### DSL
Groovy Gateway provides convenient way to work with connected resources and its features. Each resource and its features are available as properties of Groovy objects:

* `resources.resName` - gets the access to the connected resource named as `resName` is SNAMP configuration
* `resources.getResource("resName")` - the same as above
* `resources.resName.metadata.configProperty` - gets the value of the configuration parameter declared in the configuration section for resource with user-defined name `resName`
* `resources.resName.entityName` - gets the access to the feature of resource `resName`. This property can return attribute or event and its behavior depends on the user-defined name of the attribute or event used in SNAMP configuration
* `resources.resName.attributes` - gets the collection of all attributes exposed by resource `resName`
* `resources.resName.events` - gets the collection of all events exposed by resource `resName`
* `resources.resName.getAttribute("attrName")` - gets access to the attribute `attrName`. The same as `resources.resName.attrName`
* `resources.resName.getEvent("eventName")` - gets access to the event `eventName`. The same as `resources.resName.eventName`
* `resources.resName.attrName.value` - gets or sets value of the attribute if `attrName` is a name of an attribute (not event)
* `resources.resName.attrName.metadata.configProperty` - gets value of the configuration parameter declared in the configuration section for attribute `attrName`
* `resources.resName.eventName.configProperty` - gets value of the configuration parameter declared in the configuration section for attribute `attrName`

Following example demonstrates how to DSL extensions:
```groovy
def appServer = resources.appServer
if(appServer.availableMemory.value < 10000) {
    smtp.sendEmail(appServer.metadata.adminAddress, "Not enough memory on app server!")
}

if(appServer.logLevel.metadata.writable){
  appServer.logLevel.value = "SEVERE"
}
```
