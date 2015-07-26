SNAMP Configuration Guide
====
SNAMP can be configured using different ways:
* via Web Browser
* via JMX
* via HTTP
* via command-line tools

System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder or commands of Karaf shell console.

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

### Smart mode
Some resource connectors can expose attributes, events or operations without its manual configuration. In this case the connector automatically exsposes all attributes, events or operations. So, these resource features will be accessible at runtime event if configuration section with attributes and events is empty. To enable smart mode of the connector you should specify `smartMode = true` in the configuration parameters of the resource.

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
* Using HTTP protocol and `curl` or `wget` utility

For JMX-compliant tool you need establish connection to SNAMP Managed Bean and read/write `configuration` attribute.

For HTTP-based communication, use `curl` utility. The first, verify that JMX-HTTP bridge is accessible:

```bash
curl http://localhost:8181/jolokia
{"timestamp":1433451551,"status":200,"request":{"type":"version"},"value":{"protocol":"7.2","config":{"useRestrictorService":"false","canonicalNaming":"true","includeStackTrace":"true","listenForHttpService":"true","historyMaxEntries":"10","agentId":"192.168.1.51-25946-69e862ec-osgi","debug":"false","realm":"jolokia","serializeException":"false","agentContext":"\/jolokia","agentType":"servlet","policyLocation":"classpath:\/jolokia-access.xml","debugMaxEntries":"100","authMode":"basic","mimeType":"text\/plain"},"agent":"1.3.0","info":{"product":"felix","vendor":"Apache","version":"4.2.1"}}}

```
If HawtIO is already installed then you should use http://localhost:8181/hawtio/jolokia path. Otherwise, Jolokia Basic Authentication need to be configured:
1. Create `org.jolokia.osgi.cfg` file in `<snamp>/etc` directory
2. Put the following configuration properties:
```
org.jolokia.agentContext=/jolokia
org.jolokia.realm=karaf
org.jolokia.user=karaf
org.jolokia.authMode=jaas
```
3. Restart Jolokia bundle or SNAMP

