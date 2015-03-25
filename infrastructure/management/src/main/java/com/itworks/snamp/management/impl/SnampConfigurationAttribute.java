package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.configuration.SerializableAgentConfiguration;
import com.itworks.snamp.configuration.diff.ConfigurationDiffEngine;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import com.itworks.snamp.jmx.OpenMBean;
import com.itworks.snamp.jmx.TabularDataBuilderRowFill;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

import javax.management.openmbean.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class SnampConfigurationAttribute  extends OpenMBean.OpenAttribute<CompositeData, CompositeType> implements CommonOpenTypesSupport {

    private static final String NAME = "configuration";

    private static final CompositeType SNAMP_CONFIGURATION_DATA;
    private static final CompositeType CONNECTOR_METADATA;
    private static final CompositeType ADAPTER_METADATA;
    private static final CompositeType EVENT_METADATA;
    private static final CompositeType ATTRIBUTE_METADATA;
    private static final TabularType CONNECTOR_EVENT_MAP_TYPE;
    private static final TabularType CONNECTOR_ATTRIBUTE_MAP_TYPE;
    private static final TabularType ADAPTER_MAP_TYPE;
    private static final TabularType CONNECTOR_MAP_TYPE;

    private static final CompositeTypeBuilder CONNECTOR_METADATA_BUILDER;
    private static final CompositeTypeBuilder ADAPTER_METADATA_BUILDER;
    private static final CompositeTypeBuilder SNAMP_CONFIGURATION_DATA_BUILDER;

    static{
        try {

            EVENT_METADATA = EVENT_METADATA_BUILDER.build();
            ATTRIBUTE_METADATA = ATTRIBUTE_METADATA_BUILDER.build();

            CONNECTOR_ATTRIBUTE_MAP_TYPE = new TabularTypeBuilder("com.itworks.management.ConnectorAttributeMapType", "Simple type for Map<String, EventMetadata>")
                    .addColumn("name", "User defined name for connector's attribute", SimpleType.STRING, true)
                    .addColumn("attribute", "Attribute metadata instance", ATTRIBUTE_METADATA, false)
                    .build();
            CONNECTOR_EVENT_MAP_TYPE = new TabularTypeBuilder("com.itworks.management.ConnectorEventMapType", "Simple type for Map<String, AttributeMetadata>")
                    .addColumn("name", "User defined name for connector's event", SimpleType.STRING, true)
                    .addColumn("event", "Event metadata instance", EVENT_METADATA, false)
                    .build();

            CONNECTOR_METADATA_BUILDER = new CompositeTypeBuilder("com.itworks.management.ConnectorMetadata", "SNAMP connector configuration metadata")
                    .addItem("ConnectionString", "Management target connection string", SimpleType.STRING)
                    .addItem("ConnectionType", "Type of the management connector that is used to organize monitoring data exchange between agent and the management provider", SimpleType.STRING)
                    .addItem("Attributes", "Attributes", CONNECTOR_ATTRIBUTE_MAP_TYPE)
                    .addItem("Events", "Events", CONNECTOR_EVENT_MAP_TYPE)
                    .addItem("Parameters", "User defined properties for connector", SIMPLE_MAP_TYPE);

            ADAPTER_METADATA_BUILDER = new CompositeTypeBuilder("com.itworks.management.AdapterMetadata", "SNAMP adapter configuration metadata")
                    .addItem("Name", "SNAMP adapter name", SimpleType.STRING)
                    .addItem("Parameters", "Additional properties for SNAMP adapter", SIMPLE_MAP_TYPE);

            CONNECTOR_METADATA = CONNECTOR_METADATA_BUILDER.build();
            ADAPTER_METADATA = ADAPTER_METADATA_BUILDER.build();

            ADAPTER_MAP_TYPE = new TabularTypeBuilder("com.itworks.management.AdapterMapType", "Simple type for Map<String, Adapter>")
                    .addColumn("name", "User defined name for adapter", SimpleType.STRING, true)
                    .addColumn("adapter", "Adapter instance", ADAPTER_METADATA, false)
                    .build();
            CONNECTOR_MAP_TYPE = new TabularTypeBuilder("com.itworks.management.ConnectorMapType", "Simple type for Map<String, Connector>")
                    .addColumn("name", "User defined name for connector", SimpleType.STRING, true)
                    .addColumn("connector", "Connector instance", CONNECTOR_METADATA, false)
                    .build();

            SNAMP_CONFIGURATION_DATA_BUILDER = new CompositeTypeBuilder("com.itworks.management.SnampConfiguration", "SNAMP main configuration metadata")
                    .addItem("ResourceAdapters", "SNAMP resource adapters configuration", ADAPTER_MAP_TYPE)
                    .addItem("ManagedResources", "SNAMP managed resources configuration", CONNECTOR_MAP_TYPE);

            SNAMP_CONFIGURATION_DATA = SNAMP_CONFIGURATION_DATA_BUILDER.build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Parse connector attributes to the TabularData object
     * @param map - the configuration entity to parse
     * @return TabularData object
     * @throws OpenDataException
     */
    private static TabularData parseConnectorAttributes(final Map<String,AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> map)
            throws OpenDataException {
        final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(CONNECTOR_ATTRIBUTE_MAP_TYPE);
        for (final String attributeName : map.keySet()) {
            builder.newRow()
                    .cell("name", attributeName)
                    .cell("attribute", ATTRIBUTE_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Name", map.get(attributeName).getAttributeName(),
                                    "ReadWriteTimeout", (map.get(attributeName).getReadWriteTimeout() != TimeSpan.INFINITE
                                            ? map.get(attributeName).getReadWriteTimeout().convert(TimeUnit.MILLISECONDS).duration
                                            : Long.MAX_VALUE),
                                    "AdditionalProperties", MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            map.get(attributeName).getParameters())
                            )))
                    .flush();
        }
        return builder.get();
    }

    /**
     * Parse connector events to the TabularData object
     * @param map - the configuration entity to parse
     * @return TabularData object
     * @throws OpenDataException
     */
    private static TabularData parseConnectorEvents(final Map<String,AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> map)
            throws OpenDataException {
        final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(CONNECTOR_EVENT_MAP_TYPE);
        for (final String eventName : map.keySet()) {
            builder.newRow()
                    .cell("name", eventName)
                    .cell("attribute", EVENT_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Category", map.get(eventName).getCategory(),
                                    "AdditionalProperties",  MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            map.get(eventName).getParameters())
                            )))
                    .flush();
        }
        return builder.get();
    }

    /**
     * Initializes a new attribute.
     */
    SnampConfigurationAttribute() {
        super(NAME, SNAMP_CONFIGURATION_DATA);
    }

    /**
     * Parse AgentConfiguration to the CompositeData object
     * @param configuration - AgentConfiguration instance
     * @return CompositeData instance
     * @throws OpenDataException
     */
    private static CompositeData snampConfigurationToJMX(final AgentConfiguration configuration) throws OpenDataException {
        // adapter parsing
        final TabularDataBuilderRowFill builderAdapter = new TabularDataBuilderRowFill(ADAPTER_MAP_TYPE);
        final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapterMapConfig = configuration.getResourceAdapters();
        for (final String adapterName : adapterMapConfig.keySet()) {
            builderAdapter.newRow()
                    .cell("name", adapterName)
                    .cell("adapter", ADAPTER_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Name", adapterMapConfig.get(adapterName).getAdapterName(),
                                    "Parameters",  MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            adapterMapConfig.get(adapterName).getParameters())
                            )))
                    .flush();
        }

        // connector parsing
        final TabularDataBuilderRowFill builderConnector = new TabularDataBuilderRowFill(CONNECTOR_MAP_TYPE);
        final Map<String, AgentConfiguration.ManagedResourceConfiguration> connectors = configuration.getManagedResources();
        for (final String connectorName : connectors.keySet()) {
            builderConnector.newRow()
                    .cell("name", connectorName)
                    .cell("connector", CONNECTOR_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "ConnectionString", connectors.get(connectorName).getConnectionString(),
                                    "ConnectionType", connectors.get(connectorName).getConnectionType(),
                                    "Attributes", parseConnectorAttributes(connectors.get(connectorName).getElements(
                                            AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class)),
                                    "Events", parseConnectorEvents(connectors.get(connectorName).getElements(
                                            AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class)),
                                    "Parameters", MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            connectors.get(connectorName).getParameters())
                            )))
                    .flush();
        }
        return SNAMP_CONFIGURATION_DATA_BUILDER.build(
                ImmutableMap.of(
                        "ResourceAdapters", builderAdapter.get(),
                        "ManagedResources", builderConnector.get()
                )
        );
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
                                } else {
                                    config.setReadWriteTimeout(new TimeSpan(Long.MAX_VALUE));
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
            manager.load();
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
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef = new ServiceReferenceHolder<>(bundleContext, ConfigurationAdmin.class);
        try{
            final PersistentConfigurationManager manager = new PersistentConfigurationManager(adminRef);
            manager.load();
            ConfigurationDiffEngine.merge(JMXtoSnampConfiguration(data), manager.getCurrentConfiguration());
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
