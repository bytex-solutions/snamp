Nagios Resource Adapter
====
Nagios Resource Adapter allows to collect monitoring and management information from all resources connected to SNAMP using [active check](http://nagios.sourceforge.net/docs/3_0/activechecks.html). Simply, this adapter is just a HTTP endpoint that returns information in [Nagios Plugin Format](http://nagios.sourceforge.net/docs/3_0/pluginapi.html) about managed resource.

Nagios Resource Adapter supports the following features (if they are supported by managed resources too):

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

This adapter supports the following output formats for each attribute:
* Plain scalar value of the attribute
* Service status with associated descriptive value
* Extended output with thresholds as it is described in [Nagios Plugin Format](http://nagios.sourceforge.net/docs/3_0/pluginapi.html)

Service status will be detected automatically by resource adapter. But the detection algorithm depends on thresholds (warning/criticial/min/max levels) specified in the configuration parameters.

You may use `curl` or `wget` utilities in the `bash` script when you configure Nagios.

## Configuration Parameters
Nagios Resource Adapters doesn't provide any recognizable configuration parameters.

## Configuring attributes
The following configuration parameters of the attributes have influence on Nagios Resource Adapter behavior:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
serviceName | String | No | Used to identify an external service. If it is not specified then attribute name will be used instead. The service name helps to specify more informative name of the monitored resource in Nagios. | `Laptop`
criticalThreshold | Threshold | No | Used to specify a threshold for a critical level of the service based on the attribute value. It should be specified in [Nagios Threshold Format](https://nagios-plugins.org/doc/guidelines.html#THRESHOLDFORMAT) | `~:10`
warningThreshold | Threshold | No | Used to specify a threshold for a warning level of the service based on the attribute value It should be specified in [Nagios Threshold Format](https://nagios-plugins.org/doc/guidelines.html#THRESHOLDFORMAT) | `10:`
minValue | String | No | The minimum possible value (exclusive) of the attribute | `10`
maxValue | String | No | The maximum possible value (exclusive) of the attribute | `100`
label | String | No | A label of the metric. If it is not specified then user-defined attribute name will be used instead | `mem`
units | String | No | Unit of measurement (UOM) of the attribute value. For example: `ms`, `m`, `kg`, `MB` | `MB`

`criticalThreshold`, `warningThreshold`, `minValue` and `maxValue` parameters should be used together.

For example, you have attribute with name `string` and user-defined name `str`. If _serviceName_ configuration parameter of this attribute is defined as `logService` then HTTP GET will return the following string:
```
logService OK: STRVAL
```
where `STRVAL` is a value of `string` attribute.

If _serviceName_ configuration parameter will be omitted then HTTP GET will return:
```
string OK: STRVAL
```

Another example. You have an attribute `utilized` with the following configured parameters:
* `serviceName = memory`
* `maxValue = 100`
* `minValue = 0`
* `criticalThreshold = 80`
* `warningThreshold = 60`
* `units = MB`
* User-defined name is `aom`

The possible HTTP GET results will be:
```
memory OK: 20MB | aom=20MB;60;80;0;100
memory WARNING: 65MB | aom=65MB;60;80;0;100
memory CRITICAL: 85MB | aom=85MB;60;80;0;100
```

Nagios Resource Adapter automatically resolves service status using threshold/min/max values. If thresholds are not specified then the adapter specify `CRITICAL` status only when attribute read operation causes exception.

Now add `label = utilized` configuration parameter. The possible HTTP GET results will be changed:
```
memory OK: 20MB | utilized=20MB;60;80;0;100
memory WARNING: 65MB | utilized=65MB;60;80;0;100
memory CRITICAL: 85MB | utilized=85MB;60;80;0;100
```
