SNAMP Installation
====
This page provides installation instructions and system requirements

## System requirements
Environment:

* Java SE Runtime Environment 7/8. Recommended JVMs:
    * OpenJDK JRE
    * Oracle JRE
* The `JAVA_HOME` environment variable must be set to the directory where the Java runtime is installed

Supported operating systems:

* Windows
    * Windows Vista SP2
    * Windows 7
    * Windows 8
    * Windows 10
    * Windows Server 2008 R2 SP1 (64-bit)
    * Windows Server 2012 (64-bit)
* Linux
    * Red Hat Enterprise Linux 5.5+, 6.x (32-bit), 6.x (64-bit)
    * Ubuntu Linux 10.04 and above (only LTS versions)
    * Suse Linux Enterprise Server 10 SP2, 11.x
    * Debian Linux 8.x

Hardware:

* Processor architectures:
    * x86
    * x64
    * ARMv6/ARMv7 (in paid subscription only)
* 2 Cores (minimum)
* 250 MB of free disk space
> Disk space requirement ignores growing of log files

* 512 MB RAM (minimum), 2 GB RAM (recommended)

Running SNAMP on ARM-based hardware (such as RaspberryPi) is possible as well - contact us for more information.

## Installation
1. Download the latest SNAMP distribution package. You may choose package format: `zip` or `tar.gz`
1. Extract `snamp-X.Y.Z.tar.gz` or `snamp-X.Y.Z.zip` into your installation folder
> There is no limitations for installation destination

SNAMP may be launched in the following modes:

* `regular` mode starts SNAMP in foreground, including the shell console
* `server` mode starts SNAMP in foreground, without the shell console
* `background` mode starts Apache Karaf in background.

See [start, stop, restart Apache Karaf](https://karaf.apache.org/manual/latest/users-guide/start-stop.html) for more information about Apache Karaf lifecycle management.

### Regular mode
The regular mode uses `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows). That is default start process mode.

In this mode SNAMP starts as a foreground process, and displays the shell console.

On Unix:
```bash
cd <snamp>
sh bin/karaf
```

On Windows:
```
cd <snamp>
bin\karaf.bat
```

Note that closing the console or shell window will cause SNAMP to terminate.

### Server mode
The server mode starts SNAMP as a foreground process, but doesn't start the shell console.

For launching this mode, use the `server` argument to the `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows).

On Unix:
```bash
cd <snamp>
sh bin/karaf server
```

On Windows:
```
cd <snamp>
bin\karaf.bat server
```

Note that closing the console or shell window will cause Apache Karaf to terminate.

You can connect to the shell console using SSH:

* On Unix: `<snamp>/bin/client`
* On Windows: `<snamp>\bin\client.bat`

By default, client tries to connect on localhost, on port 8101. You can use `--help` to get details about the options

### Background mode
The background mode starts SNAMP as a background process.

To start in background mode, you have to use `<snamp>/bin/start` Unix script (`<snamp>\bin\start.bat` on Windows).

You can connect to the shell console using SSH.

