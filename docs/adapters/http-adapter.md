HTTP Resource Adapter
====

HTTP Resource Adapter allows to managed and monitor connected resources via HTTP protocol. The adapter exposes REST service which uses JSON data exchange format.

By default, REST service is available at the following URL:
```
http://<snamp-host>:8181/snamp/adapters/http/<adapter-instance-name>
```

You can change HTTP port in `<snamp>/etc/org.ops4j.pax.web.cfg` file or by typing `config:property-set -p org.ops4j.pax.web org.osgi.service.http.port 8080` in the shell console. See [Apache Karaf HTTP Service](http://karaf.apache.org/manual/latest/users-guide/http.html) for more details.

HTTP Resource Adapter supports the following features:

Feature | Description
---- | ----
Attributes | Read/write attributes of any type using GET and POST requests
Notifications | Receiving notifications asynchronously using WebSockets/Comet/SSE/Streaming

Attribute is accessible using the following URL:
```
http://<snamp-host>:8181/snamp/adapters/http/<adapter-instance-name>/attributes/<resource-name>/<attribute-name>
```

Use `GET` request to obtain attribute value and `POST` to change its value. For example:
```
HTTP POST http://localhost:8181/snamp/adapters/http/dummy-instance/attributes/java-app-server/logLevel
Content-Type: application/json

"DEBUG"
```

Also, Web client may receive notifications using one of the supported technologies:
* WebSockets
* Comet (long-polling)
* Server Sent Events
* Streaming (Forever frame)

It is recommended to use [atmosphere-javascript 2.2.6](https://github.com/Atmosphere/atmosphere-javascript) client-side library to simplify receiving notifications. URL for notifications:
```
HTTP GET http://<snamp-host>:8181/snamp/adapters/http/<adapter-instance-name>/notifications/<notif-name>
```

## Configuration Parameters

## Protocol
HTTP Resource Adapter uses JSON as representation of management information. The following table describes mapping between types of **Management Information Model** and JSON:

Management Information Model | JSON data type
---- | ----
int8 | Number
int16 | Number
int32 | Number
int64 | Number
bool | Boolean
string | String
objectname | String
char | String
bigint | Number
bigdecimal | Number

### Array

### Table
```json
{
  "type":{
    "rowType":{
      "typeName":"TestTableRow",
      "description":"Descr",
      "items":{
        "column1":{"description":"column1 descr","type":"int32"},
        "column2":{"description":"column2 descr","type":"string"}
      }
    },
    "index":["column1"],
    "typeName":"TestTable",
    "description":"Descr"
  },
  "rows":[
    {"column1":42,"column2":"String1"},
    {"column1":43,"column2":"String2"}
  ]
}
```

### Dictionary
```json
{
  "type":{
    "typeName":"dict",
    "description":"dict",
    "items":{
      "item1":{
        "description":"Dummy item",
        "type":"int32"
      }
    }
  },
  "value":{"item1":2}
}
```

### Notification
