MDA Resource Connector
====
MDA is an acronym of Monitoring Data Acceptor. This means that the connector is passive and doesn't establish direct connection to the managed resource unlike many other active connectors. In other words, the data flow directed from managed resource to resource connector. The managed resources responsible for delivery monitoring data to the connector. This approach is very helpful for monitoring Public/Private Clouds with hundreds or even thousands of components.

![Communication Scheme](mda-connector.png)

MDA Resource Connector provides two type of transport for delivering monitoring data:

Transport | Description
---- | ----
HTTP | MDA Connector exposes simple REST API that is used to set attributes and delivery of notifications. This kind of transport is very useful for delivering monitoring data and metrics from components located in Public Cloud into SNAMP across Internet
[Thrift](http://thrift.apache.org/) | Binary RPC protocol that is used to set attributes and delivery of notifications. Thrift client is available for many programming languages such as `C`, `C++`, `C#`, `D`, `Delphi`, `Erlang`, `Haxe`, `Haskell`, `Java`, `JavaScript`, `PHP`, `Ruby` and etc. This kind of transport os very useful in performance critical scenarios when components located in Intranet or Private Cloud.

MDA Connector stores the delivered attributes and notification in distributed or local memory and exposes these values to the Resource Adapters. Type of the storage depends on SNAMP installation. So if you use clustered installation then MDA Connector store attributes and notifications in Hazelcast. If you use standalone installation then SNAMP use in-process Java heap.

Feature | Comments
---- | ----
Attributes | Can be delivered via HTTP or Thrift and stored into Hazelcast or Java Heap
Events | Can be delivered via HTTP or Thrift and propagated to all Resource Adapters

Appliance of MDA Connector limited by capability of making changes in the existing software components. You can select one of the following approaches:
* Assembly you software with HTTP or Thrift client satisfied to MDA Connector Service Contract
* Deploy daemon/service on the same machine with HTTP or Thrift client satisfied to MDA Connector Service Contract. This daemon uses another protocol for accessing data of your software component. This approach is very helpful when software component is legacy and modification is not allowed.

## Connection String
Connection string used to configure transport type of the MDA Connector.

### HTTP Transport
If empty connection string is specified then MDA exposes REST interface via HTTP protocol available at the following context:
```
http://<snamp-host>:3535/snamp/connectors/mda/<resource-name>
```
where `resource-name` is a name of the managed resource in SNAMP configuration.

URL context can be changed by specifying
