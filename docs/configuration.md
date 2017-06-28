SNAMP Configuration Guide
====
System configuration (JVM and Apache Karaf) can be changed via set of configuration files in `<snamp>/etc` folder or Karaf shell console commands.

See [Configuring Apache Karaf](http://karaf.apache.org/manual/latest/#_configuration) for more information about Apache Karaf configuration model.

See [SNAMP Management Interface](mgmt.md) for more information about SNAMP management via JMX and REST API.

## Logging
Apache Karaf and SNAMP logs are located in `<snamp>/data/log` folder. You can configure log rotation, severity level and other logging settings using the following configurations files in `<snamp>/etc` folder:
* `org.ops4j.pax.logging.cfg` - initial log configuration (appenders, levels, log message format)
* `java.util.logging.properties` - advanced configuration properties for standard Java logging. Changing this file is not recommended.
* `org.apache.karaf.log.cfg` - display configuration of the log records in the shell console

See [Karaf Log Configuration](http://karaf.apache.org/manual/latest/users-guide/log.html) for more details.

## HTTP
By default the HTTP Server listens on port `3535`. You can change the port by modifying a file `<snamp>/etc/org.ops4j.pax.web.cfg` with the following content:

```
org.osgi.service.http.port=8181
```

or by typing:
```
root@karaf> config:property-set -p org.ops4j.pax.web org.osgi.service.http.port 3535
```

The change will take effect immediately.

## Clustering
When you install the _cellar_ feature, a _hazelcast_ feature is being automatically installed, providing the `<snamp>/etc/hazelcast.xml` configuration file. For most of the users, default configuration should be appropriate. If not, you can tailor this XML file according to your needs by adding/removing/modifying properties. Read more about Hazelcast configuration:

* [Configuring Hazelcast](http://docs.hazelcast.org/docs/3.7.2/manual/html-single/index.html#understanding-configuration)
* [Cellar and Hazelcast](https://karaf.apache.org/manual/cellar/latest-4/#_core_runtime_and_hazelcast)
