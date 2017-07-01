Upgrading SNAMP components
====

There are following updating scenarios available:
* Update SNAMP Gateways
* Update SNAMP Resource Connectors
* Update SNAMP Platform
* Update SNAMP Web Console
* Update SNAMP integration tools

Some of these scenarios support Hot Upgrade (without any availability issues).

First of all, verify version of all installed SNAMP components using `feature:list -i|grep snamp` command in SNAMP shell. Example output for this command:
```
Name                       | Version          | Required | State   | Repository              | Description
------------------------------------------------------------------------------------------------------------------------------------------------
jersey-server              | 1.19.3           |          | Started | snamp-platform          | Jersey 1.x server-side libraries
snamp-json-support         | 2.0.0            |          | Started | snamp-platform          | Various JMX helpers and JMX-to-JSON serialization
orientdb-server            | 2.2.21           |          | Started | snamp-platform          | OrientDB NoSQL Database
snamp-framework            | 2.0.0            |          | Started | snamp-platform          | SNAMP framework
snamp-core                 | 2.0.0            |          | Started | snamp-platform          | Core SNAMP bundles
snamp-security             | 2.0.0            |          | Started | snamp-platform          | SNAMP security layer
snamp-web-support          | 2.0.0            |          | Started | snamp-platform          | SNAMP support bundles for Web
snamp-scripting-support    | 2.0.0            |          | Started | snamp-platform          | SNAMP support bundles for scripting
snamp-ssh-support          | 2.0.0            |          | Started | snamp-platform          | Set of Java libraries for SSH
snamp-snmp-support         | 2.0.0            |          | Started | snamp-platform          | Set of Java libraries for SNMPv2/SNMPv3
snamp-instrumentation      | 1.0.0            |          | Started | snamp-platform          | SNAMP instrumentation library
snamp-text-support         | 2.0.0            |          | Started | snamp-platform          | SNAMP support bundles for working with text templ
snamp-discovery-over-http  | 2.0.0            | x        | Started | snamp-integration       | REST Discovery Service
snamp-devops-tools         | 2.0.0            | x        | Started | snamp-standard          | SNAMP command-line tools for DevOps
snamp-management           | 2.0.0            | x        | Started | snamp-standard          | SNAMP management tools
snamp-e2e-analyzer         | 2.0.0            | x        | Started | snamp-standard          | Data Analysis services
snamp-web-console          | 2.0.0            | x        | Started | snamp-standard          | SNAMP Web Console
snamp-groovy-gateway       | 2.0.0            | x        | Started | snamp-gateways          | SNAMP Groovy Gateway
snamp-http-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP HTTP Gateway
snamp-influx-gateway       | 2.0.0            | x        | Started | snamp-gateways          | SNAMP Gateway for InfluxDB
snamp-jmx-gateway          | 2.0.0            | x        | Started | snamp-gateways          | SNAMP JMX Gateway
snamp-nagios-gateway       | 2.0.0            | x        | Started | snamp-gateways          | SNAMP Gateway for Nagios (active check)
snamp-nrdp-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP Gateway for Nagios (passive check via NRDP)
snamp-nsca-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP Gateway for Nagios (passive check via NSCA)
snamp-snmp-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP SNMPv2/SNMPv3 Gateway
snamp-ssh-gateway          | 2.0.0            | x        | Started | snamp-gateways          | SNAMP SSH Gateway
snamp-syslog-gateway       | 2.0.0            | x        | Started | snamp-gateways          | SNAMP SysLog Gateway
snamp-xmpp-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP XMPP Gateway
snamp-smtp-gateway         | 2.0.0            | x        | Started | snamp-gateways          | SNAMP SMTP Gateway
snamp-default-supervisor   | 2.0.0            | x        | Started | snamp-connectors        | SNAMP Default Supervisor
snamp-openstack-supervisor | 2.0.0            | x        | Started | snamp-connectors        | SNAMP supervisor for OpenStack environment
snamp-actuator-connector   | 2.0.0            | x        | Started | snamp-connectors        | SNAMP connector for Spring Actuator
snamp-stub-connector       | 2.0.0            | x        | Started | snamp-connectors        | SNAMP stub connector for tests
snamp-stream-connector     | 2.0.0            | x        | Started | snamp-connectors        | SNAMP connector for capturing measurements
snamp-composite-connector  | 2.0.0            | x        | Started | snamp-connectors        | Composite Connector Karaf Feature
snamp-groovy-connector     | 2.0.0            | x        | Started | snamp-connectors        | Groovy Resource Karaf Feature
snamp-http-acceptor        | 2.0.0            | x        | Started | snamp-connectors        | HTTP Acceptor Karaf Feature
snamp-jmx-connector        | 2.0.0            | x        | Started | snamp-connectors        | JMX Connector Karaf Feature
snamp-modbus-connector     | 2.0.0            | x        | Started | snamp-connectors        | Modbus Connector Karaf Feature
snamp-rshell-connector     | 2.0.0            | x        | Started | snamp-connectors        | RShell Connector Karaf Feature
snamp-snmp-connector       | 2.0.0            | x        | Started | snamp-connectors        | SNMP Connector Karaf Feature
snamp-zipkin-connector     | 2.0.0            | x        | Started | snamp-connectors        | Zipkin Connector Karaf Feature
```
As you can see, SNAMP components divided into several repositories (column `Repository`):
* _snamp-platform_ - core SNAMP components. They cannot be updated using Hot Upgrade mechanism
* _snamp-standard_ - standard set of SNAMP components such as Web Console, management shell (commands started from _snamp:_ prefix) and data analyzer. These components are packed into single file called `standard-X.Y.Z.kar` (where `X.Y.Z` is a version) and located in `<snamp>/deploy/` folder
* _snamp-connectors_ - set of SNAMP Resource Connectors. These components are packed into single file called `connectors-X.Y.Z.kar` (where `X.Y.Z` is a version) and located in `<snamp>/deploy/` folder
* _snamp-gateways_ - set of SNAMP Gateways. These components are packed into single file called `gateways-X.Y.Z.kar` (where `X.Y.Z` is a version) and located in `<snamp>/deploy/` folder
* _snamp-integration_ - set of tools for integration SNAMP with third-party software. These components are packed into single file called `integration-X.Y.Z.kar` (where `X.Y.Z` is a version) and located in `<snamp>/deploy/` folder

## Upgrade SNAMP Platform
Upgrading SNAMP Platform cannot be completed without restart. This process can be done using the following guide:
1. Create backup configuration using `snamp:save-configuration` shell command or from SNAMP Web Console
1. Download latest version of SNAMP
1. Shutdown existing SNAMP nodes
1. Delete existing SNAMP folder
1. Install new version of SNAMP
1. Verify version using `snamp:version` shell command
1. Load configuration backup using `snamp:load-configuration` shell command or from SNAMP Web Console

## Upgrade other components
Upgrading SNAMP components can be completed without shutting down SNAMP nodes. This process can be done using the following guide:
1. Download latest version of SNAMP
1. Extract necessary **.kar** file from `deploy` folder inside of downloaded archive
1. Go to `<snamp>/deploy` folder of existing installation of SNAMP and delete **.kar** willing to upgrade
1. Place extracted **.kar** file from downloaded archive into `<snamp>/deploy` folder
1. Verify version of installed components using `feature:list -i|grep snamp` shell command
