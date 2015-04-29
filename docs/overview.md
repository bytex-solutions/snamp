SNAMP Overview
====
SNAMP is a middleware acting as a bridge between **managed resources** and **monitoring & management tools** used in your enterprise and hides management protocol details.

![Architecture Overview](/images/snamp.png)

**Resource Adapter** exposes management information to **monitoring & management tool** using a unified management protocol. Therefore, you can manage & monitor different **managed resources** with a single tool.

**Resource Connector** allows to connect **managed resource** to SNAMP. Each connected **managed resource** can be accessed via **Resource Adapter**.

Let's take a look at this example:

![Configuration Example](/images/example.png)

Example configuration consists of:
* Three **managed resources**
  * Java Application that can be managed via JMX
  * Linux Server that can be managed via SSH
  * Network Switch that can be managed via SNMP
* Two **monitoring & management tools**
  * Nagios with configured `curl`-based plugin which uses HTTP request to obtain management information from connected **managed resources**
  * Microsoft System Center Operations Manager which uses SNMP protocol to obtain management information from connected **managed resources**

In this configuration, Nagios can monitor Java Application, Linux Server, Network Switch via single HTTP protocol. Microsoft SCOM can monitor Java Application, Linux Server, Network Switch via single SNMP protocol.  

Full set of supported management protocols listed [here](/features.md).

## Managed Resource
**Managed resource** is a component of the enterprise IT infrastructure you want to manage. The possible (but not limited to) types of managed resources:
* Software component
  * Operating System
  * Application Server
  * Standalone Application
* Hardware component
  * Network Switch/Router
  * Sensor

Managed resource is accessible through SNAMP when and only when it is connected via **Managed Connector**.

## Resource Connector
**Resource Connector** (or **Managed Resource Connector**) is a software component used to connect **managed resource** to SNAMP environment using the specific management protocol. Information model of each connected resource consists of the following entities (called **management features**):
* Attributes
* Events (or notifications)
* Operations

Resource Connector may support all these features or some of them. Supported set of features dependends on the particularity of the used Resource Connector. For example, SNMP Resource Connector doesn't support operations because of SNMP protocol limitations.

**Resource Connector** has the following characteristics:
* _System name_ (or _type_) - a name of the installed resource connector. Typically, system name indicates the management protocol used by resource connector
* _Connection string_ - a string that specifies information about a managed resource and the means of connecting to it
* _Configuration_ - a set of configuration parameters controlling behavior of the resource connector

### Attribute
Management attribute (or attribute) describes the atomic metric, parameter or characteristics of the connected managed resource. Each connected managed resource may one or more attributes.

Attribute has the following characteristics:
* _Name_ - the name of the managed resource attribute. Each managed resource provides a strictly defined set of attributes
* _Accessibility_ is what you can do with attribute
  * _Read_ - attribute value is read-only
  * _Write_ - attribute value is write-only
  * _Read/Write_ - attribute value can be modified or obtained
* _Read/write timeout_ - a timeout used to read or write attribute value. By default, SNAMP uses infinite timeout
* _Type_ - the type of the attribute value. See [Management Information Model](/inform_model.md) for more information about supported attribute types
* _Configuration_ - a set of configuration parameters associated with the attribute

The attribute configuration and attribute name may be specified by SNAMP administrator. Other characteristics depends on the connected managed resource and cannot be changed by administrator.

Examples of attributes:
* Used disk space, in MB (read-only attribute)
* Number of cores (read-only attribute)
* Utilized volume of RAM, in MB (read-only attribute)
* Count of requests per hour (read-only attribute)
* Logging level (read/write attribute)
* Max size of log file (read/write attribute)

### Event
Event (or notification) is a maintenance message emitted by managed resource and carries information about some significant change in the managed resource.

Event has the following characteristics:
* _Category_ - the identifier of the notification/event in the managed resource. Each managed resource provides a strictly defined set of events
* _Configuration_ - a set of configuration parameters associated with the attribute
* _Severity_ - severity level of the emitted notifications. This is an optional characteristic

The event configuration, category and severity level (optionally) may be specified by SNAMP administrator. Other characteristics depends on the connected managed resource and cannot be changed by administrator.

See [Management Information Model](/inform_model.md) ror more information about severity level and notification content.

Examples of notifications:
* Fatal or critical error
* System Heartbeat (availability check)
* Log Event

### Operation
Operation is a maintenance action that may be applied to the managed resource.

Operation has the following characteristics:
* _Name_ - the name of the maintenance action. Each managed resource provides a strictly defined set of operations
* _Configuration_ - a set of configuration parameters associated with the operation
* _Signature_ - a set of formal parameters which should be populated with actual arguments before execution

The operation configuration may be specified by SNAMP administrator. Other characteristics depends on the connected managed resource and cannot be changed by administrator.

Examples of operations:
* Shutdown software component
* Restart software component
* Upload license file
* Invalidate cache

## Resource Adapter
**Resource Adapter** is a software component used to expose management information of connected managed resources to **monitoring & management tools** using the specific management protocol.


## Technology Stack
SNAMP constructed on top of [Apache Karaf](http://karaf.apache.org/) and requires Java Runtime Environment.

![Technology Stack](/images/tstack.png)

From the Deployment Viewpoint, SNAMP is a set of OSGi bundles packaged into KAR (Apache Karaf Feature archive) archives.

Additional topics:
* [Apache Karaf User's Guide](http://karaf.apache.org/manual/latest/users-guide/index.html)
* [OSGi Architecture](http://www.osgi.org/Technology/WhatIsOSGi)
