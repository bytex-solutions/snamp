package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class DiscoverManagementMetadataOperation extends OpenMBean.OpenOperation<CompositeData, CompositeType> {

    private static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    private static final OpenMBeanParameterInfo CONNECTION_STRING = new OpenMBeanParameterInfoSupport(
            "connectionString",
            "Connection string for SNAMP connector",
            SimpleType.STRING);

    private static final String NAME = "discoverManagementMetadata";

    private final AbstractSnampManager snampManager;

    private static final OpenMBeanParameterInfo CONNECTOR_NAME = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "Snamp connector name",
            SimpleType.STRING);

    private static final CompositeType CONNECTOR_METADATA;
    private static final CompositeType EVENT_METADATA;
    private static final CompositeType ATTRIBUTE_METADATA;
    private static final TabularType USER_DEFINED_ATTRIBUTE_SCHEMA;
    private static final TabularType USER_DEFINED_EVENT_SCHEMA;
    private static final TabularType CONNECTION_PARAMS_SCHEMA;

    private static Map<String, String> transformTabularDataToMap(final TabularData data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> result = new HashMap<>();
            for (Object value : data.values()) {
                if (!(value instanceof CompositeData)) continue;
                final CompositeData cd = (CompositeData) value;
                result.put((String) cd.get("key"), (String) cd.get("value"));
            }
            return result;
        }
    }

    static{
        try {
            CONNECTION_PARAMS_SCHEMA = new TabularType("com.itworks.management.ConnectionParams",
                    "Configuration entity schema",
                    new CompositeType("com.itworks.management.ConnectionParam",
                            "Additional parameters for filtering suggested values",
                            new String[]{"key", "value"},
                            new String[]{"Parameter key", "Parameter value"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"key"}
            );


            USER_DEFINED_ATTRIBUTE_SCHEMA = new TabularType("com.itworks.management.UserDefinedAttributeSchema",
                    "User defined properties for connector attribute",
                    new CompositeType("com.itworks.management.UserDefinedAttributeParams",
                            "User defined properties for connector attribute key value holder",
                            new String[]{"Attribute", "Description"},
                            new String[]{"Attribute name", "Attribute short description"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"Attribute"}
            );
            USER_DEFINED_EVENT_SCHEMA = new TabularType("com.itworks.management.UserDefinedEventSchema",
                    "User defined properties for connector event",
                    new CompositeType("com.itworks.management.UserDefinedEventParams",
                            "User defined properties for connector event key value holder",
                            new String[]{"Event", "Description"},
                            new String[]{"Event name", "Event short description"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"Event"}
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
                            USER_DEFINED_EVENT_SCHEMA
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
                            "User defined property for attribute"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.LONG,
                            USER_DEFINED_ATTRIBUTE_SCHEMA
                    }
            );

            //COMPONENT_CONFIG_SCHEMA
            CONNECTOR_METADATA = new CompositeType("com.itworks.management.ConnectorMetadata",
                    "SNAMP connector discovery metadata",
                    new String[]{
                            "Attributes",
                            "Events"
                    },
                    new String[]{
                            "SNAMP connector attributes",
                            "SNAMP connector events"},
                    new OpenType<?>[]{
                            ArrayType.getArrayType(ATTRIBUTE_METADATA),
                            ArrayType.getArrayType(EVENT_METADATA)
                    }
            );
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private static final OpenMBeanParameterInfo CONNECTION_STRING_PARAM = new OpenMBeanParameterInfoSupport(
            "connectionStringData",
            "Additional parameters for filtering suggested values",
            CONNECTION_PARAMS_SCHEMA
    );


    /**
     * Instantiates a new Discover management metadata operation.
     *
     * @param snampManager the snamp manager
     */
    DiscoverManagementMetadataOperation(final AbstractSnampManager snampManager) {
        super(NAME, CONNECTOR_METADATA, CONNECTOR_NAME, CONNECTION_STRING, CONNECTION_STRING_PARAM, LOCALE_PARAM);
        this.snampManager = snampManager;
    }

    private static CompositeData getDiscoverMetadata(final SnampComponentDescriptor connector, final Locale loc,
           final String connectionString, final Map<String, String> connectionOptions) throws Exception {

        final Map<String, Object> schema = Maps.newHashMapWithExpectedSize(CONNECTOR_METADATA.keySet().size());
        connector.invokeSupportService(DiscoveryService.class, new Consumer<DiscoveryService, Exception>() {
            @SuppressWarnings("unchecked")
            @Override
            public void accept(final DiscoveryService input) throws Exception {
                final DiscoveryService.DiscoveryResult metadata = input.discover(
                        connectionString,
                        connectionOptions,
                        AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class,
                        AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);

                // Attributes
                final Collection<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes =
                        metadata.getSubResult(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
                final List<CompositeDataSupport> attributesData = new ArrayList<>(attributes.size());
                for (AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute: attributes) {
                    final Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("Name", attribute.getAttributeName());
                    if(attribute.getReadWriteTimeout() != TimeSpan.INFINITE)
                        attrMap.put("ReadWriteTimeout", attribute.getReadWriteTimeout().convert(TimeUnit.MILLISECONDS).duration);
                    //read other properties
                    if (!attribute.getParameters().keySet().isEmpty())
                    {
                        final TabularDataSupport tabularDataSupport = new TabularDataSupport(USER_DEFINED_ATTRIBUTE_SCHEMA);
                        for (final String parameter : attribute.getParameters().keySet()) {
                            tabularDataSupport.put(new CompositeDataSupport(tabularDataSupport.getTabularType().getRowType(),
                                    ImmutableMap.<String, Object>of(
                                            "Attribute", parameter,
                                            "Description", attribute.getParameters().get(parameter))));
                        }

                        attrMap.put("AdditionalProperties", tabularDataSupport);
                    }
                    attributesData.add(new CompositeDataSupport(ATTRIBUTE_METADATA, attrMap));
                }

                // Events
                final Collection<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events =
                        metadata.getSubResult(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
                final List<CompositeDataSupport> eventsData = new ArrayList<>(events.size());
                for (AgentConfiguration.ManagedResourceConfiguration.EventConfiguration event: events) {
                    final Map<String, Object> eventMap = new HashMap<>();
                    eventMap.put("Category", event.getCategory());
                    if (!event.getParameters().keySet().isEmpty())
                    {
                        final TabularDataSupport tabularDataSupport = new TabularDataSupport(USER_DEFINED_EVENT_SCHEMA);

                        for (final String parameter : event.getParameters().keySet()) {
                            tabularDataSupport.put(new CompositeDataSupport(tabularDataSupport.getTabularType().getRowType(),
                                    ImmutableMap.<String, Object>of(
                                            "Event", parameter,
                                            "Description", event.getParameters().get(parameter))));
                        }
                        eventMap.put("AdditionalProperties", tabularDataSupport);
                    }
                    eventsData.add(new CompositeDataSupport(ATTRIBUTE_METADATA, eventMap));
                }
                schema.put("Attributes", attributesData.toArray(new CompositeData[attributesData.size()]));
                schema.put("Events", eventsData.toArray(new CompositeData[eventsData.size()]));
            }
        });
        return new CompositeDataSupport(CONNECTOR_METADATA, schema);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME.getName(), String.class, arguments);
        final String connectionString = getArgument(CONNECTION_STRING.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> connectionStringParam =
                transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));
        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);

        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getDiscoverMetadata(connector,
                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale),
                connectionString, connectionStringParam);
    }
}
