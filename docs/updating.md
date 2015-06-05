Upgrading SNAMP components
====

There are following updating scenarios available:
* Install a new version of SNAMP Resource Adapter or Resource Connector | Possible
* Install a new version of SNAMP Platform | Not Possible
* Install a new version of SNAMP Management Console | Possible

Some of these scenarios supporting Hot Upgrade without influence on availability.

## Update Resource Adapter
Open the shell console and print `feature:list -i`. Yo will see something like this:
```
Name                         | Version          | Installed | Repository              | Description
------------------------------------------------------------------------------------------------------
ibmwmq-connector-feature     | 1.0.0            | x         | snamp                   | IBM MQ Connector Karaf Feature
nsca-adapter-feature         | 1.0.0            | x         | snamp                   | NSCA Adapter Karaf Feature
jndi                         | 3.0.3            | x         | enterprise-3.0.3        | OSGi Service Registry JNDI access
nrdp-adapter-feature         | 1.0.0            | x         | snamp                   | NRDP Adapter Karaf Feature
jmx-connector-feature        | 1.0.0            | x         | snamp                   | JMX Connector Karaf Feature
http-adapter-feature         | 1.0.0            | x         | snamp                   | HTTP Adapter Karaf Feature
snmp-connector-feature       | 1.0.0            | x         | snamp                   | SNMP Connector Karaf Feature
ssh-adapter-feature          | 1.0.0            | x         | snamp                   | SSH Adapter Karaf Feature
rshell-connector-feature     | 1.0.0            | x         | snamp                   | RShell Connector Karaf Feature
pax-jetty                    | 8.1.15.v20140411 | x         | org.ops4j.pax.web-3.1.4 | Provide Jetty engine support
pax-http                     | 3.1.4            | x         | org.ops4j.pax.web-3.1.4 | Implementation of the OSGI HTTP Service
pax-http-whiteboard          | 3.1.4            | x         | org.ops4j.pax.web-3.1.4 | Provide HTTP Whiteboard pattern support
pax-war                      | 3.1.4            | x         | org.ops4j.pax.web-3.1.4 | Provide support of a full WebContainer
standard                     | 3.0.3            | x         | standard-3.0.3          | Karaf standard feature
config                       | 3.0.3            | x         | standard-3.0.3          | Provide OSGi ConfigAdmin support
region                       | 3.0.3            | x         | standard-3.0.3          | Provide Region Support
package                      | 3.0.3            | x         | standard-3.0.3          | Package commands and mbeans
http                         | 3.0.3            | x         | standard-3.0.3          | Implementation of the OSGI HTTP Service
war                          | 3.0.3            | x         | standard-3.0.3          | Turn Karaf as a full WebContainer
kar                          | 3.0.3            | x         | standard-3.0.3          | Provide KAR (KARaf archive) support
ssh                          | 3.0.3            | x         | standard-3.0.3          | Provide a SSHd server on Karaf
management                   | 3.0.3            | x         | standard-3.0.3          | Provide a JMX MBeanServer and a set of MBeans in K
eventadmin                   | 3.0.3            | x         | standard-3.0.3          | OSGi Event Admin service specification for event-b
snmp-adapter-feature         | 1.0.0            | x         | snamp                   | SNMP Adapter Karaf Feature
aggregator-connector-feature | 1.0.0            | x         | snamp                   | Resource Aggregator Karaf Feature
jmx-adapter-feature          | 1.0.0            | x         | snamp                   | JMX Adapter Karaf Feature
groovy-connector-feature     | 1.0.0            | x         | snamp                   | Groovy Resource Karaf Feature
nagios-adapter-feature       | 1.0.0            | x         | snamp                   | Nagios Adapter Karaf Feature
groovy-adapter-feature       | 1.0.0            | x         | snamp                   | Groovy Adapter Karaf Feature
platform-feature             | 1.0.0            | x         | snamp                   | SNAMP Platform
xmpp-adapter-feature         | 1.0.0            | x         | snamp                   | XMPP Adapter Karaf Feature
cellar-core                  | 3.0.3            | x         | karaf-cellar-3.0.3      | Karaf clustering core
hazelcast                    | 3.4.2            | x         | karaf-cellar-3.0.3      | In memory data grid
cellar-hazelcast             | 3.0.3            | x         | karaf-cellar-3.0.3      | Cellar implementation based on Hazelcast
cellar-config                | 3.0.3            | x         | karaf-cellar-3.0.3      | ConfigAdmin cluster support
cellar-features              | 3.0.3            | x         | karaf-cellar-3.0.3      | Karaf features cluster support
cellar-bundle                | 3.0.3            | x         | karaf-cellar-3.0.3      | Bundle cluster support
cellar-shell                 | 3.0.3            | x         | karaf-cellar-3.0.3      | Cellar shell support
cellar                       | 3.0.3            | x         | karaf-cellar-3.0.3      | Karaf clustering
syslog-adapter-feature       | 1.0.0            | x         | snamp                   | Syslog Adapter Karaf Feature
```
Select SNAMP Resource Adapter which you want to upgrade. For example, you want to update `SNMP Resource Adapter`.

The first, you should uninstall it:
1. Print `feature:uninstall snmp-adapter-feature` in the shell console and press ENTER
2. Go to `<snamp>/deploy` folder and delete `snmp-adapter-feature-1.0.0.kar` file
3. Verify that SNMP Resource Adapter is uninstalled with `feature:list -i` command

The second, you should install a new version of SNAMP Resource Adapter:
1. Download a new version of SNAMP Resource Adapter. The name of the downloaded file must have name similar to this: `snmp-adapter-feature-1.1.0.kar`
2. Copy downloaded file into `<snamp>/deploy` folder. The folder is automatically tracked by SNAMP. Therefore, a new version of the component will be automatically installed into OSGi environment.
3. Verify installation with `feature:list -i` command. Make sure that _snmp-adapter-feature_ is presented in the command output and its version equal to `1.1.0`:

```
snmp-adapter-feature         | 1.0.0            | x         | snamp                   | SNMP Adapter Karaf Feature

```

There is alternative ways to verify Resource Adapter update:
* Print `bundle:list` and verify that `SNMP Resource Adapter` bundle exist
* Inspect log using `log:display -n 10` or ` log:exception-display ` command in the shell console