### Verifying installation
Start SNAMP and open the shell console, then print `bundle:list` and press ENTER. You will see the following output:
```
START LEVEL 100 , List Threshold: 50
 ID | State  | Lvl | Version                    | Name
-------------------------------------------------------------------------------------------------------------------
 52 | Active |  80 | 19.0.0                     | Guava: Google Core Libraries for Java
 53 | Active |  80 | 1.4.2                      | ConcurrentLinkedHashMap
 54 | Active |  80 | 3.7.2                      | hazelcast
 55 | Active |  80 | 2.2.21                     | OrientDB Client
 56 | Active |  80 | 2.2.21                     | OrientDB Core
 57 | Active |  80 | 2.2.21                     | OrientDB Distributed Server
 58 | Active |  80 | 2.2.21                     | OrientDB Server
 59 | Active |  80 | 2.2.21                     | OrientDB Tools
 60 | Active |  80 | 1.19.3                     | jersey-client
 61 | Active |  80 | 1.19.3                     | jersey-core
 62 | Active |  80 | 1.19.3                     | jersey-server
 63 | Active |  80 | 1.19.3                     | jersey-json
 64 | Active |  80 | 1.19.3                     | jersey-servlet
 65 | Active |  80 | 1.9.13                     | Jackson JSON processor
 66 | Active |  80 | 1.9.13                     | JAX-RS provider for JSON content type, using Jackson data binding
 67 | Active |  80 | 1.9.13                     | Data mapper for Jackson JSON processor
 68 | Active |  80 | 1.9.13                     | XML Compatibility extensions for Jackson data binding
 70 | Active |  80 | 1.4.4                      | JavaMail API (compat)
 73 | Active |  80 | 1.1.1                      | jsr311-api
 80 | Active |  80 | 1.3.8                      | jettison
107 | Active |  80 | 3.10.6.Final               | Netty
108 | Active |  80 | 3.0.1                      | FindBugs-jsr305
115 | Active |  80 | 2.0.0                      | Discovery API over HTTP
116 | Active |  80 | 2.0.0                      | SNAMP Framework
117 | Active |  80 | 2.0.0                      | SNAMP Internal Services
118 | Active |  80 | 2.0.0                      | JSON Support Library
119 | Active |  80 | 2.0.0                      | SNAMP Security Layer
120 | Active |  80 | 2.4.11                     | Groovy Runtime
121 | Active |  80 | 2.4.11                     | groovy-json
122 | Active |  80 | 2.4.11                     | groovy-xml
123 | Active |  80 | 2.4.0.final_20141213170938 | Ivy
124 | Active |  80 | 2.0.0                      | Default Supervisor
125 | Active |  80 | 2.0.0                      | OSGi bridge for groovy-xml
126 | Active |  80 | 2.0.0                      | SNAMP Scripting Framework
127 | Active |  80 | 3.0.3                      | OpenStack4j
128 | Active |  80 | 2.0.0                      | OpenStack Supervisor
129 | Active |  80 | 4.0.8                      | StringTemplate
130 | Active |  80 | 2.0.0                      | Sprint Actuator Connector
131 | Active |  80 | 2.0.0                      | Stub Connector
132 | Active |  80 | 1.0.0                      | SNAMP Instrumentation Library
133 | Active |  80 | 2.0.0                      | Message Processing Framework
134 | Active |  80 | 2.0.0                      | Composite Resource Connector
135 | Active |  80 | 2.0.0                      | Groovy Script Resource
136 | Active |  80 | 2.0.0                      | HTTP Acceptor
137 | Active |  80 | 2.0.0                      | JMX Connector
138 | Active |  80 | 2.0.0                      | Modbus Connector
139 | Active |  80 | 1.54                       | bcpg
140 | Active |  80 | 1.54                       | bcpkix
141 | Active |  80 | 1.54                       | bcprov
142 | Active |  80 | 0.1.0                      | ed25519-java
143 | Active |  80 | 2.0.0                      | RShell Connector
144 | Active |  80 | 0.18.0                     | SSHJ
145 | Active |  80 | 2.0.0                      | SNMP Connector
146 | Active |  80 | 2.5.3                      | SNMP4J
147 | Active |  80 | 1.16.2                     | Zipkin
148 | Active |  80 | 2.0.0                      | Zipkin Connector
149 | Active |  80 | 2.0.0                      | DevOps Tools
150 | Active |  80 | 2.0.0                      | SNAMP Manager
151 | Active |  80 | 2.0.0                      | E2E Analyzer
156 | Active |  80 | 2.0.0                      | Web console
157 | Active |  80 | 2.0.0                      | Groovy Gateway
158 | Active |  80 | 2.0.0                      | HTTP Gateway
159 | Active |  80 | 2.0.0                      | InfluxDB Gateway
160 | Active |  80 | 2.0.0                      | JMX Gateway
161 | Active |  80 | 2.0.0                      | Nagios ActiveCheck Gateway
162 | Active |  80 | 2.0.0                      | Nagios NRDP Gateway
163 | Active |  80 | 2.0.0                      | Nagios NSCA Gateway
171 | Active |  80 | 2.0.0                      | SNMP Gateway
172 | Active |  80 | 2.0.0                      | SSH Gateway
173 | Active |  80 | 2.0.0                      | Syslog Gateway
174 | Active |  80 | 2.0.0                      | XMPP Gateway
175 | Active |  80 | 2.0.0                      | SMTP Gateway
```
> Note that version of the SNAMP components may vary and depends on the installed SNAMP version.

## Root privileges
SNAMP doesn't require `root` privileges for running. But if you want to use standard ports in the configured gateways (161 for `SNMP` protocol and 80, 8080, 443 for `HTTP` protocol) then you should have `root` privileges.

## Integration in the operating system
SNAMP may be integrated as an OS System Service:

* like a native Windows Service
* like a Unix daemon process

