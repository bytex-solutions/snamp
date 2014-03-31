# SNAMP Platform Oveview
SNAMP is a middleware solution for monitoring and management of the software components in your enterprise IT-infrastructure.
![SNAMP Overview](images/overview.png)

SNAMP supplies monitoring and management information through different technologies and management information sources. For example, you can monitor [JMX](http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html) sources (such as Java(EE) applications) using SNMPv2 or SNMPv3 protocols, or even through HTTP using REST service.

## Features
At this time, SNAMP supports the following features:
* JMX to SNMP conversion
  * SNMPv2 and SNMPv3 support
  * Get and set JMX attributes via SNMP
  * Full support of Open MBean types: composite, tabular and array types can be exposed through SNMP tables;
  * Sending JMX notifications via SNMP traps
* JMX to REST conversion that allows to integrate JMX-compliant sources with your service desk operational monitoring web console
* Monitoring of IBM Message Queue and IBM WebSphere Integration Bus (formerly known as Message Broker) via SNMP or REST.

For more information about available SNAMP adapters and connectors, see [Features table](features-table.html).

### Extensibility
SNAMP platform has extensible architecture that allows you to write custom extensions for it:
* Custom monitoring channels and notifiers, such as E-Mail, Syslog, CMIP and etc.
* Custom connectors for your enterprise-specific monitoring solutions

SNAMP can be executed on every operating system(Windows, Linux) and hardware (x86, x64, ARM), that supports Java Virtual Machine.

Additional features:
* Cross-platform - execution is supported on every operating system and hardware, that supports Java Virtual Machine
* Multiplexing - encapsulation of connections to several targets into single SNAMP instance, that allows to organize centralized management node
* Easy to install - no installer, OS-specific configurations or any other setup procedures, just copy files and run
* Easy to configure - use Web console or YAML config file to configure management targets and SNAMP startup parameters
* Easy to embed - embed SNAMP platform into your Java monitoring and management solution

## Documentation
 See additional documentation:
* [User Guide](user-guide.html), if you are an administrator and want to configure SNAMP
* [Programming Guide](programming-guide.html), [Javadoc](javadoc/index.html) if you are a developer and want to write custom extensions for SNAMP
