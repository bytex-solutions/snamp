Resource Discovery
====
Resource Discovery is a functionality provided by SNAMP through REST API that allow to register **managed resources** in resource groups. Possible usages of this functionality:
* Company has centralized registry of IT assets and want to synchronize list of managed resources from registry with list of managed resources in SNAMP
* Administrator wants to register or remove managed resources from SNAMP using `curl` or `wget` command-line tools
* Continuous integration tools (such as Jenkins) automatically connect deployed application to SNAMP
* Microservice automatically announce itself to SNAMP at boot time

Resource discovery endpoint is located at URL `http://<snamp-host>:3535/snamp/resource-discovery/<group-name>/<resource-name>`.
> Port can be changed using configuration of HTTP Server, read [this](configuration.md) about more information.

A new managed resource can be registered or removed only if it is has a membership in resource group configured **explicitly**. If group is not configured explicitly then endpoint returns 404 Not Found. When new resource is registering through Resource Discovery then its configured will be constructed from **explicit** configuration of resource group including attributes, events and operations. Only connection string and additional configuration parameters can be specified when announcing new managed resource.

The following table describes Resource Discovery operations:

* Register new managed resource in resource group
```
POST snamp/resource-discovery/<group-name>/<resource-name>
Content-Type: application/json
{
    "connectionString": "Connection string for managed resource",
    "parameters": {}
}
```
`parameters` field contains zero or more fields with configuration parameters for Resource Connector. Example of registration request:
```
{
    "connectionString": "service:jmx:rmi:///jndi/rmi://localhost:1099/karaf-root",
    "parameters": {
      "login": "karaf",
      "login": "karaf"
    }
}
```

* Remove managed resource from resource group:
```
DELETE snamp/resource-discovery/<group-name>/<resource-name>
Content-Type: text/plain
```

* Remove all managed resource from group:
```
DELETE snamp/resource-discovery/<group-name>
Content-Type: text/plain
```
