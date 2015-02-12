package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.configuration.SerializableAgentConfiguration;
import com.itworks.snamp.configuration.diff.ConfigurationDiffEngine;
import com.itworks.snamp.management.jmx.OpenMBean;
import javafx.scene.control.Tab;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

import javax.management.openmbean.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 * @date 10.02.2015
 */
final class SnampConfigurationAttribute  extends OpenMBean.OpenAttribute<CompositeData, CompositeType> {

    private static final String NAME = "configuration";

    private static final CompositeType SNAMP_CONFIGURATION_DATA;
    private static final CompositeType CONNECTOR_METADATA;
    private static final CompositeType ADAPTER_METADATA;
    private static final CompositeType EVENT_METADATA;
    private static final CompositeType ATTRIBUTE_METADATA;
    private static final TabularType CONNECTOR_EVENT_MAP_TYPE;
    private static final TabularType CONNECTOR_ATTRIBUTE_MAP_TYPE;
    private static final TabularType SIMPLE_MAP_TYPE;
    private static final TabularType ADAPTER_MAP_TYPE;
    private static final TabularType CONNECTOR_MAP_TYPE;



    static{
        try {
            SIMPLE_MAP_TYPE = new TabularType("com.itworks.management.MapType",
                    "Simple type for Map<String, String>",
                    new CompositeType("com.itworks.management.SimpleStringMap",
                            "Additional parameters for filtering suggested values",
                            new String[]{"key", "value"},
                            new String[]{"Parameter key", "Parameter value"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"key"}
            );


            EVENT_METADATA = new CompositeType("com.itworks.management.EventMetadata",
                    "SNAMP Connector Event Metadata",
                    new String[]{
                            "Category",
                            "AdditionalProperties"
                    },
                    new String[]{
                            "Connector event category",
                            "User defined property for event"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SIMPLE_MAP_TYPE
                    }
            );

            ATTRIBUTE_METADATA = new CompositeType("com.itworks.management.AttributeMetadata",
                    "SNAMP connector attribute metadata scheme",
                    new String[]{
                            "Name",
                            "ReadWriteTimeout",
                            "AdditionalProperties"
                    },
                    new String[]{
                            "Connector attribute name",
                            "Read write timeout for connector attribute",
                            "User defined properties for attribute"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.LONG,
                            SIMPLE_MAP_TYPE
                    }
            );

            CONNECTOR_ATTRIBUTE_MAP_TYPE = new TabularType("com.itworks.management.ConnectorAttributeMapType",
                    "Simple type for Map<String, EventMetadata>",
                    new CompositeType("com.itworks.management.SimpleConnectorAttributeMap",
                            "Type for holding snamp adapters configuration",
                            new String[]{"name", "attribute"},
                            new String[]{"User defined name for connector's attribute", "Attribute metadata instance"},
                            new OpenType<?>[]{SimpleType.STRING, ATTRIBUTE_METADATA}),
                    new String[]{"name"}
            );

            CONNECTOR_EVENT_MAP_TYPE = new TabularType("com.itworks.management.ConnectorEventMapType",
                    "Simple type for Map<String, AttributeMetadata>",
                    new CompositeType("com.itworks.management.SimpleConnectorEventMap",
                            "Type for holding snamp adapters configuration",
                            new String[]{"name", "event"},
                            new String[]{"User defined name for connector's event", "Event metadata instance"},
                            new OpenType<?>[]{SimpleType.STRING, EVENT_METADATA}),
                    new String[]{"name"}
            );

            CONNECTOR_METADATA = new CompositeType("com.itworks.management.ConnectorMetadata",
                    "SNAMP connector configuration metadata",
                    new String[]{
                            "ConnectionString",
                            "ConnectionType",
                            "Attributes",
                            "Events",
                            "Parameters"
                    },
                    new String[]{
                            "Management target connection string",
                            "Type of the management connector that is used to organize monitoring data exchange between" +
                                    " agent and the management provider",
                            "Attributes",
                            "Events",
                            "User defined properties for connector"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.STRING,
                            CONNECTOR_ATTRIBUTE_MAP_TYPE,
                            CONNECTOR_EVENT_MAP_TYPE,
                            SIMPLE_MAP_TYPE
                    }
            );

            ADAPTER_METADATA = new CompositeType("com.itworks.management.AdapterMetadata",
                    "SNAMP adapter configuration metadata",
                    new String[]{
                            "Name",
                            "Parameters"
                    },
                    new String[]{
                            "SNAMP adapter name",
                            "Additional properties for SNAMP adapter"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SIMPLE_MAP_TYPE
                    }
            );

            ADAPTER_MAP_TYPE = new TabularType("com.itworks.management.AdapterMapType",
                    "Simple type for Map<String, Adapter>",
                    new CompositeType("com.itworks.management.SimpleAdapterMap",
                            "Type for holding snamp adapters configuration",
                            new String[]{"name", "adapter"},
                            new String[]{"User defined name for adapter", "Adapter instance"},
                            new OpenType<?>[]{SimpleType.STRING, ADAPTER_METADATA}),
                    new String[]{"name"}
            );

            CONNECTOR_MAP_TYPE = new TabularType("com.itworks.management.ConnectorMapType",
                    "Simple type for Map<String, Connector>",
                    new CompositeType("com.itworks.management.SimpleConnectorMap",
                            "Type for holding snamp connectors configuration",
                            new String[]{"name", "connector"},
                            new String[]{"User defined name for connector", "Connector instance"},
                            new OpenType<?>[]{SimpleType.STRING, CONNECTOR_METADATA}),
                    new String[]{"name"}
            );

            SNAMP_CONFIGURATION_DATA = new CompositeType("com.itworks.management.SnampConfiguration",
                    "SNAMP main configuration metadata",
                    new String[]{
                            "ResourceAdapters",
                            "ManagedResources"
                    },
                    new String[]{
                            "SNAMP resource adapters configuration",
                            "SNAMP managed resources configuration"},
                    new OpenType<?>[]{
                            ADAPTER_MAP_TYPE,
                            CONNECTOR_MAP_TYPE
                    }
            );
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static TabularDataSupport parseConnectorAttributes(final Map<String,
            AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> map) throws OpenDataException {
        final TabularDataSupport attributesMap = new TabularDataSupport(CONNECTOR_ATTRIBUTE_MAP_TYPE);
        for (final String attributeName : map.keySet()) {
            final Map<String, Object> attributeMap = Maps.newHashMapWithExpectedSize(ATTRIBUTE_METADATA.keySet().size());

            // parse attribute system name
            attributeMap.put("Name", map.get(attributeName).getAttributeName());

            // parse attribute read write timeout if exists
            if (map.get(attributeName).getReadWriteTimeout() != TimeSpan.INFINITE) {
                attributeMap.put("ReadWriteTimeout", map.get(attributeName).getReadWriteTimeout().convert(TimeUnit.MILLISECONDS).duration);
            }

            // parse attribute user defined parameters
            attributeMap.put("AdditionalProperties", transformAdditionalPropertiesToTabularData(map.get(attributeName).getParameters()));

            // append current attribute to the map
            attributesMap.put(new CompositeDataSupport(attributesMap.getTabularType().getRowType(),
                    ImmutableMap.<String, Object>of(
                            "name", attributeName,
                            "attribute", new CompositeDataSupport(ATTRIBUTE_METADATA, attributeMap))));
        }
        return attributesMap;
    }

    private static TabularDataSupport parseConnectorEvents(final Map<String,
            AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> map) throws OpenDataException {
        final TabularDataSupport eventMap = new TabularDataSupport(CONNECTOR_EVENT_MAP_TYPE);
        for (final String eventName : map.keySet()) {
            final Map<String, Object> eventSimpleMap = Maps.newHashMapWithExpectedSize(EVENT_METADATA.keySet().size());

            // parse attribute system name
            eventSimpleMap.put("Category", map.get(eventName).getCategory());

            // parse attribute user defined parameters
            eventSimpleMap.put("AdditionalProperties", transformAdditionalPropertiesToTabularData(map.get(eventName).getParameters()));

            // append current attribute to the map
            eventMap.put(new CompositeDataSupport(eventMap.getTabularType().getRowType(),
                    ImmutableMap.<String, Object>of(
                            "name", eventName,
                            "event", new CompositeDataSupport(EVENT_METADATA, eventSimpleMap))));
        }
        return eventMap;
    }

    private static TabularDataSupport transformAdditionalPropertiesToTabularData(final Map<String, String> map) throws OpenDataException {
        final TabularDataSupport tabularDataSupport =  new TabularDataSupport(SIMPLE_MAP_TYPE);
        for (final String key : map.keySet()) {
            tabularDataSupport.put(new CompositeDataSupport(tabularDataSupport.getTabularType().getRowType(),
                    ImmutableMap.<String, Object>of(
                            "key", key,
                            "value", map.get(key))));
        }
            return tabularDataSupport;
    }

    /**
     * Initializes a new attribute.
     */
    SnampConfigurationAttribute() {
        super(NAME, SNAMP_CONFIGURATION_DATA);
    }

    private static CompositeData snampConfigurationToJMX(final AgentConfiguration configuration) throws OpenDataException {
        final Map<String, Object> schema = Maps.newHashMapWithExpectedSize(SNAMP_CONFIGURATION_DATA.keySet().size());

        final TabularDataSupport adapterMap = new TabularDataSupport(ADAPTER_MAP_TYPE);
        // adapter parsing
        final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapterMapConfig =
                configuration.getResourceAdapters();
        for (final String adapterName : adapterMapConfig.keySet()) {
            final Map<String, Object> currentAdapter = Maps.newHashMapWithExpectedSize(ADAPTER_METADATA.keySet().size());

            // parse adapter's name
            currentAdapter.put("Name", adapterMapConfig.get(adapterName).getAdapterName());

            // parse adapter's user defined parameters
            currentAdapter.put("Parameters", transformAdditionalPropertiesToTabularData(adapterMapConfig.get(adapterName).getParameters()));

            // add current adapter to the main adapters map
            adapterMap.put(new CompositeDataSupport(adapterMap.getTabularType().getRowType(),
                    ImmutableMap.<String, Object>of(
                            "name", adapterName,
                            "connector", new CompositeDataSupport(ADAPTER_METADATA, currentAdapter))));
        }

        final TabularDataSupport connectorMap = new TabularDataSupport(CONNECTOR_MAP_TYPE);
        // connector parsing
        final Map<String, AgentConfiguration.ManagedResourceConfiguration> connectors = configuration.getManagedResources();
        for (final String connectorName : connectors.keySet()) {
            final Map<String , Object> currentConnector = Maps.newHashMapWithExpectedSize(CONNECTOR_METADATA.keySet().size());

            // parse connector's connection string
            currentConnector.put("ConnectionString", connectors.get(connectorName).getConnectionString());

            // parse connector's connection type
            currentConnector.put("ConnectionType", connectors.get(connectorName).getConnectionType());

            // parse connector's attributes
            final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributeMap =
                    connectors.get(connectorName).getElements(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
            currentConnector.put("Attributes", parseConnectorAttributes(attributeMap));

            // parse connector's events
            final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventMap =
                    connectors.get(connectorName).getElements(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
            currentConnector.put("Events", parseConnectorEvents(eventMap));

            // parse connector's user defined properties
            currentConnector.put("Parameters", transformAdditionalPropertiesToTabularData(connectors.get(connectorName).getParameters()));

            // append current connector to the map
            connectorMap.put(new CompositeDataSupport(connectorMap.getTabularType().getRowType(),
                    ImmutableMap.<String, Object>of(
                            "name", connectorName,
                            "adapter", new CompositeDataSupport(CONNECTOR_METADATA, currentConnector))));

        }

        schema.put("ResourceAdapters", adapterMap);
        schema.put("ManagedResources", connectorMap);
        return new CompositeDataSupport(SNAMP_CONFIGURATION_DATA, schema);
    }

    // due to wildcard generic type in value() of TabularData
    @SuppressWarnings("unchecked")
    private static AgentConfiguration JMXtoSnampConfiguration(final CompositeData data) {
        final SerializableAgentConfiguration configuration = new SerializableAgentConfiguration();

        // parse adapters
        if (data.containsKey("ResourceAdapters") && data.get("ResourceAdapters") instanceof TabularData) {
            final TabularData adaptersData = (TabularData) data.get("ResourceAdapters");
            for (final CompositeData adapterDataCurrent : (Collection<CompositeData>) adaptersData.values()) {
                final SerializableAgentConfiguration.SerializableResourceAdapterConfiguration agentConfiguration =
                        new SerializableAgentConfiguration.SerializableResourceAdapterConfiguration();
                if (adapterDataCurrent.containsKey("adapter")) {
                    final CompositeData adapterInstance = ((CompositeData) adapterDataCurrent.get("adapter"));
                    if (adapterInstance.containsKey("name")) {
                        agentConfiguration.setAdapterName((String) adapterInstance.get("name"));
                    }
                    if (adapterInstance.containsKey("Parameters") && adapterInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) adapterInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            agentConfiguration.setParameter((String) keyParam.get("key"), (String) keyParam.get("value"));
                        }
                    }
                }
                configuration.getResourceAdapters().put((String) adapterDataCurrent.get("name"), agentConfiguration);
            }
        }

        // parse connectors
        if (data.containsKey("ManagedResources") && data.get("ManagedResources") instanceof TabularData) {
            final TabularData connectorsData = (TabularData) data.get("ManagedResources");
            for (final CompositeData connectorDataCurrent : (Collection<CompositeData>) connectorsData.values()) {
                final SerializableAgentConfiguration.SerializableManagedResourceConfiguration connectorConfiguration =
                        new SerializableAgentConfiguration.SerializableManagedResourceConfiguration();

                if (connectorDataCurrent.containsKey("connector")) {
                    final CompositeData connectorInstance = (CompositeData) connectorDataCurrent.get("connector");
                    if (connectorInstance.containsKey("ConnectionString")) {
                        connectorConfiguration.setConnectionString((String) connectorInstance.get("ConnectionString"));
                    }

                    if (connectorInstance.containsKey("ConnectionType")) {
                        connectorConfiguration.setConnectionType((String) connectorInstance.get("ConnectionType"));
                    }

                    // attributes parsing
                    if (connectorInstance.containsKey("Attributes") && connectorInstance.get("Attributes") instanceof TabularData) {
                        final TabularData attributes = (TabularData) connectorInstance.get("Attributes");
                        final Map<String, SerializableAgentConfiguration.SerializableManagedResourceConfiguration.AttributeConfiguration>
                                attributesConfigMap = Maps.newHashMap();
                        for (final CompositeData attributeInstance : (Collection<CompositeData>) attributes.values()) {
                            AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration config = connectorConfiguration.newAttributeConfiguration();
                            if (attributeInstance.containsKey("attribute") && attributeInstance.get("attribute") instanceof CompositeData) {

                                final CompositeData attributeData = (CompositeData) attributeInstance.get("attribute");
                                if (attributeData.containsKey("Name")) {
                                    config.setAttributeName((String) attributeData.get("Name"));
                                }
                                if (attributeData.containsKey("ReadWriteTimeout")) {
                                    config.setReadWriteTimeout(new TimeSpan((Long) attributeData.get("ReadWriteTimeout"), TimeUnit.MILLISECONDS));
                                }

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put((String) keyParam.get("key"), (String) keyParam.get("value"));
                                    }
                                }
                            }
                            attributesConfigMap.put((String) attributeInstance.get("name"), config);
                        }
                        connectorConfiguration.setAttributes(attributesConfigMap);
                    }

                    // attributes parsing
                    if (connectorInstance.containsKey("Events") && connectorInstance.get("Events") instanceof TabularData) {
                        final TabularData events = (TabularData) connectorInstance.get("Events");
                        final Map<String, SerializableAgentConfiguration.SerializableManagedResourceConfiguration.EventConfiguration>
                                eventsConfigMap = Maps.newHashMap();
                        for (final CompositeData eventInstance : (Collection<CompositeData>) events.values()) {
                            AgentConfiguration.ManagedResourceConfiguration.EventConfiguration config = connectorConfiguration.newEventConfiguration();
                            if (eventInstance.containsKey("event") && eventInstance.get("event") instanceof CompositeData) {
                                final CompositeData attributeData = (CompositeData) eventInstance.get("event");
                                if (attributeData.containsKey("Category")) {
                                    config.setCategory((String) attributeData.get("Category"));
                                }

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put((String) keyParam.get("key"), (String) keyParam.get("value"));
                                    }
                                }
                            }
                            eventsConfigMap.put((String) eventInstance.get("name"), config);
                        }
                        connectorConfiguration.setEvents(eventsConfigMap);
                    }

                    if (connectorInstance.containsKey("Parameters") && connectorInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) connectorInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            connectorConfiguration.setParameter((String) keyParam.get("key"), (String) keyParam.get("value"));
                        }
                    }
                }

