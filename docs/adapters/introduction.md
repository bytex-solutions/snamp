SNAMP Resource Adapters
====
**Managed Resource Adapter** (or **Resource Adapter**) is a software component used to expose monitoring and management functionality to **Monitoring & Management Tools** used in your enterprise (such as Nagios, Zabbix or Microsoft System Center Operations Manager). Resource Adapter exposes all information provided by connected managed resources (via Resource Connectors) to the external tools using specified protocol or technology.

Feature list of **Resource Adapter** contains following items:

* Management Protocols - list of management protocols used by **monitoring & management tool** to manage and monitor the connected **managed resource**
* Attributes - is **Resource Adapter** provide access to the **managed resource** attributes?
* Notifications - is **Resource Adapter** delivers **managed resource** notifications to the **monitoring & management tool**?
* Operations - is it possible to invoke maintenace action remotely using **Resource Adapter**?

> If **Resource Connector** doesn't support one of management feature then this feature will not be exposed by **Resource Adapter**.

Detailed description for each resource adapter supported by SNAMP:

Display name | System name | Management Protocols | Data Exchange Format | Attributes | Notifications | Operations
---- | ---- | ---- | ---- | ---- | ----
[SNMP Resource Adapter](snmp-adapter.md) | snmp | SNMPv2, SNMPv3 | BER/ASN.1 | Yes | Yes | No
[JMX Resource Adapter](jmx-adapter.md) | jmx | JMX | Java Binary Serialization | Yes | Yes | No
[SSH Resource Adapter](ssh-adapter.md) | ssh | SSH | Character Stream | Yes | Yes | No
[HTTP Resource Adapter](groovy-adapter.md) | http | HTTP | JSON | Yes | Yes | No
[XMPP Resource Adapter](xmpp-adapter.md) | xmpp | XMPP (Jabber) | XML | Yes | Yes | No
[Syslog Resource Adapter](syslog-adapter.md) | syslog | RFC-3164, RFC-5424 | Binary | Yes | Yes | No
[Nagios Resource Adapter](nagios-adapter.md) | nagios | HTTP | [Nagios Plugin Format](https://nagios-plugins.org/doc/guidelines.html#PLUGOUTPUT) | Yes | Yes | No
[NSCA Resource Adapter](nsca-adapter.md) | nsca | TCP | Binary (Nagios NSCA format) | Yes | Yes | No
[NRDP Resource Adapter](nrdp-adapter.md) | nrdp | HTTP | XML (Nagios NRDP format) | Yes | Yes | No
[Groovy Resource Adapter](groovy-adapter.md) | groovy | _Any_ | _Any_ | Yes | Yes | No
