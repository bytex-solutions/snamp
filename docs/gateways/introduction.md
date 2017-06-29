SNAMP Gateways
====
**Gateway** is a software component used to expose monitoring and management functionality to **Monitoring & Management Tools** used in your enterprise (such as Nagios, Zabbix, e-mail or even Jabber). Gateway exposes all information provided by connected managed resources (via Resource Connectors) to the external tools using specified protocol or technology.

Feature list of **Gateway** contains following items:

* Publication Protocols - list of management protocols used by **monitoring & management tool** to manage and monitor the connected **managed resource**
* Attributes - is **Gateway** provide access to the **managed resource** attributes?
* Notifications - is **Gateway** delivers **managed resource** notifications to the **monitoring & management tool**?
* Operations - is it possible to invoke maintenance action remotely using **Gateway**?
* Health status - is it possible to expose health information to external tool

> If **Resource Connector** doesn't support one of management feature then this feature will not be exposed by **Gateway**.

Detailed description for each Gateway supported by SNAMP:

Display name | Type | Publication Protocols | Data Exchange Format | Attributes | Notifications | Operations | Health status
---- | ---- | ---- | ---- | ---- | ---- | ----
[SNMP Gateway](snmp-gateway.md) | snmp | SNMPv2, SNMPv3 | BER/ASN.1 | Yes | Yes | No | No
[JMX Gateway](jmx-gateway.md) | jmx | JMX | Java Binary Serialization | Yes | Yes | Yes | No
[SSH Gateway](ssh-gateway.md) | ssh | SSH | Character Stream | Yes | Yes | No | Yes
[HTTP Gateway](groovy-gateway.md) | http | HTTP, WebSocket | JSON | Yes | Yes | No | No
[XMPP Gateway](xmpp-gateway.md) | xmpp | XMPP (Jabber) | XML | Yes | Yes | No | No
[Syslog Gateway](syslog-gateway.md) | syslog | RFC-3164, RFC-5424 | Binary | Yes | Yes | No | No
[Nagios Gateway](nagios-gateway.md) | nagios | HTTP | [Nagios Plugin Format](https://nagios-plugins.org/doc/guidelines.html#PLUGOUTPUT) | Yes | Yes | No | No
[NSCA Gateway](nsca-gateway.md) | nsca | TCP | Binary (Nagios NSCA format) | Yes | Yes | No | No
[NRDP Gateway](nrdp-gateway.md) | nrdp | HTTP | XML (Nagios NRDP format) | Yes | Yes | No | No
[Groovy Gateway](groovy-gateway.md) | groovy | _Any_ | _Any_ | Yes | Yes | No | Yes
[SMTP Gateway](smtp-gateway.md) | smtp | SMTP, SMTPS | Plain text, JSON | No | Yes | No | Yes

## Predefined configuration parameters
The following table describes optional configuration parameters that are applicable to all gateways:

Parameter | Applied to | Meaning
---- | ---- | ----
threadPool | Gateway | Name of thread pool used by gateway. If not specified then default thread pool is shared between all components.
