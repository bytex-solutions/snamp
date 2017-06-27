SNAMP Command-line Interface
====
Execute SNAMP using the following command

```bash
sh ./snamp/bin/karaf
```

... and you will see the following welcome screen:
```
_____ _   _          __  __ _____  
/ ____| \ | |   /\   |  \/  |  __ \
| (___ |  \| |  /  \  | \  / | |__) |
\___ \| . ` | / /\ \ | |\/| |  ___/
____) | |\  |/ ____ \| |  | | |
|_____/|_| \_/_/    \_\_|  |_|_|

Bytex SNAMP (2.0.0)

Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.
Hit '<ctrl-d>' or type 'system:shutdown' or 'logout' to shutdown SNAMP.

snamp.root@karaf>
```

Now you can use standard Karaf commands described [here](http://karaf.apache.org/manual/latest/#_using_the_console).
Also, SNAMP provides additional set of commands (started with **snamp** prefix):

Command | Description
---- | ----
snamp:adapter | Display configuration of the specified adapter instance
snamp:configure-adapter | Configure new or existing instance of adapter
snamp:configure-attribute | Configure new or existing attribute assigned to the managed resource
snamp:configure-event | Configure new or existing event (notification) assigned to the managed resource
snamp:configure-operation | Configure new or existing operation (notification) assigned to the managed resource
snamp:configure-resource | Configure new or existing managed resource using the specified connector and connection string
snamp:adapter-instances | List of configured adapter instances
snamp:resources | List of configured managed resources
snamp:read-attributes | Read attributes
snamp:listen-events | Wait for events and display each of them
snamp:suspend-events | Suspend events raised by the specified managed resource. This command is not cluster-wide
snamp:resume-events | Resume events raised by the specified managed resource. This command is not cluster-wide
snamp:delete-adapter | Delete adapter instance from configuration
snamp:delete-adapter-param | Delete configuration parameter from the specified adapter instance
snamp:delete-attribute | Delete attribute from the specified managed resource
snamp:delete-attribute-param | Delete configuration parameter from the specified attribute
snamp:delete-event | Delete event (notificaiton) from the specified managed resource
snamp:delete-event-param | Delete configuration parameter from the specified event
snamp:delete-operation | Delete operation from the specified managed resource
snamp:delete-resource | Delete managed resource from configuration
snamp:delete-resource-param | Delete configuration parameter from the specified resource
snamp:dump-jaas | Save JAAS configuration in JSON format into the specified file
snamp:setup-jaas | Load JAAS configuration from the external file
snamp:installed-adapters | List of installed adapters
snamp:installed-components | List of all installed SNAMP components including adapters and connectors
snamp:installed-connectors | List of installed resource connectors
snamp:reset-config | Setup empty SNAMP configuration
snamp:resource | Show configuration of the managed resource including attributes, events and operations
snamp:restart | Restart all adapters and connectors
snamp:start-adapter | Start bundle with individual adapter
snamp:start-connector | Start bundle with individual resource connector
snamp:stop-adapter | Stop bundle with individual adapter
snamp:stop-connector | Stop bundle with individual resource connector
snamp:version | Show version of SNAMP platform
snamp:cluster-member | Status of the SNAMP cluster member
snamp:resource-metrics | Collect metrics provided by managed resources
snamp:thread-pool-add | Register a new thread pool that can be used by resource connector or adapter through `threadPool` configuration property
snamp:thread-pool-list | List of registered thread pools
snamp:thread-pool-remove | Remove thread pool
snamp:script | Execute JavaScript-based script to configure SNAMP

Use `--help` flag to know more information about command and its parameters:
```bash
snamp:configure-resource --help
```
