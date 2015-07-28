SNAMP Management Interface
====
SNAMP provides JMX management interface which allows you to configure resource adapters, resource connectors, monitoring counters, staring/stopping SNAMP components and etc.

## JMX settings
JMX management settings located in `<snamp>/etc/org.apache.karaf.management.cfg`.

Default settings are:
* RMI Registry Port `1099`
* RMI Server Port `44444`

For localhost connection you may use the following JMX service URL:
```
service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root
```

`com.bytex.snamp.management:type=SnampCore` managed bean (MBean) provides SNAMP management functions

## SNAMP MBeans
`com.bytex.snamp.management:type=SnampCore` supplies the following useful attributes which can be used in health monitoring:
* _StatisticRenewalTime_ - counters renewal time, in millis
* _FaultsCount_ - a number of faults caused in SNAMP. This attribute interprets a log entry with `error` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _WarningMessagesCount_ - a number of faults caused in SNAMP. This attribute interprets a log entry with `warning` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _DebugMessagesCount_ - a number of faults caused in SNAMP. This attribute interprets a log entry with `debug` level as a fault. This counter resets every time when _StatisticRenewalTime_ is reached.
* _InformationMessagesCount_ - a number of faults caused in SNAMP. This attribute interprets a log entry with `info` level as a fault. This counter resets every time when _InformationMessagesCount_ is reached.

Other attributes and operations can be discovered using JConsole or VisualVM tools.
