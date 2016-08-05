package com.bytex.snamp.management.jmx;

import com.bytex.snamp.Box;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.diff.ConfigurationDiffEngine;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.*;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.*;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.jmx.CompositeDataUtils.getLong;
import static com.bytex.snamp.jmx.CompositeDataUtils.getString;

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
                    .addColumn("Name", "User defined name for connector's attribute", SimpleType.STRING, true)
                    .addColumn("Attribute", "Attribute metadata instance", ATTRIBUTE_METADATA, false)
                    .build();
            CONNECTOR_EVENT_MAP_TYPE = new TabularTypeBuilder("com.bytex.management.ConnectorEventMapType", "Simple type for Map<String, AttributeMetadata>")
                    .addColumn("Category", "User defined name for connector's event", SimpleType.STRING, true)
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

    private static Duration convertTimeout(final long timeout){
        return timeout == Long.MAX_VALUE || timeout <= INFINITE_TIMEOUT ?
                null:
                Duration.ofMillis(timeout);
    }

    private static long convertTimeout(final Duration timeout){
        if(timeout == null)
            return INFINITE_TIMEOUT;
        final long result = timeout.toMillis();
        return result == Long.MAX_VALUE ? INFINITE_TIMEOUT : result;
    }

    private static Duration convertTimeout(final CompositeData entry, final String key){
        return convertTimeout(getLong(entry, key, INFINITE_TIMEOUT));
    }

    /**
     * Parse connector attributes to the TabularData object
     * @param map - the configuration entity to parse
     * @return TabularData object
     * @throws OpenDataException
     */
    private static TabularData parseConnectorAttributes(final Map<String, ? extends ManagedResourceConfiguration.AttributeConfiguration> map)
            throws OpenDataException {
        final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(CONNECTOR_ATTRIBUTE_MAP_TYPE);
        for (final Map.Entry<String, ? extends ManagedResourceConfiguration.AttributeConfiguration> attribute : map.entrySet()) {
            builder.newRow()
                    .cell("Name", attribute.getKey())
                    .cell("Attribute", ATTRIBUTE_METADATA_BUILDER.build(
                            ImmutableMap.of(
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
    private static TabularData parseConnectorEvents(final Map<String, ? extends ManagedResourceConfiguration.EventConfiguration> map)
            throws OpenDataException {
        final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(CONNECTOR_EVENT_MAP_TYPE);
        for (final Map.Entry<String, ? extends ManagedResourceConfiguration.EventConfiguration> event : map.entrySet()) {
            builder.newRow()
                    .cell("Category", event.getKey())
                    .cell("Event", EVENT_METADATA_BUILDER.build(
                            ImmutableMap.of(
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
        final Map<String, ? extends ResourceAdapterConfiguration> adapterMapConfig = configuration.getEntities(ResourceAdapterConfiguration.class);
        for (final Map.Entry<String, ? extends ResourceAdapterConfiguration> adapter : adapterMapConfig.entrySet()) {
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
        final Map<String, ? extends ManagedResourceConfiguration> connectors = configuration.getEntities(ManagedResourceConfiguration.class);
        for (final Map.Entry<String, ? extends ManagedResourceConfiguration> connector : connectors.entrySet()) {
            builderConnector.newRow()
                    .cell("UserDefinedName", connector.getKey())
                    .cell("Connector", CONNECTOR_METADATA_BUILDER.build(
                            ImmutableMap.of(
                                    "ConnectionString", connector.getValue().getConnectionString(),
                                    "ConnectionType", connector.getValue().getConnectionType(),
                                    "Attributes", parseConnectorAttributes(connector.getValue().getFeatures(AttributeConfiguration.class)),
                                    "Events", parseConnectorEvents(connector.getValue().getFeatures(EventConfiguration.class)),
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
    private static void JMXtoSnampConfiguration(final CompositeData input, final AgentConfiguration output) {
        // parse adapters
        if (input.containsKey("ResourceAdapters") && input.get("ResourceAdapters") instanceof TabularData) {
            final TabularData adaptersData = (TabularData) input.get("ResourceAdapters");
            for (final CompositeData adapterDataCurrent : (Collection<CompositeData>) adaptersData.values()) {
                final ResourceAdapterConfiguration adapterConfig = output
                        .getEntities(ResourceAdapterConfiguration.class)
                        .getOrAdd(getString(adapterDataCurrent, "UserDefinedName", ""));
                if (adapterDataCurrent.containsKey("Adapter")) {
                    final CompositeData adapterInstance = ((CompositeData) adapterDataCurrent.get("Adapter"));
                    adapterConfig.setAdapterName(getString(adapterInstance, "Name", ""));
                    if (adapterInstance.containsKey("Parameters") && adapterInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) adapterInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            adapterConfig.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                        }
                    }
                }
            }
        }
        // parse connectors
        if (input.containsKey("ManagedResources") && input.get("ManagedResources") instanceof TabularData) {
            final TabularData connectorsData = (TabularData) input.get("ManagedResources");
            for (final CompositeData connectorDataCurrent : (Collection<CompositeData>) connectorsData.values()) {
                final ManagedResourceConfiguration connectorConfiguration = output
                        .getEntities(ManagedResourceConfiguration.class)
                        .getOrAdd(getString(connectorDataCurrent, "UserDefinedName", ""));

                if (connectorDataCurrent.containsKey("Connector")) {
                    final CompositeData connectorInstance = (CompositeData) connectorDataCurrent.get("Connector");
                        connectorConfiguration.setConnectionString(getString(connectorInstance, "ConnectionString", ""));

                        connectorConfiguration.setConnectionType(getString(connectorInstance, "ConnectionType", ""));

                    // attributes parsing
                    if (connectorInstance.containsKey("Attributes") && connectorInstance.get("Attributes") instanceof TabularData) {
                        final TabularData attributes = (TabularData) connectorInstance.get("Attributes");
                        for (final CompositeData attributeInstance : (Collection<CompositeData>) attributes.values()) {
                            AttributeConfiguration config = connectorConfiguration
                                    .getFeatures(AttributeConfiguration.class)
                                    .getOrAdd(getString(attributeInstance, "Name", ""));
                            if (attributeInstance.containsKey("Attribute") && attributeInstance.get("Attribute") instanceof CompositeData) {

                                final CompositeData attributeData = (CompositeData) attributeInstance.get("Attribute");
                                if (attributeData.containsKey("ReadWriteTimeout"))
                                    config.setReadWriteTimeout(convertTimeout(attributeData, "ReadWriteTimeout"));

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                                    }
                                }
                            }
                        }
                    }

                    // events parsing
                    if (connectorInstance.containsKey("Events") && connectorInstance.get("Events") instanceof TabularData) {
                        final TabularData events = (TabularData) connectorInstance.get("Events");
                        for (final CompositeData eventInstance : (Collection<CompositeData>) events.values()) {
                            EventConfiguration config = connectorConfiguration
                                    .getFeatures(EventConfiguration.class)
                                    .getOrAdd(getString(eventInstance, "Category", ""));
                            if (eventInstance.containsKey("Event") && eventInstance.get("Event") instanceof CompositeData) {
                                final CompositeData attributeData = (CompositeData) eventInstance.get("Event");

                                if (attributeData.containsKey("AdditionalProperties") && attributeData.get("AdditionalProperties") instanceof TabularData) {
                                    final TabularData params = (TabularData) attributeData.get("AdditionalProperties");
                                    for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                                        config.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                                    }
                                }
                            }
                        }
                    }

                    if (connectorInstance.containsKey("Parameters") && connectorInstance.get("Parameters") instanceof TabularData) {
                        final TabularData params = (TabularData) connectorInstance.get("Parameters");
                        for (final CompositeData keyParam : (Collection<CompositeData>) params.values()) {
                            connectorConfiguration.getParameters().put(getString(keyParam, "Key", ""), getString(keyParam, "Value", ""));
                        }
                    }
                }
            }
        }
    }

    @Override
    public CompositeData getValue() throws IOException, ConfigurationException, OpenDataException {
        final BundleContext bundleContext = getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> adminRef =
                ServiceHolder.tryCreate(bundleContext, ConfigurationManager.class);
        if(adminRef != null)
            try{
                final Box<CompositeData> result = new Box<>();
                adminRef.get().readConfiguration(configuration -> result.set(snampConfigurationToJMX(configuration)));
                return result.get();
            }
            finally {
                adminRef.release(bundleContext);
            }
        else throw new IOException("Configuration storage is not available");
    }

    @Override
    public void setValue(final CompositeData data) throws IOException {
        if (data == null || data.values().size() == 0)
            throw new IllegalArgumentException("No valid input data received");

        final BundleContext bundleContext = getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(bundleContext, ConfigurationManager.class);
        if (adminRef != null)
            try {
                adminRef.get().processConfiguration(config -> {
                    final AgentConfiguration clonedConfig = config.clone();
                    clonedConfig.clear();
                    JMXtoSnampConfiguration(data, clonedConfig);
                    ConfigurationDiffEngine.merge(clonedConfig, config);
                    return true;
                });
            } finally {
                adminRef.release(bundleContext);
            }
        else throw new IOException("Configuration storage is not available");
    }

    @Override
    protected String getDescription() {
        return "Main SNAMP Configuration";
    }
}
