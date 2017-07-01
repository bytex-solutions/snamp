SNAMP Documentation
====
This is a top-level page of SNAMP technical documentation. Here you can found all the necessary information about any aspect of installation, configuration etc.

## Fundamentals
* [Overview](overview.md) - basic terms and concepts. Getting familiar with a platform and modules
* [Resource Connectors](connectors/introduction.md) - about SNAMP Resource Connectors
* [Supervisors](supervisors/introduction.md) - about SNAMP Supervisors
* [Gateways](gateways/introduction.md) - about SNAMP Gateways

## Administrator's Guide
* [Installation](installation.md) - how to install SNAMP in standalone and clustered configuration
* [Performance](performance.md) - how to tune SNAMP to fit performance requirements
* [Configuration](configuration.md) - how to configure SNAMP installation
* [Management](mgmt.md) - how to manage SNAMP
* [Updating](updating.md) - how to install new SNAMP components and update old ones
* [Release Notes](ReleaseNotes.md) - what's new in the current version of SNAMP

## User's Guide
* [Overview of Web Console](webconsole/overview.md) - monitoring, tracing and elasticity management from browser
* [Overview of SNAMP command-line interface](cli.md) - configuring SNAMP from command line
* Configuration using SNAMP Web Console
  - [Resource connectors](webconsole/config-connectors.md) - configuration of managed resources
  - [Resource groups](webconsole/config-groups.md) - configuration of resource groups
  - [Supervisors](webconsole/config-supervisors.md) - configuration of supervisors
  - [Gateways](webconsole/config-gateways.md) - configuration of gateways
* [Dashboard of charts](webconsole/charts.md) - display metrics in the form of charts
* [E2E Analysis](webconsole/e2e.md) - display end-to-end communication topology using tracing information
* [Notifications](webconsole/notifications.md) - important notifications about monitoring objects

Advanced configuration:
* [Global properties](webconsole/global-props.md) - configuration of global SNAMP properties
* [Thread pools](webconsole/thread-pools.md) - tuning of thread pools in SNAMP

## Developer's Guide
* [Connecting resources using Groovy](connectors/groovy-connector.md) - how to connect object of monitoring to SNAMP using Groovy scripts
* [Monitoring connected resources using Groovy](gateways/groovy-gateway.md) - how to collect and process management information from SNAMP using Groovy scripts
* [SNAMP Instrumentation Library](instrumentation/introduction.md) - how enable send monitoring and tracing information from your applications to SNAMP

## Advanced Topics
* [Management Information Model](inform_model.md) - how SNAMP represents and interprets management information
* **SNAMP Integration Tools** allow to integrate SNAMP with other software in your company:
  - [Resource Discovery](resource-discovery.md) - how to automatically register new resources in SNAMP using REST API
