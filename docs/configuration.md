SNAMP Configuration Guide
====
SNAMP provides JMX management interface which allows you to configure resource adapters, resource connectors, monitoring counters, staring/stopping SNAMP components and etc.


## Using SNAMP Management Console
SNAMP Management Console allows you to configure and maintain SNAMP via user-friendly Web interface in your browser.
> SNAMP Management Console available in paid subscription only

## Without SNAMP Management Console
There are the following ways to change the SNAMP configuration:
* Using JMX tool such as JConsole or VisualVM
* Using JMX command-line tool such as [jmxterm](http://wiki.cyclopsgroup.org/jmxterm/)
* Using `curl` utility

## Predefined configuration parameters
SNAMP Configuration Model provides a set of optional configuration parameters with predefined semantics.

Parameter | Applied to | Meaning
---- | ---- | ----
`severity` | Event configuration | Overrides severity of notification supplied by managed resource

The possible values of `severity` parameter:

Value | Description
---- | ----
panic | A `panic` condition usually affecting multiple apps/servers/sites. At this level it would usually notify all tech staff on call.


## Examples
* [Monitoring JMX resources over SNMP](/jmx-over-snmp.md)
* [Monitoring SNMP resources over HTTP](/snmp-over-http.md)
* [Monitoring JMX resources over XMPP](/jmx-over-xmpp.md)
