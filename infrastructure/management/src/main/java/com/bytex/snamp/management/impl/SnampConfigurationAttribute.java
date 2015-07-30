package com.bytex.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.bytex.snamp.ServiceReferenceHolder;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.PersistentConfigurationManager;
import com.bytex.snamp.configuration.SerializableAgentConfiguration;
import com.bytex.snamp.configuration.diff.ConfigurationDiffEngine;
import com.bytex.snamp.jmx.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static com.bytex.snamp.jmx.CompositeDataUtils.getString;
import static com.bytex.snamp.jmx.CompositeDataUtils.getLong;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class SnampConfigurationAttribute  extends OpenMBean.OpenAttribute<CompositeData, CompositeType> implements CommonOpenTypesSupport<MBeanAttributeInfo> {
    private static final long INFINITE_TIMEOUT = -1L;
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

            CONNECTOR_ATTRIBUTE_MAP_TYPE = new TabularTypeBuilder("com.bytex.management.ConnectorAttributeMapType", "Simple type for Map<String, EventMetadata>")
                    .addColumn("UserDefinedName", "User defined name for connector's attribute", SimpleType.STRING, true)
                    .addColumn("Attribute", "Attribute metadata instance", ATTRIBUTE_METADATA, false)
                    .build();
            CONNECTOR_EVENT_MAP_TYPE = new TabularTypeBuilder("com.bytex.management.ConnectorEventMapType", "Simple type for Map<String, AttributeMetadata>")
                    .addColumn("UserDefinedName", "User defined name for connector's event", SimpleType.STRING, true)
                    .addColumn("Event", "Event metadata instance", EVENT_METADATA, false)
                    .build();

            CONNECTOR_METADATA_BUILDER = new CompositeTypeBuilder("com.bytex.management.ConnectorMetadata", "SNAMP connector configuration metadata")
                    .addItem("ConnectionString", "Management target connection string", SimpleType.STRING)
                    .addItem("ConnectionType", "Type of the management connector that is used to organize monitoring data exchange between agent and the management provider", SimpleType.STRING)
                    .addItem("Attributes", "Attributes", CONNECTOR_ATTRIBUTE_MAP_TYPE)
                    .addItem("Events", "Events", CONNECTOR_EVENT_MAP_TYPE)
                    .addItem("Parameters", "User defined properties for connector", SIMPLE_MAP_TYPE);

            ADAPTER_METADATA_BUILDER = new CompositeTypeBuilder("com.bytex.management.AdapterMetadata", "SNAMP adapter configuration metadata")
                    .addItem("Name", "SNAMP adapter name", SimpleType.STRING)
                    .addItem("Parameters", "Additional properties for SNAMP adapter", SIMPLE_MAP_TYPE);

            CONNECTOR_METADATA = CONNECTOR_METADATA_BUILDER.build();
            ADAPTER_METADATA = ADAPTER_METADATA_BUILDER.build();

            ADAPTER_MAP_TYPE = new TabularTypeBuilder("com.bytex.management.AdapterMapType", "Simple type for Map<String, Adapter>")
                    .addColumn("UserDefinedName", "User defined name for adapter", SimpleType.STRING, true)
                    .addColumn("Adapter", "Adapter instance", ADAPTER_METADATA, false)
                    .build();
            CONNECTOR_MAP_TYPE = new TabularTypeBuilder("com.bytex.management.ConnectorMapType", "Simple type for Map<String, Connector>")
                    .addColumn("UserDefinedName", "User defined name for connector", SimpleType.STRING, true)
                    .addColumn("Connector", "Connector instance", CONNECTOR_METADATA, false)
                    .build();

            SNAMP_CONFIGURATION_DATA_BUILDER = new CompositeTypeBuilder("com.bytex.management.SnampConfiguration", "SNAMP main configuration metadata")
                    .addItem("ResourceAdapters", "SNAMP resource adapters configuration", ADAPTER_MAP_TYPE)
                    .addItem("ManagedResources", "SNAMP managed resources configuration", CONNECTOR_MAP_TYPE);

            SNAMP_CONFIGURATION_DATA = SNAMP_CONFIGURATION_DATA_BUILDER.build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static TimeSpan convertTimeout(final long timeout){
        return timeout == Long.MAX_VALUE || timeout <= INFINITE_TIMEOUT ?
                TimeSpan.INFINITE:
                new TimeSpan(timeout);
    }

    private static long convertTimeout(final TimeSpan timeout){
        if(timeout == TimeSpan.INFINITE)
            return INFINITE_TIMEOUT;
        final long result = timeout.toMillis();
        return result == Long.MAX_VALUE ? INFINITE_TIMEOUT : result;
    }

    private static TimeSpan convertTimeout(final CompositeData entry, final String key){
        return convertTimeout(getLong(entry, key, INFINITE_TIMEOUT));
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
        for (final Map.Entry<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attribute : map.entrySet()) {
            builder.newRow()
                    .cell("UserDefinedName", attribute.getKey())
                    .cell("Attribute", ATTRIBUTE_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Name", attribute.getValue().getAttributeName(),
                                    "ReadWriteTimeout", convertTimeout(attribute.getValue().getReadWriteTimeout()),
                                    "AdditionalProperties", MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            attribute.getValue().getParameters())
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
        for (final Map.Entry<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> event : map.entrySet()) {
            builder.newRow()
                    .cell("UserDefinedName", event.getKey())
                    .cell("Event", EVENT_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Category", event.getValue().getCategory(),
                                    "AdditionalProperties",  MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            event.getValue().getParameters())
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
        for (final Map.Entry<String, AgentConfiguration.ResourceAdapterConfiguration> adapter : adapterMapConfig.entrySet()) {
            builderAdapter.newRow()
                    .cell("UserDefinedName", adapter.getKey())
                    .cell("Adapter", ADAPTER_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "Name", adapter.getValue().getAdapterName(),
                                    "Parameters",  MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            adapter.getValue().getParameters())
                            )))
                    .flush();
        }

        // connector parsing
        final TabularDataBuilderRowFill builderConnector = new TabularDataBuilderRowFill(CONNECTOR_MAP_TYPE);
        final Map<String, AgentConfiguration.ManagedResourceConfiguration> connectors = configuration.getManagedResources();
        for (final Map.Entry<String, AgentConfiguration.ManagedResourceConfiguration> connector : connectors.entrySet()) {
            builderConnector.newRow()
                    .cell("UserDefinedName", connector.getKey())
                    .cell("Connector", CONNECTOR_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "ConnectionString", connector.getValue().getConnectionString(),
                                    "ConnectionType", connector.getValue().getConnectionType(),
                                    "Attributes", parseConnectorAttributes(connector.getValue().getElements(
                                            AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class)),
                                    "Events", parseConnectorEvents(connector.getValue().getElements(
                                            AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class)),
                                    "Parameters", MonitoringUtils.transformAdditionalPropertiesToTabularData(
                                            connector.getValue().getParameters())
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
                if (adapterDataCurrent.containsKey("Adapter")) {
                    final CompositeData adapterInstance = ((CompositeData) adapterDataCurrent.get("Adapter"));
                    agentConfiguration.setAdapterName(getString(adapterInstance, "Name", ""));
                    if (adapterInstance.containsKey("Parameters") && adapterInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) adapterInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            agentConfiguration.setParameter(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                        }
                    }
                }
                configuration.getResourceAdapters().put(getString(adapterDataCurrent, "UserDefinedName", ""), agentConfiguration);
            }
        }
        // parse connectors
        if (data.containsKey("ManagedResources") && data.get("ManagedResources") instanceof TabularData) {
            final TabularData connectorsData = (TabularData) data.get("ManagedResources");
            for (final CompositeData connectorDataCurrent : (Collection<CompositeData>) connectorsData.values()) {
                final SerializableAgentConfiguration.SerializableManagedResourceConfiguration connectorConfiguration =
                        new SerializableAgentConfiguration.SerializableManagedResourceConfiguration();

                if (connectorDataCurrent.containsKey("Connector")) {
                    final CompositeData connectorInstance = (CompositeData) connectorDataCurrent.get("Connector");
                        connectorConfiguration.setConnectionString(getString(connectorInstance, "ConnectionString", ""));

                        connectorConfiguration.setConnectionType(getString(connectorInstance, "ConnectionType", ""));

                    // attributes parsing
                    if (connectorInstance.containsKey("Attributes") && connectorInstance.get("Attributes") instanceof TabularData) {
                        final TabularData attributes = (TabularData) connectorInstance.get("Attributes");
                        final Map<String, SerializableAgentConfiguration.SerializableManagedResourceConfiguration.AttributeConfiguration>
                                attributesConfigMap = Maps.newHashMap();
                        for (final CompositeData attributeInstance : (Collection<CompositeData>) attributes.values()) {
                            AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration config = connectorConfiguration.newAttributeConfiguration();
                            if (attributeInstance.containsKey("Attribute") && attributeInstance.get("Attribute") instanceof CompositeData) {

                                final CompositeData attributeData = (CompositeData) attributeInstance.get("Attribute");
                                if (attributeData.containsKey("Name")) {
                                    config.setAttributeName(getString(attributeData, "Name", ""));
                                }
                                if (attributeData.containsKey("ReadWriteTimeout"))
                                    config.setReadWriteTimeout(convertTimeout(attributeData, "ReadWriteTimeout"));

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                                    }
                                }
                            }
                            attributesConfigMap.put(getString(attributeInstance, "UserDefinedName", ""), config);
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
                            if (eventInstance.containsKey("Event") && eventInstance.get("Event") instanceof CompositeData) {
                                final CompositeData attributeData = (CompositeData) eventInstance.get("Event");
                                config.setCategory(getString(attributeData, "Category", ""));

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                                    }
                                }
                            }
                            eventsConfigMap.put(getString(eventInstance, "UserDefinedName", ""), config);
                        }
                        connectorConfiguration.setEvents(eventsConfigMap);
                    }

                    if (connectorInstance.containsKey("Parameters") && connectorInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) connectorInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            connectorConfiguration.setParameter(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                        }
                    }
                }

                configuration.getManagedResources().put(getString(connectorDataCurrent, "UserDefinedName", ""), connectorConfiguration);
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
            else return snampConfigurationToJMX(configuration);
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
