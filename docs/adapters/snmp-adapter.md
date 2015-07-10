SNMP Resource Adapter
====
SNMP Resource Adapter exposes management information about connected resources through SNMP protocol. It supports SNMPv2 and SNMPv3 protocol versions. You may use many powerful tools such as `snmpwalk`, HP OpenView, OpenNMS, Microsoft System Center Operations Manager, Zabbix for monitoring components connected to SNAMP.

SNMP Resource Adapter supports the following features (if they are supported by managed resources too):

Feature | Description
---- | ----
Attributes | Each attribute will be exposed as Managed Object with their own unique OID
Notifications | Each notification will be asynchronously delivered to SNMP Trap Receiver as Traps

Additionally, SNMP Resource Adapter supports integration with LDAP. You can place authentication and authorization parameters into LDAP server for more centralized control. Also, this adapter utilizes **its own internal thread pool that can be configured explicitly**.

## Configuration Parameters
SNMP Resource Adapters recognizes the following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
context | OID | Yes | Prefix OID used to filter attributes and events during agent boot process | `1.1`
engineID | HEX String | No | The engine ID is used with a hashing function to generate keys for authentication and encryption of SNMP v3 messages. If you do not specify an engine ID, one is generated when you enable the standalone SNMP agent. This parameter is for SNMPv3 protocol only | `80:00:13:70:01:7f:00:01:01:be:1e:8b:35`
snmpv3-groups | Comma-separated list of groups | No | A list of groups with users that can be authenticated on SNMP Agent. The groups can be configured locally or supplied from LDAP. This parameter is for SNMPv3 protocol only | See **User groups** section for examples
socketTimeout | Integer | No | Socket timeout (in millis) used for sending outgoing UDP packets. By default it is equal to `5000` | `2000`
restartTimeout | Integer | No | Timeout value (in millis) used by instance of the adapter to reconfigure internal database of managed objects. This configuration parameter is for experts only. Change it when you want to expose more than 256 attributes in total. By default it is equal to `10000`  | `3000`
port | Integer | Yes | Port number used to listen incoming UDP packets | `161`
hostName | IP Address | Yes | Network interface used to listen incoming UDP packets | `0.0.0.0`
ldap-uri | URI | No | Address of LDAP server. This parameter is for SNMPv3 protocol only | `ldap://127.0.0.1:389`
ldap-user
ldap-password

Note that parameters related to thread pool is omitted. See **SNAMP Configuration Guide** page for more information about thread pool configuration. All other parameters will be ignored.

## User groups (SNMPv3 only)
