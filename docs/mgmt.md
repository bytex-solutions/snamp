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

`com.itworks.snamp.management:type=SnampCore` managed bean (MBean) provides SNAMP management functions
