# SNAMP Features 
SNAMP platform consists of two main components:
* Management connector - provides access to the source management information base(MIB) and converting the management information into source independent form. This is a back-end SNAMP component;
* Adapter - converts the management information accepted from the management connector into technology-specific management information. This is a front-end SNAMP component.

Management connector and adapter exposes the following MIB entities:
* Attributes - static information associated with the management target that exposes some information about it
* Notifications - asynchronous events raised by management target (such as shutdown or exceptions)

Adapters and management connectors are independent components and can be combined as you whish.
<table>
<tr>
	<td>**Adapter**</td>
	<td>**Connector**</td>
	<td>**Features**</td>
</tr>
<tr>
	<td>SNMP</td>
	<td>JMX</td>
	<td>Read and write JMX attributes via SNMPv2 or SNMPv3. Full support of Open MBean types: composite, tabular and array type. SNMP authenticiation via simple login/password or LDAP</td>
</tr>
<tr>
	<td>SNMP</td>
	<td>IBM MQ</td>
	<td>Monitors key parameters of IBM Message Queue, such as messages per minute, via SNMPv2 or SNMPv3.</td>
</tr>
<tr>
	<td>SNMP</td>
	<td>IBM WSIB</td>
	<td>Monitors key parameters of IBM WebSphere Integration Bus via SNMPv2 or SNMPv3.</td>
</tr>
<tr>
	<td>HTTP-JSON</td>
	<td>JMX</td>
	<td>Read and write JMX attributes via HTTP using JSON. Full support of Open MBean types: composite, tabular and array type. Notification and authentication is not supported</td>
</tr>
<tr>
	<td>HTTP-REST</td>
	<td>IBM MQ</td>
	<td>Moniotors key parameters of IBM Message Queue via HTTP using JSON. Authentication is not supported.</td>
</tr>
<tr>
	<td>HTTP-REST</td>
	<td>IBM WSIB</td>
	<td>Monitors key parameters of IBM WebSphere Integration Bus (formerly known as Message Broker) via HTTP using JSON. Authentication is not supported.</td>
</tr>
</table>
