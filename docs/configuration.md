SNAMP Configuration Guide
====
SNAMP can be configured using different ways:
* via Web Browser
* via JMX
* via HTTP
* via command-line tools

System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder.

See [Configuring Apache Karaf](http://karaf.apache.org/manual/latest/users-guide/configuration.html) for more information about Apache Karaf configuration model.

See [SNAMP Management Interface](mgmt.md) for more information about SNAMP management via JMX.

## Configuration Model
SNAMP configuration describes a set of resource adapters, resource connectors and its attributes, notifications and operations.

At high level, configuration model can be represented as a tree of configurable elements:
* _Resource Adapters_ section may contain zero or more configured adapter instances
  * `Adapter Instance Name` - unique name of the configured resource adapter
    * `System Name`: system name of the resource adapter
    * Additional configuration parameters in the form of key/value pairs
* _Managed Resources_ section may contain zero or more configured managed resources
  * `Managed Resource Name` - unique name of the managed resource. Some adapters use this name when exposing connected resource to the outside
    * `Resource Connector Name` - the system name of the resource connector
    * `Connection String` - resource-specific connection string used by Resource Connector
    * Additional configuration parameters in the form of key/value pairs
    * _Attributes_ section may contain zero or more configured attributes
    * _Events_ section may contain zero or more configured events
    * _Operations_ section may contain zero or more configured operations

_Attributes_ section:
* `Attribute Instance Name` - unique name of the attribute. Resource Connector uses this name when exposing attributes to Resource Adapters
  * `Name` - the name of the attribute declared by managed resource. Note that this name depends on the management information provided by managed resource. This is the required parameter.
  * `Read/write timeout` - timeout (in millis) used when accessing attribute value. This is the optional parameter.
  * Additional configuration parameters in the form of key/value pairs

_Events_ section:
* `Event Name` - unique name of the event. Resource Connector uses this name as notification type when exposing notifications to Resource Adapters
  * `Category` - the event category declared by managed resource. Note that category depends on the management information provided by managed resource. This is the required parameter
  * Additional configuration parameters in the form of the key/value pairs

_Operations_ section may contain zero:
* `Operation instance name` - unique name of the operation. Resource Connector uses this name when exposing operations to Resource Adapters
  * `Operation Name` - the name of the operation declared by managed resource. Note that operation name depends on the management information provided by managed resource. This is the required parameter
  * Additional configuration parameters in the form of the key/value pairs

A set of additional configuration parameters depends on the particular Resource Adapter or Resource Connector.

Let's consider the following example of the configuration model:
* _Resource Adapter_
  * `Adapter Instance Name`: adapter1
    * `System Name`: http
    * `dateFormat`: MM/DD/YYYY
  * `Adapter Instance Name`: adapter2
    * `System Name`: http
    * `notificationTransport`: WebSockets
  * `Adapter Instance Name`: adapter3
    * `System Name`: xmpp
    * `userName`: user
    * `password`: pwd
    * `host`: jabber.test.com
    * `enableM2M`: true
* _Managed Resources_
  * `Managed Resource Name`: partner-gateway
    * `Resource Connector Name`: jmx
    * `Connection String`: service:jmx:rmi:///jndi/rmi://localhost:1099/glassfish
    * `login`: jmxLogin
    * `password`: jmxPassword
    * _Attributes_
      * `Attribute Instance Name`: freeMemory
        * `Name`: freeMemoryInMB
        * `Read/write timeout`: 2000
        * `objectName`: com.sun.glassfish.management:type=Memory
        * `xmppFormat`: human-readable
      * `Attribute Instance Name`: freeMemoryRaw
        * `Name`: freeMemoryInMB
        * `objectName`: com.sun.glassfish.management:type=Memory
        * `xmppFormat`: binary
      * `Attribute Instance Name`: available
        * `Name`: isActive
        * `objectName`: com.sun.glassfish.management:type=Common
        * `xmppFormat`: human-readable
    * _Events_
      * `Event Name`: error
        * `Category`: com.sun.glassfish.ejb.unhandledException
        * `objectName`: com.sun.glassfish.management:type=Common
        * `fullStackTrace`: true

In this example single managed resource named _partner-gateway_ connected via JMX protocol. The managed resource contains two MBeans (`Memory` and `Common`) with attributes and JMX notifications. This managed resource can be managed via two protocols: `http` and `xmpp` (Jabber). It is possible because configuration model contains three configured resource adapters. Two adapters have the same `http` system name. But these two adapters configured with different set of parameters. Each `http` adapter exposes REST API on its own URL context (`http://localhost/snamp/adapters/http/adapter1` and `http://localhost/snamp/adapters/http/adapter2`). The third resource adapter allows to managed resources via Jabber client (such as Miranda IM).

Managed Resource `partner-gateway` is connected using `jmx` Resource Connector. This example demonstrates how to expose two JMX attributes with the same in MBean (`freeMemoryMB`) with different settings and instance names. Both attributes are visible from `http` resource adapter using the following URLs:
* `http://localhost/snamp/adapters/http/adapter1/attributes/freeMemoryRaw`
* `http://localhost/snamp/adapters/http/adapter2/attributes/freeMemoryRaw`
* `http://localhost/snamp/adapters/http/adapter1/attributes/freeMemory`
* `http://localhost/snamp/adapters/http/adapter2/attributes/freeMemory`

XMPP Adapter provides value of the `freeMemoryRaw` and `freeMemory` in two different formats: human-readable and binary. This behavior is defined using `xmppFormat` configuration property associated with each attribute.

As you can see, configuration parameters depends on the Resource Adapter and Resource Connector. See [Configuring Resource Adapters](adapters/introduction.md) and [Configuring Resource Connectors](connectors/introduction.md) for more details about SNAMP configuration.

## Using SNAMP Management Console
SNAMP Management Console allows you to configure and maintain SNAMP via user-friendly Web interface in your browser.
> SNAMP Management Console available in paid subscription only

The console supports the following configuration features:
* Highlight the available Resource Adapters
* Highlight the available Resource Connectors
* Highlight the available configuration properties
* Discovers available attributes, events and operations
* Start, stop and restart resource adapters and connectors

SNAMP Management Console build on top of powerful [hawt.io](http://hawt.io) web console.

## Without SNAMP Management Console
There are the following ways to change the SNAMP configuration:
* Using JMX tool such as JConsole or VisualVM
* Using JMX command-line tool such as [jmxterm](http://wiki.cyclopsgroup.org/jmxterm/)
* Using `curl` utility

## Predefined configuration parameters
SNAMP Configuration Model provides a set of optional configuration parameters with predefined semantics.

Parameter | Applied to | Meaning
---- | ---- | ----
`severity` | Event configuration | Overrides severity of notification supplied by managed resource

The possible values of `severity` parameter (in descending order):

Value | Description
---- | ----
panic | A `panic` condition usually affecting multiple apps/servers/sites. At this level it would usually notify all tech staff on call.
alert | Should be corrected immediately, therefore notify staff who can fix the problem. An example would be the loss of a primary ISP connection.
critical | Should be corrected immediately, but indicates failure in a secondary system, an example is a loss of a backup ISP connection.
error | Non-urgent failures, these should be relayed to developers or admins; each item must be resolved within a given time.
warning | Warning messages, not an error, but indication that an error will occur if action is not taken, e.g. file system 85% full - each item must be resolved within a given time.
notice | Events that are unusual but not error conditions - might be summarized in an email to developers or admins to spot potential problems - no immediate action required.
informational | Normal operational messages - may be harvested for reporting, measuring throughput, etc. - no action required.
debug | Info useful to developers for debugging the application, not useful during operations.

## Examples
* [Monitoring JMX resources over SNMP](jmx-over-snmp.md)
* [Monitoring SNMP resources over HTTP](snmp-over-http.md)
* [Monitoring JMX resources over XMPP](jmx-over-xmpp.md)
