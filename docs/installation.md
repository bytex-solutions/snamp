SNAMP Installation
====
This page provides installation instructions and system requirements

## System requirements
Environment:
* Java SE Runtime Environment 7. Recommended JVMs
    * OpenJDK
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

Hardware:
* Processor architectures:
    * x86
    * x64
    * ARMv6/ARMv7 (in paid subscription only)
* 2 Cores (minimum)
* 150 MB of free disk space
> Disk space requirement ignores growing of log files
* 2 GB RAM (minimum)

It is possible to run SNAMP on ARM-based hardware (such as RaspberryPi). Contact us for more information.

## Installation
1. Download latest SNAMP distribution package. You may choose package format: `zip` or `tar.gz`
1. Extract `snamp-X.Y.Z.tar.gz` or `snamp-X.Y.Z.zip` into your installation folder
> There is no limitations for installation destination

SNAMP may be launched in the following modes:
* the `regular` mode starts SNAMP in foreground, including the shell console
* the `server` mode starts SNAMP in foreground, without the shell console
* the `background` mode starts Apache Karaf in background.

See [start, stop, restart Apache Karaf](https://karaf.apache.org/manual/latest/users-guide/start-stop.html) for more information about Apache Karaf lifecycle management.

### Regular mode
The regular mode uses the `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows). It's the default start process.

It starts SNAMP as a foreground process, and displays the shell console.

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
The server mode starts SNAMP as a foreground process, but it doesn't start the shell console.

To use this mode, you use the server argument to the `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows).

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
START LEVEL 30 , List Threshold: 50
 ID | State     | Lvl | Version                    | Name
------------------------------------------------------------------------------------------
 62 | Installed |  80 | 3.0.3                      | Apache Karaf :: JNDI :: Command
 73 | Installed |  80 | 2.3.1                      | Gson
 74 | Installed |  80 | 18.0.0                     | Guava: Google Core Libraries for Java
 75 | Installed |  80 | 1.0.0                      | SNAMP Framework
 76 | Installed |  80 | 1.0.0                      | SNAMP Manager
 77 | Installed |  80 | 1.0.0                      | JAAS Configuration Manager
 78 | Installed |  80 | 1.0.0                      | JMX Support Library
 79 | Resolved  |  80 | 1.50                       | bcpkix
 80 | Resolved  |  80 | 1.50                       | bcprov
 81 | Installed |  80 | 0.10.0                     | SSHJ
 82 | Installed |  80 | 1.0.0                      | RShell Connector
 83 | Installed |  80 | 1.0.0                      | Nagios NRDP Adapter
 84 | Installed |  80 | 1.0.0                      | Syslog Adapter
 85 | Installed |  80 | 1.0.0                      | Apache Aries JNDI Bundle
 86 | Installed |  80 | 1.0.0                      | Apache Aries Util
 87 | Installed |  80 | 1.0.0                      | Apache Aries Proxy API
 88 | Installed |  80 | 2.3.4                      | SNMP4J
 89 | Installed |  80 | 1.0.0                      | SNMP Resource Adapter
 90 | Installed |  80 | 2.4.3                      | Groovy Runtime
 91 | Installed |  80 | 2.4.0.final_20141213170938 | Ivy
 92 | Installed |  80 | 1.0.0                      | Groovy Resource Adapter
 93 | Installed |  80 | 1.0.0                      | SNMP Connector
