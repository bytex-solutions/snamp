SNAMP Feature List
====
This page describes a features provided by different **Resource Adapters** and **Resource Connectors**.

## Resource Adapters
Feature list of **Resource Adapter** consists of the following items:
* Management Protocols - a list of management protocols used by **monitoring & management tool** to manage and monitor the connected **managed resource**
* Attributes - is **Resource Adapter** provide access to the **managed resource** attributes?
* Notifications - is **Resource Adapter** delivers **managed resource** notifications to the **monitoring & management tool**?
* Operations - is it possible to invoke maintenace action remotely using **Resource Adapter**?

> If **Resource Connector** doesn't support one of management feature then this feature will not be exposed by **Resource Adapter**.

Name | Management Protocols | Data Exchange Format | Attributes | Notifications | Operations
---- | ---- | ---- | ---- | ---- | ----
snmp | SNMPv2, SNMPv3 | BER/ASN.1 | Yes | Yes | No
jmx | JMX | Java Binary Serialization | Yes | Yes | No
ssh | SSH | Character Stream | Yes | Yes | No
http | HTTP | JSON | Yes | Yes | No
xmpp | XMPP (Jabber) | XML | Yes | Yes | No
syslog | RFC-3164, RFC-5424 | Binary | Yes | Yes | No
nagios | HTTP | [Nagios Plugin Format](https://nagios-plugins.org/doc/guidelines.html#PLUGOUTPUT) | Yes | Yes | No
nsca | TCP | Binary (Nagios NSCA format) | Yes | Yes | No
nrdp | HTTP | XML (Nagios NRDP format) | Yes | Yes | No

## Resource Connectors
Feature list of **Resource Connector** consists of the following items:
* Supported Protocols - a list of management protocols used for connecting **managed resource** to SNAMP
* Attributes - is **Resource Connector** expose **managed resource** attributes to **Resource Adapter**?
* Notifications - is **Resource Connector** expose **managed resource** notifications to the **Resource Adapter**
* Operations - is it possible to invoke maintenace action of **managed resource**

Name | Supported Protocols | Attributes | Notifications | Operations
---- | ---- | ---- | ---- | ----
jmx | JMX | Yes | Yes | No
rshell | SSH, REXEC, RSHELL, Local Process Execution | Yes | No | No
snmp | SNMPv2, SNMPv3 | Yes | Yes | No
ibm-wmq | Proprietary | Yes | No | No
ibm-wmb | Proprietary | Yes | Yes | No
aggregator | _Not Applicable_ | Yes | Yes | No
