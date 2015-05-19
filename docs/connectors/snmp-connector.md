SNMP Resource Connector
====
SNMP Resource Connector allows to manage and monitor managed resources (software components, networks switchers and routers) accessible through SNMPv2/SNMPv3 protocol.
> SNMPv1 protocol is not supported. UDP is the only supported transport protocol. `TCP` binding is not supported.

Short list of supported features:

Feature name | Description
---- | ----
Attributes | Each SNMP Managed Object with unqiue OID will be accessible as attribute. Accessing to attributes is implemented via SNMP GET/SET
Notifications | Converting SNMP Traps to SNAMP notifications
LDAP integration | LDAP integration for SNMPv3 authentication and encryption


## Connection String
SNMP Resource Connector requires an address of the SNMP Agent acting as a managed resource. This address has the following format:
```
udp://<ip-address>/<port>
```

It is recommended to use IPv4 or IPv6 address instead of Host Name to avoid DNS resolving problems.

On Linux, you may use `snmpwalk` to test SNMP connection. Make sure that SNMP Agent is SNMPv2/SNMPv3 compatible.

Examples of connection string:
* `udp://127.0.0.1/161`
* `udp://192.168.0.1/9293`

## Configuration Parameters
SNMP Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
