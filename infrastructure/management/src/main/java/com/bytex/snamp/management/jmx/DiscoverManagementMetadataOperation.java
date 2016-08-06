package com.bytex.snamp.management.jmx;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.discovery.DiscoveryService;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.*;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class DiscoverManagementMetadataOperation extends OpenMBean.OpenOperation<CompositeData, CompositeType> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final TypedParameterInfo<String> CONNECTION_STRING = new TypedParameterInfo<>(
            "connectionString",
            "Connection string for SNAMP connector",
            SimpleType.STRING,
            false);

    private static final TypedParameterInfo<TabularData> CONNECTION_STRING_PARAM = new TypedParameterInfo<>(
            "connectionStringData",
            "Additional parameters for filtering suggested values",
            SIMPLE_MAP_TYPE,
            false
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

    private static CompositeData getDiscoverMetadata(final SnampComponentDescriptor connector,
                                                     final String connectionString, final Map<String, String> connectionOptions) throws Exception {

        final Map<String, Object> schema = Maps.newHashMapWithExpectedSize(CONNECTOR_METADATA.keySet().size());
        connector.invokeSupportService(DiscoveryService.class, input -> {
            @SuppressWarnings("unchecked")
            final DiscoveryService.DiscoveryResult metadata = input.discover(connectionString, connectionOptions,
                    AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class,
                    AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
            // Attributes
            final Collection<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes =
                    metadata.getSubResult(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
            final List<CompositeData> attributesData = Lists.newArrayListWithExpectedSize(attributes.size());
            for (AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute: attributes) {
                final Map<String, Object> attrMap = new HashMap<>();
                // append the r/w timeout
                if (attribute.getReadWriteTimeout() != null)
                    attrMap.put("ReadWriteTimeout", attribute.getReadWriteTimeout(ChronoUnit.MILLIS));
                else {
                    attrMap.put("ReadWriteTimeout", -1L);
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
        });
        return CONNECTOR_METADATA_BUILDER.build(schema);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws Exception {
        final String connectorName = CONNECTOR_NAME_PARAM.getArgument(arguments);
        final String connectionString = CONNECTION_STRING.getArgument(arguments);
        final Map<String, String> connectionStringParam =
                MonitoringUtils.transformTabularDataToMap(CONNECTION_STRING_PARAM.getArgument(arguments));
        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);

        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getDiscoverMetadata(connector,
                connectionString, connectionStringParam);
    }
}
