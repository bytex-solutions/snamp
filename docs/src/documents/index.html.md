# SNAMP Platform Oveview
SNAMP is a middleware solution for monitoring and management of the software components in your enterprise IT-infrastructure.
![SNAMP Overview](/images/overview.png)

SNAMP supplies monitoring and management information through different technologies and management information sources. For example, you can monitor [JMX](http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html) sources (such as Java(EE) applications) using SNMPv2 or SNMPv3 protocols, or even through HTTP using REST service.

## Features
At this time, SNAMP supports bridge between the following technologies:
* Monitoring Java(EE) applications or any JMX-compliant services via SNMPv2, SNMPv3, HTTP(REST-JSON);
* Monitoring of IBM Message Queue state via SNMPv2, SNMPv3, HTTP(REST-JSON).
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
* User Guide, if you are an administrator and want to configure SNAMP
* [Programming Guide](/programming-guide.html), [Javadoc](/javadoc/index.html) if you are a developer and want to write custom extensions for SNAMP
