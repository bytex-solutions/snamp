SNAMP Resource Connectors
====
**Resource Connector** (or **Managed Resource Connector**) is a software component used to connect **managed resource** to SNAMP environment using specific management protocol.

Feature list of **Resource Connector** consists of the following items:

* Supported Protocols - list of management protocols used for connecting **managed resource** to SNAMP
* Attributes - is **Resource Connector** provide attributes (metrics) of **managed resource**?
* Notifications - is **Resource Connector** provide notifications if **managed resource**?
* Operations - is it possible to invoke maintenance action of **managed resource**?
* Health check - is **Resource Connector** provide health information of **managed resource**?
* Smart mode - resource connector can expose attributes, events or operations without manual configuration (see related section)

At this page you can find detailed description for each resource connector supported by SNAMP:

Display name | Category | Type | Supported Protocols | Attributes | Notifications | Operations | Health check | Smart mode
---- | ---- | ---- | ---- | ---- | ---- | ----
[JMX Resource Connector](jmx-connector.md) | Active | jmx | JMX | Yes | Yes | Yes | Yes | Yes
[RShell Resource Connector](rshell-connector.md) | Active | rshell | SSH, REXEC, RSHELL, Local Process Execution | Yes | No | No | No | No
[SNMP Resource Connector](snmp-connector.md)| Active | snmp | SNMPv2, SNMPv3 | Yes | Yes | No | No | Limited (only for attributes)
[Composite Resource](aggregator-connector.md) | Active/Passive | aggregator | _Not Applicable_ | Yes | Yes | No | Yes | No
[Groovy Resource Connector](groovy-connector.md) | _Not applicable_ | groovy | _Any_ | Yes | Yes | Yes | Yes | Yes
[Modbus Resource Connector](modbus-connector.md) | Active | modbus | Modbus/TCP, Modbus/UDP, Modbus RTU-IP | Yes | No | No | No | No
[HTTP Acceptor](http-acceptor.md) | Passive | http | HTTP | Yes | Yes | Yes (limited set) | Yes | No
[Zipkin Connector](zipkin-connector.md) | Passive | kafka | Kafka, HTTP | Yes | Yes | Yes (limited set) | No | No
[Actuator Connector](actuator-connector.md) | Active | actuator | HTTP | Yes | No | No | Yes | Yes
[Stub Connector](stub-connector.md) | Active | stub | _Not applicable_ | Yes | No | No | Yes | Yes

Read [this page](webconsole/config-connectors.md) about configuration of Resource connectors using SNAMP Web Console.

## Smart mode
Some resource connectors can expose attributes, events or operations without its manual configuration. In this case the connector automatically exposes all attributes, events or operations. So, these resource features will be accessible at runtime event if configuration section with attributes and events is empty. To enable smart mode of the connector you should specify `smartMode = true` in the configuration parameters of the resource.

## Predefined configuration parameters
The following table describes optional configuration parameters that are applicable to all resource connectors:

Parameter | Applied to | Meaning
---- | ---- | ----
name | Event, Attribute, Operation | Resource-specific name of the attribute, event or operation
group | Resource Connector | Name of the resource group
severity | Event | Overrides severity of notification supplied by managed resource
threadPool | Managed Resource | Name of thread pool used by connector. If not specified then default thread pool is shared between all components.
units | Attribute | Unit of measurement (UOM) of the attribute value. For example: `ms`, `m`, `kg`, `MB`
defaultValue | Attribute | The default value of the attribute if the actual value is not available
minValue | Attribute | The minimum (exclusive) permitted value for the attribute
maxValue | Attribute configuration | The maximum (exclusive) permitted value for the attribute
smartMode | Resource configuration | Enable or disable smart mode of the connector. The possible values are `true` or `false`
description | Event, Attribute, Operation | Human-readable description of the attribute, event or operation

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