Because of SNAMP is developed on top of Apache Karaf you can use exising [Apache Karaf Integration Guide](https://karaf.apache.org/manual/latest/wrapper).

## Clustering
SNAMP clustering solution is based on [Apache Karaf Cellar](https://karaf.apache.org/projects.html#cellar) implementation. By default, clustering is disabled. Execute

```
feature:install cellar
```

in the shell console to enable clustering. If everything went well - Cellar cluster commands are now available:
```
cluster:<TAB>
```
Please note that all your saved dashboards will not be migrated automatically into cluster. If you have plan to use clustered configuration initially please enabled clusteing after installation.

If you want to manage SNAMP cluster located in the cloud then install _cellar-cloud_ feature using `feature:install cellar-cloud` shell command.

> You may use official Cellar documentation about deployment and maintenace of the cluster. See [Apache Karaf Deployment](https://karaf.apache.org/manual/cellar/latest-4/) for more information.

To verify Cellar installation print `feature:list |grep -i cellar` and you will see the following output:
```
cellar-core                             | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Karaf clustering core
hazelcast                               | 3.7.2            |          | Started     | karaf-cellar-4.0.3       | In memory data grid
cellar-hazelcast                        | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Cellar implementation based on Hazelcast
cellar-config                           | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | ConfigAdmin cluster support
cellar-features                         | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Karaf features cluster support
cellar-kar                              | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Karaf kar cluster support
cellar-bundle                           | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Bundle cluster support
cellar-shell                            | 4.0.3            |          | Started     | karaf-cellar-4.0.3       | Cellar shell support
cellar                                  | 4.0.3            | x        | Started     | karaf-cellar-4.0.3       | Karaf clustering

```

Apache Karaf Cellar supports two kind of topologies:

* Cross topology
* Star topology

**Cross topology** is the default SNAMP topology.

_cellar_ feature should be installed on each node (virtual or physical machine) within your cluster. Do not deploy two nodes on the same machine.

Print `cluster:node-list` to verify cluster installation. The expected output should look like the following output:

```
| Id                 | Alias | Host Name     | Port
-----------------------------------------------------
x | 192.168.100.6:5701 |       | 192.168.100.6 | 5701
  | 192.168.100.7:5701 |       | 192.168.100.7 | 5701
```

`x` indicates that it's the Karaf instance on which you are logged on (the local node). If you don't see the other nodes there (whereas they should be there), it's probably due to a network issue. By default, Cellar uses multicast to discover the nodes. If your network or network interface doesn't support multicast(UDP), you have to switch to TCP/IP instead of multicast.

You can ping any node to test it:

```
snamp.root@karaf> cluster:node-ping node1:5701
PING node1:5701
from 1: req=node1:5701 time=11 ms
from 2: req=node1:5701 time=12 ms
from 3: req=node1:5701 time=13 ms
from 4: req=node1:5701 time=7 ms
from 5: req=node1:5701 time=12 ms
```

Now synchronize all nodes in the cluster:

```
snamp.root@karaf> cluster:sync
Synchronizing cluster group default
        bundle: done
        config: done
        feature: done
        obr.urls: No synchronizer found for obr.urls
```

Cellar uses [Hazelcast](http://hazelcast.org/) as a cluster engine.

Advanced learning materials:

* "Learning Karaf Cellar" by Jean-Baptiste Onofré, ISBN-10: 1783984600, ISBN-13: 978-1783984602
* "Apache Karaf Cookbook" by Achim Nierbeck, ISBN-10: 1783985089, ISBN-13: 978-1783985081

### Load balancing
Load balancer distributes requests between SNAMP nodes. The recommended ordering pattern is _Round-robin_. Load balancer must have access to each SNAMP node in the cluster. In the clustered configuration your Monitoring & Management Tool should interacts with SNAMP via load balancer only.

Note that Load Balancer might require a special configuration because interaction between your Monitoring & Management Tool and SNAMP might be based on connectionless protocol, such as SNMP.

### Handling notifications in bi-directional protocols
Some protocols supported by SNAMP have bi-directional nature of communication. For example, JMX is not a request-response protocol. Asynchronous event can be raised at both sides: JMX Agent (such as JConsole or VisualVM) may send attribute request, on the other side MBean may emit notification. In the clustered environment each SNAMP node with configured JMX Connector has individual connection to the single MBean. In this case emitted notification will be caught by multiple SNAMP nodes and routed to the Management & Monitoring tool. This tool will receive multiple duplicates of the same notification.

> Don't care about sequence number of notifications. Sequence number is synchronized across cluster.

This issue can be produced by JMX Connector, NSCA Gateway, NRDP Gateway and several other components (read documentation of each [Connector](connectors/introduction.md) or [Gateway](gateways/introduction.md) carefully). Solution is to choose a **leader node** responsible for delivery of notifications and other reverse-way information. Leader node will be selected dynamically using the leader election mechanism. The notifications from managed resources will be ignored by non-leader nodes but leader node duplicates notifications to all non-leader nodes using distributed messaging so listener will receive correct notification even on non-leader nodes.
