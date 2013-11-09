# How to: Writing custom Management Connectors
There are two ways of implementation of custom Management Connector:
* With late-bound access to the source management target, where schema of attributes and notifications can be resolved only at runtime (after management connector instantiation). Examples of such management targets: Java Management Beans, SNMP agents, WSDM resources and etc. In this case you should inheris from [AbstractManagementConnector](javadoc/com/snamp/connectors/AbstractManagementConnector.html).
* With early-bound access to the source management target, where source doesn't have any MIB and shemas. In this case, Management Connector should define MIB schema. In this case you should inherits from [ManagementConnectorBean](javadoc/com/snamp/connectors/ManagementConnectorBean.html).

Whichever way you choose, you must implement [ManagementConnector](javadoc/com/snamp/connectors/ManagementConnector.html) interface.

You have to go through the following steps:
1. Add SNAMP Agent JAR and JSPF into your dependencies;
1. Implements [ManagementConnector](javadoc/com/snamp/connectors/ManagementConnector.html) interface, or inherits from [AbstractManagementConnector](javadoc/com/snamp/connectors/AbstractManagementConnector.html) or [ManagementConnectorBean](javadoc/com/snamp/connectors/ManagementConnectorBean.html).
1. Implements attributes and notifications discovery.
1. Implements [management connector factory](javadoc/com/snamp/connector/AbstractManagementConnectorFactory.html)
1. Your management connector factory should have parameterless constructor
1. Marks factory implementation with [PluginImplementation](http://data.xeoh.net/jspf/api/net/xeoh/plugins/base/annotations/PluginImplementation.html) annotation.

For more details, see the following guides:

## Management connector with late-bound access to management target
The following example describes how to write custom management connector that converts entries from Java Properties file into management attributes. Management connector allows to overwrite attribute values.
