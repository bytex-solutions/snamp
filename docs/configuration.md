SNAMP Configuration Guide
====
SNAMP can be configured using different ways:
* via Web Browser
* via JMX
* via HTTP
* via command-line tools

System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder.

See [Configuring Apache Karaf](http://karaf.apache.org/manual/latest/users-guide/configuration.html) for more information about Apache Karaf configuration model.

See [SNAMP Management Interface](/mgmt.md) for more information about SNAMP management via JMX.

## Configuration Model
SNAMP configuration describes a set of resource adapters, resource connectors and its attributes, notifications and operations.

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

## Examples
* [Monitoring JMX resources over SNMP](/jmx-over-snmp.md)
* [Monitoring SNMP resources over HTTP](/snmp-over-http.md)
* [Monitoring JMX resources over XMPP](/jmx-over-xmpp.md)
