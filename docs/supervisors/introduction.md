SNAMP Supervisors
====
SNAMP Supervisor is a component of SNAMP platform responsible for supervising group of managed resources. Therefore, supervisor instance should be associated with resource group in configuration. It is not required to register resource group **explicitly** except case when Resource Discovery is supported by supervisor.

Supervisor provides the following functionality:
* **Resource Discovery** responsible for discovering new managed resources and register its in resource group without manual configuration.
* **Group health status** provides aggregated health status constructed from health statuses of each resource in the group plus summary health status of the group that may include health status of entire cluster. Also, it is possible to define custom health checks which can be based on attribute values.
* **Elasticity management** is a decision engine for automatic scaling of managed resources based on scaling policies. This functionality provides efficient utilization of computation resources in the cluster.

SNAMP provides several types of supervisors. The functionality describes above depends on type of supervisor. Some supervisors may not provide elasticity management or automatic resource discovery.

Each resource group is supervised by **default** supervisor implicitly. If other type of supervisor is required then supervisor should be configured explicitly for resource group.

Detailed description for each Gateway supported by SNAMP:

Display name | Type | Resource Discovery | Group health status | Elasticity management
---- | ---- | ---- | ---- | ----
Default Supervisor | default | No | Yes | Partially (using Groovy scripts)
OpenStack Supervisor | openstack | Yes | Yes | Yes

## Resource Discovery
Resource Discovery should be configured explicitly if supervisor supports this functionality as well as resource group should be configured explicitly. Resource group should have attributes, events and operations. Without explicit configuration of resource group with management features a newly discovered managed resource will not provide monitoring information.

Configuration of resource discovery consists of **connection string template**. Connection string template used to create connection string for discovered managed resources. Format of connection string template depends on type of supervisor.

For example, OpenStack supervisor can use the following template for connection string:
```
{first(addresses.private).addr}
```
This means that final connection string is an IP address extracted from cluster node metadata in the form of JSON. Cluster node may have several IP addresses. `addresses.private` is a list of private IP addresses. `first` function returns first IP address in the list of addresses. `addr` field contains raw IP address. When new cluster node is detected by OpenStack supervisor then metadata of this node will be parsed according this template and new managed resource will be registered automatically with parsed connection string.

## Group health status
Group health status is a combination all statuses associated with each managed resource in the group plus summary health status. Summary health status may include health status of entire cluster. For example, OpenStack supervisor constructs group health status from health status of every cluster node and health status of OpenStack cluster manager.

Additionally, it is possible to define custom health checkers. Custom health checker will be executed on every managed resource in the resource group and analyzes values of attributes supplied by these managed resources. It produces health status for managed resource as a result of health check. 
