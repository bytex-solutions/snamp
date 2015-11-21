SNAMP Configuration Guide
====
SNAMP can be configured using different ways:

* via Web Browser
* via JMX
* via HTTP
* via command-line interface

System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder or Karaf shell console commands.

See [Configuring Apache Karaf](http://karaf.apache.org/manual/latest/users-guide/configuration.html) for more information about Apache Karaf configuration model.

See [SNAMP Management Interface](mgmt.md) for more information about SNAMP management via JMX.

## Configuration Model
SNAMP configuration describes set of resource adapters, resource connectors and its attributes, notifications and operations.

At high level, logical model of configuration can be represented as a configurable elements tree:

* _Resource Adapters_ section may contain zero or more configured adapter instances
  * `Adapter Instance Name` - unique name of the configured resource adapter
    * `System Name`: system name of the resource adapter
    * Additional configuration parameters in the form of key/value pairs
* _Managed Resources_ section may contain zero or more configured managed resources
  * `Managed Resource Name` - unique name of the managed resource. Some adapters use this name when exposing connected resource to the outside
    * `Resource Connector Name` - system name of the resource connector
    * `Connection String` - resource-specific connection string used by Resource Connector
    * Additional configuration parameters in the form of key/value pairs
    * _Attributes_ section may contain zero or more configured attributes
    * _Events_ section may contain zero or more configured events
    * _Operations_ section may contain zero or more configured operations

_Attributes_ section:

* `Attribute Instance Name` - unique name of the attribute. Resource Connector uses this name when exposing attributes to Resource Adapters
  * `Name` - name of the attribute declared by managed resource. Note that this name depends on the management information provided by managed resource. This parameter is required.
  * `Read/write timeout` - timeout (in millis) used when accessing attribute value. This is the optional parameter.
  * Additional configuration parameters in the form of key/value pairs

_Events_ section:

* `Event Name` - unique name of the event. Resource Connector uses this name as notification type when exposing notifications to Resource Adapters
  * `Category` - event category declared by managed resource. Note that the category depends on the management information provided by managed resource. This parameter is required.
  * Additional configuration parameters in the form of the key/value pairs

_Operations_ section may contain zero:

* `Operation instance name` - unique name of the operation. Resource Connector uses this name when exposing operations to Resource Adapters
  * `Operation Name` - name of the operation declared by managed resource. Note that operation name depends on the management information provided by managed resource. This parameter is required.
  * Additional configuration parameters in the form of the key/value pairs

A set of additional configuration parameters depends on the particular Resource Adapter or Resource Connector.

Let's consider the following example of the configuration logical model:

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
Some resource connectors can expose attributes, events or operations without its manual configuration. In this case the connector automatically exposes all attributes, events or operations. So, these resource features will be accessible at runtime event if configuration section with attributes and events is empty. To enable smart mode of the connector you should specify `smartMode = true` in the configuration parameters of the resource.

## Using SNAMP Management Console
SNAMP Management Console allows you to configure and maintain SNAMP via user-friendly Web interface in your browser.
> SNAMP Management Console available in paid subscription only

The console supports the following configuration features:

* [Highlight available Resource Adapters, Resource Connectors, configuration properties, attributes, events and operations](webconsole/configuration.md)
* [Start, stop and restart resource adapters and connectors](webconsole/managing.md)
* [Configure JAAS settings](webconsole/jaas.md)
* [License management](webconsole/license.md)
* [Detailed overview of platform and modules state](webconsole/general.md)

SNAMP Management Console build on top of powerful [hawt.io](http://hawt.io) web console.

## Using command-line interface
Execute SNAMP using the following command

```bash
sh ./snamp/bin/karaf
```

... and you will see the following welcome screen:
```
_____ _   _          __  __ _____  
/ ____| \ | |   /\   |  \/  |  __ \
| (___ |  \| |  /  \  | \  / | |__) |
\___ \| . ` | / /\ \ | |\/| |  ___/
____) | |\  |/ ____ \| |  | | |
|_____/|_| \_/_/    \_\_|  |_|_|

Bytex SNAMP (1.0.0)

Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.
Hit '<ctrl-d>' or type 'system:shutdown' or 'logout' to shutdown SNAMP.

snamp.root@karaf>
```

Now you can use standard Karaf commands described [here](http://karaf.apache.org/manual/latest-3.0.x/users-guide/console.html).
Also, SNAMP provides additional set of commands (started with **snamp** prefix):

Command | Description
---- | ----
snamp:adapter | Display configuration of the specified adapter instance
snamp:configure-adapter | Configure new or existing instance of adapter
snamp:configure-attribute | Configure new or existing attribute assigned to the managed resource
snamp:configure-event | Configure new or existing event (notification) assigned to the managed resource
snamp:config-operation | Configure new or existing operation (notification) assigned to the managed resource
snamp:configure-resource | Configure new or existing managed resource using the specified connector and connection string
snamp:adapter-instances | List of configured adapter instances
snamp:resources | List of configured managed resources
snamp:delete-adapter | Delete adapter instance from configuration
snamp:delete-adapter-param | Delete configuration parameter from the specified adapter instance
snamp:delete-attribute | Delete attribute from the specified managed resource
snamp:delete-attribute-param | Delete configuration parameter from the specified attribute
snamp:delete-event | Delete event (notificaiton) from the specified managed resource
snamp:delete-event-param | Delete configuration parameter from the specified event
snamp:delete-operation | Delete operation from the specified managed resource
snamp:delete-resource | Delete managed resource from configuration
snamp:delete-resource-param | Delete configuration parameter from the specified resource
snamp:dump-jaas | Save JAAS configuration in JSON format into the specified file
snamp:setup-jaas | Load JAAS configuration from the external file
snamp:installed-adapters | List of installed adapters
snamp:installed-components | List of all installed SNAMP components including adapters and connectors
snamp:installed-connectors | List of installed resource connectors
snamp:reset-config | Setup empty SNAMP configuration
snamp:resource | Show configuration of the managed resource including attributes, events and operations
snamp:restart | Restart all adapters and connectors
snamp:start-adapter | Start bundle with individual adapter
snamp:start-connector | Start bundle with individual resource connector
snamp:stop-adapter | Stop bundle with individual adapter
snamp:stop-connector | Stop bundle with individual resource connector
snamp:version | Show version of SNAMP platform
snamp:cluster-member | Status of the SNAMP cluster member

Use `--help` flag to know more information about command and its parameters:
```bash
snamp:configure-resource --help
```

## Using Management API
There are several ways to change SNAMP configuration via management API:
* Using JMX tool such as JConsole or VisualVM
* Using JMX command-line tool such as [jmxterm](http://wiki.cyclopsgroup.org/jmxterm/)
* Using HTTP protocol and `curl` or `wget` utility

For JMX-compliant tool you need establish connection to SNAMP Managed Bean and read/write `configuration` attribute.

For HTTP-based communication, use `curl` utility. The first, verify that JMX-HTTP bridge is accessible:

```bash
curl http://localhost:3535/jolokia
```
Output:
```json
{"timestamp":1433451551,"status":200,"request":{"type":"version"},"value":{"protocol":"7.2","config":{"useRestrictorService":"false","canonicalNaming":"true","includeStackTrace":"true","listenForHttpService":"true","historyMaxEntries":"10","agentId":"192.168.1.51-25946-69e862ec-osgi","debug":"false","realm":"jolokia","serializeException":"false","agentContext":"/jolokia","agentType":"servlet","policyLocation":"classpath:/jolokia-access.xml","debugMaxEntries":"100","authMode":"basic","mimeType":"text/plain"},"agent":"1.3.0","info":{"product":"felix","vendor":"Apache","version":"4.2.1"}}}
```

> If HawtIO is already installed then you can use http://localhost:3535/hawtio/jolokia path.

Now you can obtain SNAMP configuration:
```bash
curl -u karaf:karaf http://localhost:3535/jolokia/read/com.bytex.snamp.management:type=SnampCore/configuration?maxDepth=20&maxCollectionSize=500&ignoreErrors=true&canonicalNaming=false
```

Output:
```json
{"timestamp":1433455091,"status":200,"request":{"mbean":"com.bytex.snamp.management:type=SnampCore","attribute":"configuration","type":"read"},"value":null}
```

`value` field in the resulting JSON holds SNAMP configuration in the form of the JSON tree. `null` means that SNAMP configuration is empty. JSON structure of the SNAMP configuration repeats its logical structure described above.

Following example shows setup of JMX-to-SNMP bridge:
```json
{
  "ResourceAdapters": {
    "test-snmp": {  
      "UserDefinedName": "test-snmp",
      "Adapter": {
        "Name": "snmp",   
        "Parameters": {
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
    "test-target": {
      "Connector": {
        "Parameters": {
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
        "Attributes": {
          "attribute1": {
            "Attribute": {
              "Name": "int32",  
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
              "ReadWriteTimeout": -1
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
              "ReadWriteTimeout": -1
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
              "ReadWriteTimeout": -1
            },
            "UserDefinedName": "attribute3"
          }
        },
        "Events": {
          "19.1": {
            "Event": {
              "Category": "jmx.attribute.change",
              "AdditionalProperties": {
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
            "UserDefinedName": "19.1"
          },
        },
        "ConnectionType": "jmx"   
      },
      "UserDefinedName": "test-target"
    }
  }
}
```
JSON format of SNAMP configuration is just a mapping between JMX data type and JSON. This mapping is implemented using [Jolokia](https://jolokia.org/) library. JMX-to-JSON mapping protocol is described [here](https://jolokia.org/reference/html/protocol.html).

If your SNAMP configuration is ready then save JSON into the file and use `curl` utility to setup a new configuration:
```bash
curl -u karaf:karaf -X POST -d @config.json http://localhost:3535/jolokia/
```

The content of `config.json` file (used in previous command):
```json
{
  "type": "write",
  "mbean": "com.bytex.snamp.management:type=SnampCore",
  "attribute": "configuration",
  "value": {"ResourceAdapters": { }, "ManagedResources": {} }
}
```

## Predefined configuration parameters
SNAMP Configuration Model provides set of optional configuration parameters with predefined semantics.

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
* Setting `keepAliveTime` to zero is not recommended due to performance penalties
* If `priority` is not specified then SNAMP uses default OS priority for threads in pool
* Value `priority` must lie in the interval _1..10_, where _1_ is the lowest priority.

Generally, SNAMP supports four major configuration of thread pool:
* Limited capacity of the queue, limited count of threads.
* Unlimited capacity of the queue, limited count of threads. In this case you should specify _2147483647_ value for `queueSize` parameter.
* Limited capacity of the queue, unlimited count of threads. In this case you should specify _2147483647_ value for `maxPoolSize`
* Unlimited capacity of the queue, unlimited count of threads. In this case you should specify _2147483647_ value for `maxPoolSize` and `queueSize`.

## Configuring OSGi
All configuration files are located in `<snamp>/etc` folder. These files supply a low-level access to Apache Karaf configuration.

### Logging
Apache Karaf and SNAMP logs are located in `<snamp>/data/log` folder. You can configure log rotation, severity level and other logging settings using the following configurations files in `<snamp>/etc` folder:
* `org.ops4j.pax.logging.cfg` - initial log configuration (appenders, levels, log message format)
* `java.util.logging.properties` - advanced configuration properties for standard Java logging. Changing this file is not recommended.
* `org.apache.karaf.log.cfg` - display configuration of the log records in the shell console

See [Karaf Log Configuration](http://karaf.apache.org/manual/latest/users-guide/log.html) for more details.

### HTTP
By default the HTTP Server listens on port `3535`. You can change the port by modifying a file `<snamp>/etc/org.ops4j.pax.web.cfg` with the following content:

```
org.osgi.service.http.port=8181
```

or by typing:
```
root@karaf> config:property-set -p org.ops4j.pax.web org.osgi.service.http.port 3535
```

The change will take effect immediately.
