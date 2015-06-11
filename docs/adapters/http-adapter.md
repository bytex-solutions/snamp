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
* WebSocket
* Comet (long-polling)
* Server Sent Events
* Streaming (Forever frame)

It is recommended to use [atmosphere-javascript 2.2.6](https://github.com/Atmosphere/atmosphere-javascript) client-side library to simplify receiving notifications. URL for notifications:
```
HTTP GET http://<snamp-host>:8181/snamp/adapters/http/<adapter-instance-name>/notifications/<notif-name>
```

## Configuration Parameters
HTTP Resource Adapters recognizes the following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
dateFormat | String | false | Configures adapter to serialize Date objects into JSON string according to the pattern provided | `yyyy-MM-dd'T'HH:mm:ss.SSSZ`

Any other configuration parameters will be ignored by adapter.

## Data formats
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
date | String

### Array
An array data that comes from managed resource will be converted into appropriate JSON array type. For example, array of `int32` values will be converted into array of Numbers (`[1, 5, 7.8, 42]`).

### Table
Table is represented in JSON format as follows:
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

`type` element describes metadata information about table:
* `typeName` - table name
* `description` - human-readable description of the table
* `rowType` - definition of columns (name, type and description)
* `index` element contains an array of columns which are the part of table index

`rows` element contains an array of table rows.

> When you change table rows (via POST request) you should not modify anything in `type` element.

The possible values of `type` element: `void`, `bool`, `int8`, `int16`, `int32`, `int64`, `char`, `string`, `objectname`, `float32`, `float64`, `bigint`, `bigdec`, `date`.

### Dictionary
Dictionary is represented in JSON format as follows:
```json
{
  "type":{
    "typeName":"Dictionary",
    "description":"Description stub",
    "items":{
      "key1":{
        "description":"Dummy item",
        "type":"int32"
      },
      "key2":{
        "description":"Dummy item",
        "type":"string"
      }
    }
  },
  "value":{"item1":2, "item2": "Hello, world"}
}
```

`type` element describes metadata information about dictionary:
* `typeName` - table name
* `description` - human-readable description of the dictionary
* `items` - definition of dictionary keys (name, type and description)

`value` element contains a dictionary key/value pairs.

### Notification
Notification received through WebSocket/Comet/SSE channel can be represented in JSON format as follows:
```json
{
  "source":"java-app-server",
  "type":"jmx.attribute.change",
  "sequenceNumber":5,
  "timeStamp":"Jun 11, 2015 12:26:54 PM",
  "message":"Log level changed",
  "userData":{
    "type":{
      "typeName":"dict",
      "description":"dict",
      "items":{
        "item1":{"description":"Dummy item","type":"int32"}
      }
    },
    "value":{"item1":2}
  }
}
```

The information model of the notification described in **Management Information Model** page.