                configuration.getManagedResources().put((String) connectorDataCurrent.get("name"), connectorConfiguration);
            }
        }
        return configuration;
    }

    @Override
    public CompositeData getValue() throws IOException, ConfigurationException, OpenDataException {
        final BundleContext bundleContext = getBundleContextByObject(this);
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef =
                new ServiceReferenceHolder<>(bundleContext,ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(adminRef);
            final AgentConfiguration configuration = manager.getCurrentConfiguration();
            if (configuration == null) throw new ConfigurationException("configuration admin",
                    "Configuration admin does not contain appropriate SNAMP configuration");
            return snampConfigurationToJMX(configuration);
        }
        finally {
            adminRef.release(bundleContext);
        }
    }

    @Override
    public void setValue(final CompositeData data) throws IOException {
        if(data == null || data.values().size() == 0) throw new IllegalArgumentException("No valid input data received");

        final BundleContext bundleContext = getBundleContextByObject(this);
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef =
                new ServiceReferenceHolder<>(bundleContext,ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(adminRef);
            ConfigurationDiffEngine.merge(manager.getCurrentConfiguration(), JMXtoSnampConfiguration(data));
            manager.save();
        }
        finally {
            adminRef.release(bundleContext);
        }
    }

    @Override
    protected String getDescription() {
        return "Main SNAMP Configuration";
    }
}