127 | Installed |  80 | 1.18.1                     | jersey-core
128 | Installed |  80 | 1.18.1                     | jersey-server
129 | Installed |  80 | 1.18.1                     | jersey-servlet
130 | Installed |  80 | 1.0.0                      | HTTP Adapter
131 | Installed |  80 | 1.0.0                      | Groovy Script Resource
132 | Installed |  80 | 1.0.0                      | JMX Adapter
133 | Installed |  80 | 1.0.0                      | JMX Connector
134 | Installed |  80 | 1.0.0                      | IBM MQ Connector
135 | Installed |  80 | 1.0.0                      | XMPP Adapter
136 | Installed |  80 | 1.0.0                      | Nagios ActiveCheck Adapter
137 | Installed |  80 | 1.50                       | bcpg
138 | Installed |  80 | 1.0.0                      | SSH Adapter
139 | Installed |  80 | 1.0.0                      | Managed Resource Aggregator
140 | Installed |  80 | 1.0.0                      | Nagios NSCA Adapter
```
> Note that version of the SNAMP components may vary and depends on the installed SNAMP version.

Sometimes, this example output does not match with what you see. This may happen for the first start of SNAMP, because SNAMP components installing asynchronously. Wait for 1-2 minutes and print `bundle:list` again.

After that, print `log:exception-display` in the shell console and verify that the command has empty output. But you might see the following message:
```
com.itworks.snamp.connectors.wmq.MQConnectorActivator$WMQJavaClassesNotInstalled: WebSphere MQ classes for Java are not installed into OSGi environment
```

This is not a fatal error but warning related to **IBM WMQ Connector** or **IBM WMB Connector**. The message informs that IBM WebSphere libraries are not installed into Apache Karaf correctly. You may choose the following ways to fix this problem:
* Uninstall these resource connectors if you don't want to monitor IBM WebSphere Message Queue or Message Broker. See [Upgrading SNAMP components](updating.md) for uninstallation instructions
* Install IBM WebSphere libraries for Java correctly. See [IBM WMQ Connector](connectors/wmq-connector.md) for more details.

Or, you can ignore this warning if you have no plans to monitor IBM WebSphere Message Queue or Message Broker.

## Root privileges
SNAMP doesn't require `root` privileges for running. But if you want to use standard ports in the configured resource adapters (161 for `SNMP` protocol and 80, 8080, 443 for `HTTP` protocol) then you should have `root` privileges.

## Integration in the operating system
SNAMP may be integrated as an OS System Service:
* like a native Windows Service
* like a Unix daemon process

Because of SNAMP is developed on top of Apache Karaf you can use exising [Apache Karaf Integration Guide](https://karaf.apache.org/manual/latest/users-guide/wrapper.html).

## Clustering
SNAMP clustering solution is based on [Apache Karaf Cellar](https://karaf.apache.org/index/subprojects/cellar.html) implementation. By default, clustering is disabled. Print
```
feature:install cellar
```
in the shell console to enable clustering. And Cellar cluster commands are now available:
```
cluster:<TAB>
```
If you want to use manage SNAMP cluster located in the cloud then install _cellar-cloud_ feature using `feature:install cellar-cloud` shell command.

> You may use official Cellar documentation about deployment and maintenace of the cluster. See [Apache Karaf Deployment](http://karaf.apache.org/manual/cellar/latest/user-guide/index.html) for more information.

To verify Cellar installation print `feature:list |grep -i cellar` and you will see the following output:
```
cellar-core                   | 3.0.1   |           | karaf-cellar-3.0.3 | Karaf clustering core
hazelcast                     | 3.2.3   |           | karaf-cellar-3.0.3 | In memory data grid
cellar-hazelcast              | 3.0.1   |           | karaf-cellar-3.0.3 | Cellar implementation based on Hazelcast
cellar-config                 | 3.0.1   |           | karaf-cellar-3.0.3 | ConfigAdmin cluster support
cellar-features               | 3.0.1   |           | karaf-cellar-3.0.3 | Karaf features cluster support
cellar-bundle                 | 3.0.1   |           | karaf-cellar-3.0.3 | Bundle cluster support
cellar-shell                  | 3.0.1   |           | karaf-cellar-3.0.3 | Cellar shell support
cellar                        | 3.0.1   |           | karaf-cellar-3.0.3 | Karaf clustering
cellar-dosgi                  | 3.0.1   |           | karaf-cellar-3.0.3 | DOSGi support
cellar-obr                    | 3.0.1   |           | karaf-cellar-3.0.3 | OBR cluster support
cellar-eventadmin             | 3.0.1   |           | karaf-cellar-3.0.3 | OSGi events broadcasting in clusters
cellar-cloud                  | 3.0.1   |           | karaf-cellar-3.0.3 | Cloud blobstore support in clusters
cellar-webconsole             | 3.0.1   |           | karaf-cellar-3.0.3 | Cellar plugin for Karaf WebConsole
```

Apache Karaf Cellar supports two kind of topologies:
* Cross topology
* Star topology

**Cross topology** is the default SNAMP topology. It is highly not recommended to use **Star topology** for your SNANP cluster.

_cellar_ feature should be installed on each node (virtual or physical machine) in your cluster. Do not deploy two nodes on the same machine.

Print `cluster:node-list` to verify cluster installation. The expected output should be something like this:
```
| Id             | Host Name | Port
-------------------------------------
x | node2:5702     | node2 | 5702
  | node1:5701     | node1 | 5701
```
`x` indicates that it's the Karaf instance on which you are logged on (the local node). If you don't see the other nodes there (whereas they should be there), it's probably due to a network issue. By default, Cellar uses multicast to discover the nodes. If your network or network interface don't support multicast(UDP), you have to switch to TCP/IP instead of multicast.

You can ping a node to test it:
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

Cellar uses [Hazelcast](http://hazelcast.org/) as cluster engine.

When you install the _cellar_ feature, a _hazelcast_ feature is automatically installed, providing the `<snamp>/etc/hazelcast.xml` configuration file. For most of the users, default configuration should be fine. If not, you can tailor this XML file according to your needs by adding/removing/modifying properties. Read more about Hazelcast configuration:
* [Configuring Hazelcast](http://docs.hazelcast.org/docs/3.3/manual/html/configuringhazelcast.html)
* [Cellar and Hazelcast](http://karaf.apache.org/manual/cellar/latest/user-guide/hazelcast.html)


Advanced learning materials:
* "Learning Karaf Cellar" by Jean-Baptiste Onofré, ISBN-10: 1783984600, ISBN-13: 978-1783984602
* "Apache Karaf Cookbook" by Achim Nierbeck, ISBN-10: 1783985089, ISBN-13: 978-1783985081

### Load balancing
Load balancer should distribute requests between SNAMP nodes. The recommended ordering pattern is _Round-robin_. The load balancer must have access to each SNAMP node in the cluster. In the clustered configuration your Monitoring & Management Tool should interacts with SNAMP via load balancer only.

Note that Load Balancer might require a special configuration because interaction between your Monitoring & Management Tool and SNAMP can be based on connectionless protocol, such as SNMP.

## SNAMP Management Console
How to install console.
