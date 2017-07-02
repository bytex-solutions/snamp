SNAMP Management Interface
====
SNAMP provides JMX and REST management interfaces which allows to configure resource gateways, resource connectors, monitoring counters, staring/stopping SNAMP components etc.

## Managing over JMX
SNAMP JMX interface provides the following information and operations related to running instance:
* Disable or enable installed Resource Connector
* Disable or enable installed Gateway
* List of installed SNAMP components
* Information about SNAMP cluster node
* Logs
* Usage metrics
* Restart all SNAMP components
* Force leader election process when using clustering

This information can be used to control state of SNAMP instance. For programmatic configuration of internal SNAMP entities such as Resource Connectors and Gateways the administrator should use REST API.

### JMX Settings
JMX management settings are located in `<snamp>/etc/org.apache.karaf.management.cfg`.

Default settings are:

* RMI Registry Port `1099`
* RMI Server Port `44444`

For localhost connection you may use following JMX service URL:
```
service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root
```

`com.bytex.snamp.management:type=SnampCore` managed bean (MBean) provides SNAMP related management functions

### SNAMP MBeans
`com.bytex.snamp.management:type=SnampCore` supplies the following useful attributes which can be used in health monitoring:

* _StatisticRenewalTime_ - counters renewal time (ms)
* _FaultsCount_ - number of faults caused in SNAMP. This attribute interprets a log entry with `error` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _WarningMessagesCount_ - number of warnings caused in SNAMP. This attribute interprets a log entry with `warning` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _DebugMessagesCount_ - number of debug messages caused in SNAMP. This attribute interprets a log entry with `debug` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _InformationMessagesCount_ - number of faults caused in SNAMP. This attribute interprets a log entry with `info` level as a fault. This counter resets every time when _InformationMessagesCount_ is reached.

Other attributes and operations can be discovered using JConsole or VisualVM tools.

`com.bytex.snamp.management:type=SnampClusterNode` supplies information about cluster node:

* Attribute _isInCluster_ - `true` if SNAMP instance launched as a part of cluster
* Attribute _isActiveNode_ - `true` if SNAMP instance is a leader node
* Attribute _memberName_ - name of the cluster node
* Operation `resign` - execute election of leader node. After this operation previous leader node can be changed.

## Managing using REST API
