# SNAMP 1.2.0
Upgrade of all SNAMP components and migration to Java 8:

Change list:

* SNAMP now requires at least Java 8. Java 7 is not supported anymore.
* Centralized configuration of parallelism using `snamp:thread-pool-*` shell commands.
* Configuration script written on JavaScript can be executed using `snamp:script` command.
* Significant performance improvements.

Updated components:

* Apache Karaf
* Apache Decanter
* Google GSON
* Groovy
* Bouncy Castle
* SNMP4J
* ActiveMQ Runtime

All SNAMP components are affected so you need to reconfigure SNAMP. It is not possible to restore configuration from backup.

## SNAMP Platform 1.2.0
Centralized configuration of parallelism provided by the following commands:

* Thread pool configuration commands using `snamp:thread-pool-add`, `snamp:thread-pool-remove` and `snamp:thread-pool-list` commands
* `threadPool` configuration property represents a name of thread pool configured with `snamp:thread-pool-add` command and used by resource connector or adapter

The following configuration parameters are no longer supported: `minPoolSize`, `maxPoolSize`, `queueSize`, `keepAliveTime` and `priority`.

## SNMP Resource Adapter 1.2.0
Remove support for nonstandard AES 192 and 256 using 3DES key extend algorithm.

Support for new authentication protocols: `hmac128-sha224`, `hmac192-sha256`, `hmac256-sha384`, `hmac384-sha512`.

## SNMP Resource Connector 1.2.0
Improved compliance with RFC 3412 Â§6.2.

Support for new authentication protocols: `hmac128-sha224`, `hmac192-sha256`, `hmac256-sha384`, `hmac384-sha512`.

## RShell Resource Connector 1.2.0
Nashorn is a primary JavaScript engine.
