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
snamp:cluster-member | Status of the SNAMP cluster member                    
snamp:configure-attribute | Configure new or existing attribute assigned to the managed resource         
snamp:configure-attribute-checker | Configure health checker based on attribute value
snamp:configure-auto-scaling | Configure auto-scaling parameters for group of resources (supervisor should be previously configured for the group)
snamp:configure-discovery-service | Configure resource discovery for group of resources (supervisor should be previously configured for the group)
snamp:configure-event | Configure new or existing event (notification) assigned to the managed resource                 
snamp:configure-gateway | Register, modify or delete gateway instance
snamp:configure-health-check-trigger | Configure trigger for health check associated with resource group (supervisor should be previously configured for the group)
snamp:configure-operation | Configure new or existing operation (notification) assigned to the managed resource
snamp:configure-resource | Register, modify or delete managed resource using the specified connector and connection string              
snamp:configure-resource-group | Register, modify or delete explicit configuration of resource group          
snamp:configure-scaling-policy | Configure scaling policies for group of resources (supervisor should be previously configured for the group)
snamp:configure-supervisor | Register, modify or delete configuration of supervisor associated with group of resources
snamp:configure-thread-pool | Register, modify or delete configuration of thread pool that can be assigned to resource connector or gateway using `threadPool` configuration property
snamp:gateway-instance | Display configuration of the specified gateway instance
snamp:gateway-instances | List of configured gateways instances
snamp:installed-components | List of installed SNAMP components including gateways and resource connectors
snamp:installed-connectors | List of installed resource connectors              
snamp:installed-gateways | List of installed gateways
snamp:installed-supervisors | List of installed supervisors
snamp:load-configuration | Load configuration of gateways, managed resources, groups and supervisors from external source. Configuration should be stored in JSON format
snamp:manage-connector | Enable or disable specified type of resource connector
snamp:manage-gateway | Enable or disable specified type of gateway                    
snamp:manage-supervisor | Enable or disable specified type of supervisor
snamp:read-attributes | Read attributes
snamp:reset-config | Setup empty SNAMP configuration                   
snamp:resource | Show configuration of the managed resource including attributes, events and operations
snamp:resource-group | Show configuration of resource group including attributes, events and operations
snamp:resource-groups | List of explicitly configured resource groups
snamp:resource-metrics | Collect metrics provided by managed resources
snamp:resources | List of configured managed resources                        
snamp:restart | Restart all gateways and connectors
snamp:save-configuration | Create backup configuration of gateways, managed resources, groups and supervisors into external file in JSON format
snamp:supervisor | Show configuration of supervisor
snamp:supervisors | List of configured supervisors
snamp:thread-pools | List of configured thread pools
snamp:version | Show version of SNAMP platform

Special commands for DevOps specialists:

Command | Description
---- | ----
snamp:debug-attribute-checker | Execute health checker based on attribute using real or test data. Useful for debugging
snamp:debug-health-status-trigger | Execute health check trigger using real or test data. Useful for debugging
snamp:debug-scaling-policy | Evaluate scaling policy. Useful for debugging
snamp:receive-message | Wait for cluster-wide message transmitted using command `post-message`
snamp:post-message | Send cluster-wide message. Useful for debugging clustered configuration of SNAMP



Use `--help` flag to know more information about command and its parameters:
```bash
snamp:configure-resource --help
```
