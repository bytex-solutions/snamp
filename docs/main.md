SNAMP Documentation
====
This is a top-level page of SNAMP technical documentation. Here you can found all the necessary information about any aspect of installation, configuration etc.

## Fundamentals

* [Overview](overview.md) - basic terms and concepts. Getting familiar with a platform and modules
* [Resource Connectors](connectors/introduction.md) - about SNAMP Resource Connectors
* [Supervisors](supervisors/introduction.md) - about SNAMP Supervisors
* [Gateways](gateways/introduction.md) - about SNAMP Gateways

## User's Guide

* [Overview of Web Console](webconsole/overview.md) - monitoring, tracing and elasticity management from browser
* [Configuration](configuration.md) - how to configure SNAMP

## Administrator's Guide

* [Installation](installation.md) - how to install SNAMP in standalone and clustered configuration
* [Performance](performance.md) - how to tune SNAMP to fit performance requirements
* [Management](mgmt.md) - how to manage SNAMP
* [Updating](updating.md) - how to install new SNAMP components and update old ones
* [Release Notes](ReleaseNotes.md) - what's new in the current version of SNAMP

## Developer's Guide

* [Connecting resources using Groovy](connectors/groovy-connector.md) - how to connect object of monitoring to SNAMP using Groovy scripts
* [Monitoring connected resources using Groovy](gateways/groovy-adapter.md) - how to collect and process management information from SNAMP using Groovy scripts
* **SNAMP Instrumentation Library** - how enable send monitoring and tracing information from your applications to SNAMP
  * [for Java](instrumentation/java.md) - for Java-based applications
* [Resource Discovery](resource-discovery.md) - how to automatically register new resources in SNAMP using REST API

## Advanced Topics

* [Management Information Model](inform_model.md) - how SNAMP represents and interprets management information
* [Authentication & Authorization](jaas.md) - how to configure Java Authentication & Authorization Service

## Examples
* [Step-by-step guide](examples/complete-example.md) for SNAMP installation and configuration. Real-life example shows how to setup JMX-to-HTTP, JMX-to-SNMP, cmdline-to-HTTP, cmdline-to-SNMP bridges within a single SNAMP instance.
