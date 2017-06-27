SNAMP Configuration Guide
====
SNAMP can be configured using different ways:

* via Web Browser
* via JMX
* via HTTP
* via command-line interface

System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder or Karaf shell console commands.

See [Configuring Apache Karaf](http://karaf.apache.org/manual/latest/#_configuration) for more information about Apache Karaf configuration model.

See [SNAMP Management Interface](mgmt.md) for more information about SNAMP management via JMX and REST API.

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

* `Name` - name of the attribute declared by managed resource. Note that this name depends on the management information provided by managed resource. This parameter is required. Also, Resource Connector uses this name when exposing attributes to Resource Adapters. In some cases name of the attribute may differs from the name declared in SNAMP configuration. In this case use `name` configuration parameter in the set of additional configuration parameters.
  * `Read/write timeout` - timeout (in millis) used when accessing attribute value. This is the optional parameter.
  * Additional configuration parameters in the form of key/value pairs

_Events_ section:

* `Category` - event category declared by managed resource. Note that the category depends on the management information provided by managed resource. This parameter is required. Also, Resource Connector uses this name as notification type when exposing notifications to Resource Adapters. In some cases category of the event may differs from the category declared in SNAMP configuration. In this case use `name` configuration parameter in the set of additional configuration parameters.
  * Additional configuration parameters in the form of the key/value pairs

_Operations_ section may contain zero:

* `Name` - name of the operation declared by managed resource. Note that operation name depends on the management information provided by managed resource. This parameter is required. Also, Resource Connector uses this name when exposing operations to Resource Adapters. In some cases name of the operation may differs from the name declared in SNAMP configuration. In this case use `name` configuration parameter in the set of additional configuration parameters.
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
      * `Name`: freeMemory
        * `Read/write timeout`: 2000
        * `objectName`: com.sun.glassfish.management:type=Memory
        * `xmppFormat`: human-readable
      * `Name`: freeMemoryRaw
        * `name`: freeMemory
        * `objectName`: com.sun.glassfish.management:type=Memory
        * `xmppFormat`: binary
      * `Name`: available
        * `name`: isActive
        * `objectName`: com.sun.glassfish.management:type=Common
        * `xmppFormat`: human-readable
    * _Events_
      * `Category`: com.sun.glassfish.ejb.unhandledException
        * `objectName`: com.sun.glassfish.management:type=Common
        * `fullStackTrace`: true

In this example single managed resource named _partner-gateway_ connected via JMX protocol. The managed resource contains two MBeans (`Memory` and `Common`) with attributes and JMX notifications. This managed resource can be managed via two protocols: `http` and `xmpp` (Jabber). It is possible because configuration model contains three configured resource adapters. Two adapters have the same `http` system name. But these two adapters configured with different set of parameters. Each `http` adapter exposes REST API on its own URL context (`http://localhost/snamp/gateway/http/gateway1` and `http://localhost/snamp/gateway/http/gateway2`). The third resource adapter allows to managed resources via Jabber client (such as Miranda IM).

Managed Resource `partner-gateway` is connected using `jmx` Resource Connector. This example demonstrates how to expose two JMX attributes with the same in MBean (`freeMemoryMB`) with different settings and instance names. Both attributes are visible from `http` resource adapter using the following URLs:

* `http://localhost/snamp/gateway/http/adapter1/attributes/freeMemoryRaw`
* `http://localhost/snamp/gateway/http/adapter2/attributes/freeMemoryRaw`
* `http://localhost/snamp/gateway/http/adapter1/attributes/freeMemory`
* `http://localhost/snamp/gateway/http/adapter2/attributes/freeMemory`

XMPP Adapter provides value of the `freeMemoryRaw` and `freeMemory` in two different formats: human-readable and binary. This behavior is defined using `xmppFormat` configuration property associated with each attribute.

As you can see, configuration parameters depends on the Resource Adapter and Resource Connector. See [Configuring Resource Adapters](gateways/introduction.md) and [Configuring Resource Connectors](connectors/introduction.md) for more details about SNAMP configuration.

## Using SNAMP Management Console
SNAMP Management Console allows you to configure and maintain SNAMP via user-friendly Web interface in your browser.
> SNAMP Management Console available in paid subscription only

The console supports the following configuration features:

* [Highlight available Resource Adapters, Resource Connectors, configuration properties, attributes, events and operations](webconsole/configuration.md)
* [Start, stop and restart resource adapters and connectors](webconsole/managing.md)
* [Detailed overview of platform and modules state](webconsole/general.md)

SNAMP Management Console build on top of powerful [hawt.io](http://hawt.io) web console.

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
              "AdditionalProperties": {
                "name": {
                    "Key": "name",
                    "Value": "int32"
                },
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
            "Name": "attribute1"
          },
          "attribute2": {
            "Attribute": {
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
            "Name": "dictionary"
          },
          "attribute3": {
            "Attribute": {
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
            "Name": "bigint"
          }
        },
        "Events": {
          "19.1": {
            "Event": {
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
            "Category": "jmx.attribute.change"
          }
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
