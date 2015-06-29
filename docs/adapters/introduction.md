SNAMP Resource Adapters
====
**Managed Resource Adapter** (or **Resource Adapter**) is a software component used to expose monitoring and management functionality to **Monitoring & Management Tools** used in your enterprise (such as Nagios, Zabbix or Microsoft System Center Operations Manager). Resource Adapter exposes all information provided by connected managed resources (via Resource Connectors) to the external tools using specified protocol or technology.

At this page you can find detailed description for each resource adapter supported by SNAMP:
* [Groovy Resource Adapter](groovy-adapter.md) - how to write a custom bridge using Groovy between your Monitoring & Management Tool and management data provided by SNAMP
* [HTTP Resource Adapter](groovy-adapter.md) - monitor and manage connected resources via HTTP/JSON
* [JMX Resource Adapter](jmx-adapter.md) - monitoring and manage connected resources via JMX
* [Nagios Resource Adapter](nagios-adapter.md) - using Nagios active check for connected resources
* [NRDP Resource Adapter](nrdp-adapter.md) - using Nagios Remote Data Process for passive checks of connected resources
* [NSCA Resource Adapter](nsca-adapter.md) - using Nagios Service Check Acceptor for passive checks of connected resources
* [XMPP Resource Adapter](xmpp-adapter.md) - monitoring and management of connected resources using Jabber client or any other XMPP-compliant software
