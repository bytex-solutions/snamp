JMX Resource Connector
====
JMX Resource Connector allows you to manage and monitor Java components via JMX protocol, such as
* Java Application Servers
* Java components
* Java standalone application

Supported features:

Feature | Comments
---- | ----
Attributes | Transforming JMX attributes into SNAMP attributes
Events | Routing JMX notifications to resource adapters

## Connection String
JMX Resource Connector uses JMX service URL for connecting to JMX Mbean Server.

JMX Service URL is an Abstract Service URL for SLP, as defined in _RFC 2609_ and amended by _RFC 3111_. It must look like this:
```
service:jmx:protocol:sap
```

Here, `protocol` is the transport protocol to be used to connect to the connector server. It is a string of one or more ASCII characters, each of which is a letter, a digit, or one of the characters + or -. The first character must be a letter. Uppercase letters are converted into lowercase ones.

`sap` is the address at which the connector server is found. This address uses a subset of the syntax defined by _RFC 2609_ for IP-based protocols. It is a subset because the `user@host` syntax is not supported.

The other syntaxes defined by _RFC 2609_ are not currently supported.

The supported syntax is:
```
//[host[:port]][url-path]
```

Square brackets [] indicate optional parts of the address. Not all protocols will recognize all optional parts.

The host is a host name, an IPv4 numeric host address, or an IPv6 numeric address enclosed in square brackets.

The port is a decimal port number. 0 means a default or anonymous port, depending on the protocol.

The host and port can be omitted. The port cannot be supplied without a host.

The url-path, if any, begins with a slash (/) or a semicolon (;) and continues to the end of the address. It can contain attributes using the semicolon syntax specified in RFC 2609. Those attributes are not parsed by this class and incorrect attribute syntax is not detected.

Although it is legal according to _RFC 2609_ to have a url-path that begins with a semicolon, not all implementations of SLP allow it, so it is recommended to avoid that syntax.

Case is not significant in the initial `service:jmx:protocol` string or in the host part of the address. Depending on the protocol, case can be significant in the url-path.

Examples:
* `service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/karaf-root` for local connection
* `service:jmx:rmi://192.168.0.1:1100/jndi/rmi://192.168.0.1:1099/jmxrmi` for remote connection

## Configuration Parameters
JMX Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
login | String | No | JMX login used for authentication on remote JMX server | `user`
password | String | No | JMX password used for authentication on remote JMX server | `password`
connectionCheckPeriod | Number | No | Time period used by JMX connection watchdog, in milliseconds. The default value is `3000` | `4000`

### JMX connection watchdog
JMX connection watchdog is a background process controlling consistency of JMX connection. If JMX connection was lost then watchdog restores it and all subscriptions on JMX notifications.

## Configuring attributes
Each attribute configured in JMX Resource Connector has the following configuration schema:
* `Name` - the name of attribute in the remote MBean
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
objectName | String | Yes | The name of the MBean on remote JMX server. This MBean provides access to the JMX attribute so the `Name` of the SNAMP Attribute must be configured properly | `java.lang:type=OperatingSystem`
useRegexp | Boolean (`true`/`false`) | No | Indicating that the `objectName` parameter defines regular expression used to find the appropriate MBean. The default value is `false`

## Configuring notifications
Each event configured in JMX Resource Connector has the following configuration schema:
* `Category` - notification type of the target JMX notification. For example, `jmx.attribute.change`
* 
