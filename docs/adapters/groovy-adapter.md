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
scriptFile | string | true | The name of the boot script | `Adapter.groovy`
scriptPath | string | true | A collection of paths with groovy scripts | `/usr/local/snamp/groovy:/usr/local/snamp/scripts`

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

Logging API:

Function | Description
---- | ----
error(String msg) | Reports about error
warning(String msg) | Reports about warning
info(String msg) | Emits informational message
debug(String msg) | Emits debug message
fine(String msg) | Emits trace message
