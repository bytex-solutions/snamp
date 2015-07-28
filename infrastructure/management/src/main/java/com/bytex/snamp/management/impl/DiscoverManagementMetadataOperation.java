package com.bytex.snamp.management.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class DiscoverManagementMetadataOperation extends OpenMBean.OpenOperation<CompositeData, CompositeType> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final OpenMBeanParameterInfo CONNECTION_STRING = new OpenMBeanParameterInfoSupport(
            "connectionString",
            "Connection string for SNAMP connector",
            SimpleType.STRING);

    private static final OpenMBeanParameterInfo CONNECTION_STRING_PARAM = new OpenMBeanParameterInfoSupport(
            "connectionStringData",
            "Additional parameters for filtering suggested values",
            SIMPLE_MAP_TYPE
    );

    private static final String NAME = "discoverManagementMetadata";

    private final AbstractSnampManager snampManager;

    private static final CompositeType CONNECTOR_METADATA;
    private static final CompositeType EVENT_METADATA;
    private static final CompositeType ATTRIBUTE_METADATA;

    private static final CompositeTypeBuilder CONNECTOR_METADATA_BUILDER;

    static{
        try {

            EVENT_METADATA = EVENT_METADATA_BUILDER.build();
            ATTRIBUTE_METADATA = ATTRIBUTE_METADATA_BUILDER.build();

            CONNECTOR_METADATA_BUILDER = new CompositeTypeBuilder("com.bytex.management.ConnectorMetadata", "SNAMP connector discovery metadata")
                    .addItem("Attributes", "SNAMP connector attributes", ArrayType.getArrayType(ATTRIBUTE_METADATA))
                    .addItem("Events", "SNAMP connector events", ArrayType.getArrayType(EVENT_METADATA));

            CONNECTOR_METADATA = CONNECTOR_METADATA_BUILDER.build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    /**
     * Instantiates a new Discover management metadata operation.
     *
     * @param snampManager the snamp manager
     */
    DiscoverManagementMetadataOperation(final AbstractSnampManager snampManager) {
        super(NAME, CONNECTOR_METADATA, CONNECTOR_NAME_PARAM, CONNECTION_STRING, CONNECTION_STRING_PARAM, LOCALE_PARAM);
        this.snampManager = snampManager;
    }

    private static CompositeData getDiscoverMetadata(final SnampComponentDescriptor connector, final Locale loc,
           final String connectionString, final Map<String, String> connectionOptions) throws Exception {

        final Map<String, Object> schema = Maps.newHashMapWithExpectedSize(CONNECTOR_METADATA.keySet().size());
        connector.invokeSupportService(DiscoveryService.class, new Consumer<DiscoveryService, Exception>() {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(final DiscoveryService input) throws Exception {
                final DiscoveryService.DiscoveryResult metadata = input.discover(connectionString, connectionOptions,
                        AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class,
                        AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
                // Attributes
                final Collection<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes =
                        metadata.getSubResult(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
                final List<CompositeData> attributesData = Lists.newArrayListWithExpectedSize(attributes.size());
                for (AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute: attributes) {
                    final Map<String, Object> attrMap = new HashMap<>();
                    // append the name
                    attrMap.put("Name", attribute.getAttributeName());
                    // append the r/w timeout
                    if (attribute.getReadWriteTimeout() != TimeSpan.INFINITE)
                        attrMap.put("ReadWriteTimeout", attribute.getReadWriteTimeout().convert(TimeUnit.MILLISECONDS).duration);
                    else {
                        attrMap.put("ReadWriteTimeout", Long.MAX_VALUE);
                    }
                    //read other properties
                    final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(SIMPLE_MAP_TYPE);
                    for (final Map.Entry<String, String> parameter : attribute.getParameters().entrySet()) {
                        builder.newRow()
                                .cell("Key", parameter.getKey())
                                .cell("Value", parameter.getValue())
                                .flush();
                    }
                    // append additional properties
                    attrMap.put("AdditionalProperties", builder.get());
                    attributesData.add(ATTRIBUTE_METADATA_BUILDER.build(attrMap));
                }
                // Events
                final Collection<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events =
                        metadata.getSubResult(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
                final List<CompositeData> eventsData = Lists.newArrayListWithExpectedSize(events.size());
                for (AgentConfiguration.ManagedResourceConfiguration.EventConfiguration event: events) {
                    final Map<String, Object> eventMap = new HashMap<>();
                    // append the category
                    eventMap.put("Category", event.getCategory());
                    //read other properties
                    final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(SIMPLE_MAP_TYPE);
                    for (final Map.Entry<String, String> parameter : event.getParameters().entrySet()) {
                        builder.newRow()
                                .cell("Key", parameter.getKey())
                                .cell("Value", parameter.getValue())
                                .flush();
                    }
                    // append additional properties
                    eventMap.put("AdditionalProperties", builder.get());
                    eventsData.add(EVENT_METADATA_BUILDER.build(eventMap));
                }
                schema.put("Attributes", attributesData.toArray(new CompositeData[attributesData.size()]));
                schema.put("Events", eventsData.toArray(new CompositeData[eventsData.size()]));
            }
        });
        return CONNECTOR_METADATA_BUILDER.build(schema);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments);
        final String connectionString = getArgument(CONNECTION_STRING.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> connectionStringParam =
                MonitoringUtils.transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));
        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);

        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getDiscoverMetadata(connector,
                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale),
                connectionString, connectionStringParam);
    }
}