SNAMP Resource Connectors
====
**Resource Connector** (or **Managed Resource Connector**) is a software component used to connect **managed resource** to SNAMP environment using specific management protocol.

Feature list of **Resource Connector** consists of the following items:

* Supported Protocols - list of management protocols used for connecting **managed resource** to SNAMP
* Attributes - is **Resource Connector** provide attributes (metrics) of **managed resource**?
* Notifications - is **Resource Connector** provide notifications if **managed resource**?
* Operations - is it possible to invoke maintenance action of **managed resource**?
* Health check - is **Resource Connector** provide health information of **managed resource**?
* Smart mode - resource connector can expose attributes, events or operations without manual configuration

At this page you can find detailed description for each resource connector supported by SNAMP:

Display name | Category | Type | Supported Protocols | Attributes | Notifications | Operations | Health check | Smart mode
---- | ---- | ---- | ---- | ---- | ---- | ----
[JMX Resource Connector](jmx-connector.md) | Active | jmx | JMX | Yes | Yes | Yes | Yes | Yes
[RShell Resource Connector](rshell-connector.md) | Active | rshell | SSH, REXEC, RSHELL, Local Process Execution | Yes | No | No | No | No
[SNMP Resource Connector](snmp-connector.md)| Active | snmp | SNMPv2, SNMPv3 | Yes | Yes | No | No | Limited (only for attributes)
[Composite Resource](aggregator-connector.md) | Active/Passive | aggregator | _Not Applicable_ | Yes | Yes | No | Yes | No
[Groovy Resource Connector](groovy-connector.md) | _Not applicable_ | groovy | _Any_ | Yes | Yes | No | No | No
[Modbus Resource Connector](modbus-connector.md) | Active | modbus | Modbus/TCP, Modbus/UDP, Modbus RTU-IP | Yes | No | No | No | No
[HTTP Acceptor](http-acceptor.md) | Passive | http | HTTP | Yes | Yes | No | No | No
[Zipkin Connector](zipkin-connector.md) | Passive | kafka | Kafka, HTTP | Yes | Yes | No | No | No
[Actuator Connector](actuator-connector.md) | Active | actuator | HTTP | Yes | No | No | Yes | Yes
