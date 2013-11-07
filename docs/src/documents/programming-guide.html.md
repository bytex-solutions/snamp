# SNAMP Programming Guide
Programming guide helps you to understand software architecture of SNAMP and gives you some best practices and examples for writing custom extensions.

SNAMP is written on Java and you can use any JVM-compliant programming language for writting your own extensions. SNAMP requires Java 7 SE (or newest) for execution and programming.
## Table of contents
* [Architecture](#architecture)
  * [Management Connector](#management-connector)
  * [Adapter](#adapter)
  * [Manager](#manager)
  * [Agent configuration](#agent-configuration)
  * [Agent](#agent)
  * [Diving in-to-deep](#diving-in-to-deep)


##Architecture
The architecture of SNAMP platform displayed on the following picture:
![SNAMP Architecture](/images/architecture.png)

SNAMP platform consists of the following parts:
* Management connector - provides connection to the management information base and exposes
    atomic management entities (called attributes) into the SNAMP platform. This component is a back-end
    of the SNAMP platform.
* Adapter - provides conversion of the management information supplied by connector into the new
    representation and exposes this information to the world This component is a front-end of the
    SNAMP platform.
* Manager - provides SNAMP platform management interface for configuring SNAMP, such as Web console or
    command-line interface.
* (Agent) configuration - the configuration of the SNAMP platform that is used to instantiate
    connectors and adapter.
* Agent - the heart of the SNAMP platform that provides hosting of the connector, adapter and manager.
The following SNAMP components are pluggable (connecting to SNAMP as plug-ins):
* Management connector
* Adapter
* Manager

So, you can write your own SNAMP plug-in using Java (or another JVM-compliant programming language).

The unit of management information in SNAMP is an [attribute](/javadoc/com/snamp/connectors/AttributeMetadata.html). The management attribute consists of:
* Attribute name, that uniquely identifies this entity in the management information base.
* Attribute specifiers indicating that the attribute is read-only, write-only or bi-directional.
* Attribute [type](/javadoc/com/snamp/connectors/AttributeTypeInfo.html), that provides routines for converting source-specific attribute values into the well-known SNAMP data types and vice versa.

Attribute entity implementation should be supplied by management connector.


### Management connector
[Management connector](/javadoc/com/snamp/connectors/ManagementConnector.html) is a back-end component of SNAMP platform that provides abstraction layer over source-specific management information base. It represents every source as a collection of [management attributes](/javadoc/com/snamp/connectors/AttributeMetadata.html). Single instance of SNAMP process can holds many instances of management connectors, therefore, it is possible to unify monitoring and management of different software components that are incompatible through monitoring and management technology.

For more information about management connector, see [Diving in-to-deep: Custom Management Connectors](/custom-connector.html).
### Adapter
[Adapter](/javadoc/com/snamp/adapters/Adapter.html) is a front-end component of SNAMP platforms that provides abstraction layer between consumer-specific monitoring and management technology and management connector. This component converts the management attributes into the technology-specific entities (such as MIB's in SNMP). Single instance of SNAMP process can holds only single adapter instance.

For more information about adapters, see [Diving in-to-deep: Custom Adapters](/custom-adapter.html).
### Manager
[Manager](/javadoc/com/snamp/hosting/management/AgentManager.html) is a service-level component that organizes communication with SNAMP maintainer, such as administrator or automatic configuration. SNAMP provides two built-in managers:
* Console-based manager that allows administrator to maintain SNAMP via command-line
* Web-based manager that allows administrator to maintain and configure SNAMP via Web page

For more information about managers, see [Diving in-to-deep: Custom Agent Managers](/custom-manager.html)
### Agent configuration
[Agent configuration](/javadoc/com/snamp/hosting/AgentConfiguration.html) is an internal component that represents persistent configuration of the SNAMP. The configuration describes connection to management targets and SNAMP hosting settings (for exampe, which technology should be used for centralized management and monitoring of management targets). SNAMP configuration model consists of the following parts:
* [Agent configuration](/javadoc/com/snamp/hosting/AgentConfiguration.html) that describes Configuration Object Model
* [Configuration storage](/javadoc/com/snamp/hosting/AgentConfigurationStorage.html) that describes persistence storage for SNAMP configuration
* [Configuration format parsers](/javadoc/com/snamp/hosting/ConfigurationFormat.html) that provides parsing of streams with SNAMP configuration to Configuration Object Model and vice versa.

For more information about SNAMP configuration model, see description of [com.snamp.hosting](/javadoc/com/snamp/hosting/package-summary.html) package.


### Agent
[Agent](/javadoc/com/snamp/hosting/Agent.html) is an internal component that responsible for:
* Lifecycle of the adapter and management connectors;
* Communication between adapter and management connectors;

Simply, this component is a heart of SNAMP platform that implements the glue logic for all other SNAMP components. 

For more information about SNAMP Agent, see [Diving in-to-deep: Embedding SNAMP](/embedding.html)

## Diving in-to-deep
Additional resources for SNAMP platform development:
* Writing [Custom Management Connectors](/custom-connector.html)
* Writing [Custom Adapters](/custom-adapters.html)
* Writing [Custom Agent Managers](/custom-manager.html)
* [Embedding Gude](/embedding.html)
