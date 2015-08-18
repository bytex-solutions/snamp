SNAMP Resource Connectors
====
**Resource Connector** (or **Managed Resource Connector**) is a software component used to connect **managed resource** to SNAMP environment using specific management protocol.

Feature list of **Resource Connector** consists of the following items:
* Supported Protocols - list of management protocols used for connecting **managed resource** to SNAMP
* Attributes - how **Resource Connector** exposes **managed resource** attributes to **Resource Adapter**?
* Notifications - how **Resource Connector** exposes **managed resource** notifications to the **Resource Adapter**
* Operations - is it possible to invoke maintenace action of **managed resource**
* Smart mode - resource connector can expose attributes, events or operations without manual configuration

At this page you can find detailed description for each resource connector supported by SNAMP:

Display name | System name | Supported Protocols | Attributes | Notifications | Operations | Smart mode
---- | ---- | ---- | ---- | ----
[JMX Resource Connector](jmx-connector.md) | jmx | JMX | Yes | Yes | Yes | Yes
[RShell Resource Connector](rshell-connector.md) | rshell | SSH, REXEC, RSHELL, Local Process Execution | Yes | No | No | No
[SNMP Resource Connector](snmp-connector.md) | snmp | SNMPv2, SNMPv3 | Yes | Yes | No | Limited (only for attributes)
[IBM WebSphere MQ Connector](wmq-connector.md) | ibm-wmq | Proprietary | Yes | No | No | Yes
[Resource Aggregator](aggregator-connector.md) | aggregator | _Not Applicable_ | Yes | Yes | No | No
[Groovy Resource Connector](groovy-connector.md) | groovy | _Any_ | Yes | Yes | No | No
[Modbus Resource Connector](modbus-connector.md) | modbus | Modbus/TCP, Modbus/UDP, Modbus RTU-IP | Yes | No | No | No
