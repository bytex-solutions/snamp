Nagios Resource Adapter
====
Nagios Resource Adapter allows to collect monitoring and management information from all resources connected to SNAMP using **active check**. Simply, this adapter is just a HTTP endpoint that returns information in [Nagios Plugin Format](http://nagios.sourceforge.net/docs/3_0/pluginapi.html) about managed resource.

JMX Resource Adapter supports the following features (if they are supported by managed resources too):

Feature | Description
---- | ----
Attributes | The attribute and its value will be converted into _Nagios Plugin Format_ and placed into HTTP response

Notifications are not supported by Nagios Resource Adapter.

Use the following URL for retrieving attributes:
```
HTTP GET
http://<snamp-host>:<port>/snamp/adapters/nagios/<adapter-instance-name>/attributes/<resource-name>/<attribute-name>
```

`8181` is the default port of SNAMP web server.