The second, obtain SNAMP configuration:
```bash
curl -u karaf:karaf http://localhost:8181/jolokia/read/com.bytex.snamp.management:type=SnampCore/configuration?maxDepth=20&maxCollectionSize=500&ignoreErrors=true&canonicalNaming=false

{"timestamp":1433455091,"status":200,"request":{"mbean":"com.bytex.snamp.management:type=SnampCore","attribute":"configuration","type":"read"},"value":null}
```
> If you have 403 error then read [this](http://modio.io/jolokia-in-karaf-3-0-x-fixing-the-403-access-error/) article

`value` field in the resulting JSON holds SNAMP configuration in the form of the JSON tree. `null` means that SNAMP configuration is empty. JSON structure of the SNAMP configuration repeats its logical structure described above.

The following example shows setup of JMX-to-SNMP bridge:
```javascript
{
  "ResourceAdapters": {
    "test-snmp": {  //adapter instance name
      "UserDefinedName": "test-snmp", //adapter instance name
      "Adapter": {
        "Name": "snmp",   //adapter's system name
        "Parameters": { //adapter-level configuration parameters
          "port": {
            "Value": "3222",
            "Key": "port"
          },
          "host": {
            "Value": "127.0.0.1",
            "Key": "host"
          },
          "socketTimeout": {
            "Value": "5000",
            "Key": "socketTimeout"
          },
          "context": {
            "Value": "1.1",
            "Key": "context"
          }
        }
      }
    }
  },
  "ManagedResources": {
    "test-target": {  //managed resource name
      "Connector": {
        "Parameters": { //managed resource configuration parameters
          "login": {
            "Value": "karaf",
            "Key": "login"
          },
          "password": {
            "Value": "karaf",
            "Key": "password"
          }
        },
        "ConnectionString": "service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root",
        "Attributes": { //a set of connected attributes
          "attribute1": { //user-defined attribute name
            "Attribute": {
              "Name": "int32",  //name of the attribute in remote MBean
              "AdditionalProperties": {
                "objectName": {
                  "Value": "com.bytex.snamp:type=TestManagementBean",
                  "Key": "objectName"
                },
                "oid": {
                  "Value": "1.1.3.0",
                  "Key": "oid"
                }
              },
              "ReadWriteTimeout": 9223372036854776000
            },
            "UserDefinedName": "attribute1"
          },
          "attribute2": {
            "Attribute": {
              "Name": "dictionary",
              "AdditionalProperties": {
                "objectName": {
                  "Value": "com.bytex.snamp:type=TestManagementBean",
                  "Key": "objectName"
                },
                "oid": {
                  "Value": "1.1.6.1",
                  "Key": "oid"
                }
              },
              "ReadWriteTimeout": 9223372036854776000
            },
            "UserDefinedName": "attribute2"
          },
          "attribute3": {
            "Attribute": {
              "Name": "bigint",
              "AdditionalProperties": {
                "objectName": {
                  "Value": "com.bytex.snamp:type=TestManagementBean",
                  "Key": "objectName"
                },
                "oid": {
                  "Value": "1.1.4.0",
                  "Key": "oid"
                }
              },
              "ReadWriteTimeout": 9223372036854776000
            },
            "UserDefinedName": "attribute3"
          }
        },
        "Events": {
          "19.1": { //user-defined name of the notification
            "Event": {
              "Category": "jmx.attribute.change", //name of the JMX notification in MBean
              "AdditionalProperties": {//event configuration parameters
                "objectName": {
                  "Value": "com.bytex.snamp:type=TestManagementBean",
                  "Key": "objectName"
                },
                "oid": {
                  "Value": "1.1.19.1",
                  "Key": "oid"
                },
                "receiverName": {
                  "Value": "test-receiver-1",
                  "Key": "receiverName"
                },
                "severity": {
                  "Value": "notice",
                  "Key": "severity"
                },
                "receiverAddress": {
                  "Value": "127.0.0.1/63778",
                  "Key": "receiverAddress"
                }
              }
            },
            "UserDefinedName": "19.1" //user-defined name of the notification
          },
        },
        "ConnectionType": "jmx"   //type of the managed resource connector
      },
      "UserDefinedName": "test-target"  //managed resource name
    }
  }
}
```
JSON format of SNAMP configuration is just a mapping between JMX data type and JSON. This mapping is implemented using [Jolokia](https://jolokia.org/) library. JMX-to-JSON mapping protocol is described [here](https://jolokia.org/reference/html/protocol.html).

If your SNAMP configuration is ready then save JSON into the file and use `curl` utility to setup a new configuration:
```bash
curl -u karaf:karaf -X POST -d @config.json http://localhost:8181/jolokia/read/com.bytex.snamp.management:type=SnampCore/configuration?maxDepth=20&maxCollectionSize=500&ignoreErrors=true&canonicalNaming=false
```

## Predefined configuration parameters
SNAMP Configuration Model provides a set of optional configuration parameters with predefined semantics.

Parameter | Applied to | Meaning
---- | ---- | ----
severity | Event configuration | Overrides severity of notification supplied by managed resource
minPoolSize | Managed Resource or Resource Adapter configuration | The number of threads to keep in the pool, even if they are idle
maxPoolSize | Managed Resource or Resource Adapter configuration | The maximum number of threads to allow in the pool
queueSize | Managed Resource or Resource Adapter configuration | The maximum number of waiting input requests
keepAliveTime | Managed Resource or Resource Adapter configuration | when the number of threads is greater than the `minPoolSize`, this is the maximum time (in millis) that excess idle threads will wait for new tasks before terminating
priority | Managed Resource or Resource Adapter configuration | Priority of all threads in the thread pool
units | Attribute configuration | Unit of measurement (UOM) of the attribute value. For example: `ms`, `m`, `kg`, `MB`
defaultValue | Attribute configuration | The default value of the attribute if the actual value is not available
minValue | Attribute configuration | The minimum (exclusive) permitted value for the attribute
maxValue | Attribute configuration | The maximum (exclusive) permitted value for the attribute
smartMode | Resource configuration | Enable or disable smart mode of the connector. The possible values are `true` or `false`

### `severity` parameter
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

### Thread pool configuration parameters
Some Resource Connectors and Adapters supports explicit configuration of its internal thread pool. All related configuration parameters are optional therefore you may specify some of them. But you should take into account the following restrictions:
* `minPoolSize` must be less than `maxPoolSize`
* If `queueSize` is not specified explicitly then SNAMP component uses unlimited capacity of the queue
* It is not recommended to set `keepAliveTime` to zero due to performance penalties
* If `priority` is not specified then SNAMP uses default OS priority for threads in pool
* `priority` must be is in range _1..10_. Note that _1_ is the lowest priority.

## Configuring OSGi
All configuration files located in `<snamp>/etc` directory. These files supply a low-level access to Apache Karaf configuration.

### Logging
Apache Karaf and SNAMP logs located in `<snamp>/data/log` folder. You can configure log rotation, severity level and other logging settings using the following configurations files in `<snamp>/etc` directory:
* `org.ops4j.pax.logging.cfg` - initial log configuration (appenders, levels, log message format)
* `java.util.logging.properties` - advanced configuration properties for standard Java logging. It is not recommended to change this file
* `org.apache.karaf.log.cfg` - display configuration of the log records in the shell console

See [Karaf Log Configuration](http://karaf.apache.org/manual/latest/users-guide/log.html) for more details.

## Examples
* [Monitoring JMX resources over SNMP](examples/jmx-over-snmp.md)
* [Monitoring SNMP resources over HTTP](examples/snmp-over-http.md)
