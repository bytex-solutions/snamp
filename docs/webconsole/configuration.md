SNAMP main configuration tab
====
In the main configuration tab you can change and save your current SNAMP configuration in the user-friendly mode.

SNAMP configuration has hierarchical structure and represented as a tree. It has a view similar to the following image:

![Main configuration view example](config1.png)

As you can see two main nodes are:
* Resource-adapters (SNAMP adapters)
* Managed resources (SNAMP connectors)

Main instrument for interaction with a tree is a context menu (right click in the node you want to modify):

![Context menu view example](config2.png)

You can change the value of the regular parameter (i.e. adapter/connector name or parameter of its children) just with edit menu item:

![Edit leaf view example](config3.png)

You also can change the type of adapter or managed resource's connection type:

![Type changing example](config4.png)

Webconsole provides useful function for appending/changing parameters of adapter/managed resource - just select "Add.." context submenu (or edit for existing one):

Our GUI retrieves possible params automatically. Each param has its own set of properties (mandatory, support suggestion, description etc)

![Appending new parameter](config5.png)

In case you need to append new adapter/managed resourse - just click "Add.." submenu on the corresponding main root:

Here you can select necessary type from the list of available entities:

![Appending new entity](config6.png)

Appending params for connector is similar as for adapter:

![Connector appending new parameter](config8.png)

Managed resources have its attributes. You can append it with "Add.." submenu item or change existing one.
If connectors supports suggestion - GUI will provide you set of predefined attributes. You can append your own one:

![New attribute manually](config9.png)

Or select from the list, grouped by its additional parameters:

![New attribute from the set](config10.png)

Use same workflow for modifying events of managed resource:

![New event](config11.png)

If managed resource supports "smart mode" - you can just enable it:

![Smart mode](config12.png)

In case you activated smart mode for managed resource - you may leave the attributes/events configuration blank - all necessary entities will be retrieved automatically.
If managed resource supports smart mode and it has been enabled (scheme should be saved) - you can browse attributes and events that belong to connector. 
Click "Attributes" or "Events" submenu of corresponding managed resource context menu
 
Attributes:

![Smart mode attributes](config13.png) 

Events:

![Smart mode events](config14.png)

You also can browse binding information for adapters - just select appropriate submenu within the adapter's context menu:
Binding information is group by user defined name of attribute. You can disable grouping or change another grouping column. The table is also sortable.

![Binding information](config15.png)

