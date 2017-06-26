SNAMP 2.0.0
====
Major update of SNAMP platform and its components. A new functionality includes:

* Group of resources
* Supervisor used to control group of resources
* Elasticity Management for automatic scaling of clusters
* Health checks
* New gateways and connectors
* Distributed tracing
* Service discovery

## SNAMP Platform 2.0.0
SNAMP introduces a new shell commands and API for working with resource groups,
health checks, supervisors and service discovery. Also, SNAMP provides client-side
library called SNAMP Instrumentation Library for sending metrics, notifications
and health checks from client applications
similar to [Dropwizard](http://metrics.dropwizard.io/3.2.2/getting-started.html) and
[Apache HTrace](http://htrace.incubator.apache.org/).

### SNMP Resource Connector 2.0.0
Numerous bug fixes.

### JMX Resource Connector 2.0.0
Support of management operations using JMX operations.

### RShell Resource Connector 2.0.0
Support of management operations using execution of shell commands.

### Groovy Resource Connector 2.0.0
Support of management operations using Groovy methods.

### Composite Resource 2.0.0
Allows to compose two or more resources as a single managed resource.
Previous functionality from Resource Aggregator such as aggregation of metrics is included.

### HTTP Acceptor 2.0.0
A new type of resource connector introduced since version 2.0. It accepts
monitoring and tracing information using HTTP transport. This connector should
be used in conjunction with SNAMP Instrumentation Library.

### Zipkin Connector 2.0.0
A new type of resource connector introduced since version 2.0. It receives spans
from Zipkin-instrumented applications using HTTP or Kafka transports.

### Actuator Connector 2.0.0
A new type of resource connector introduced since version 2.0. It harvests metrics
and health checks from Spring-based applications using [Spring Actuator](http://www.baeldung.com/spring-boot-actuators).

### Modbus Connector 2.0.0
Without any changes in comparison with SNAMP 1.2.0.

### Stub Connector 2.0.0
Useful resource connector for tests and demonstration. It provides synthetic metrics.

### JMX Resource Adapter 2.0.0
Fix serialization issues associated with JMX.

### SSH Resource Adapter 2.0.0
Numerous bug fixes.

### HTTP Resource Adapter 2.0.0
WebSockets is the only transport for notifications. Long-polling and SSE was removed.

### XMPP Resource Adapter 2.0.0
Numerous bug fixes.

### Syslog Resource Adapter 2.0.0
Asynchronous message sending was replaced with synchronous.

### Nagios Resource Adapter 2.0.0
Numerous bug fixes.

### Nagios Service Check Acceptor (NSCA) Adapter 2.0.0
Numerous bug fixes.

### Nagios Remote Data Processor (NRDP) Adapter 2.0.0
Numerous bug fixes.

### SMTP Gateway 2.0.0
A new type of gateway introduced since version 2.0. This gateway publishes
information about notifications and health check to one or more e-mail boxes
using SMTP protocol.

### InfluxDB Gateway 2.0.0
A new type of gateway introduced since version 2.0. It can be used to store
all monitoring information and notifications into InfluxDB for further visualization
through [Grafana](https://grafana.com/).

### OpenStack Supervisor 2.0.0
Provides automatic scaling of cluster based on OpenStack using OpenStack Senlin.
Also, it provides health information about cluster.

### SNAMP Web Console
SNAMP Web Console provides web interface for configuration and displaying
monitoring and tracing information.

## Dependencies

* Upgrade to Apache Karaf 4.0.9.
* Upgrade to Apache Cellar 4.0.3
* Upgrade to Hazelcast 3.7.2

## Removed components
The following connectors are removed from 2.0.0 release:

* JMS Resource Connector
* IBM WebSphere MQ Resource Connector
* Resource Aggregator turned into Composite Resource
* Monitoring Data Acceptor replaced with HTTP Acceptor and Zipkin Connector
