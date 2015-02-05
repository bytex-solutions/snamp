package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.Box;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;

import javax.management.openmbean.*;
import java.util.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnampCoreMBeanUtils {
    static final CompositeType COMPONENT_CONFIG_SCHEMA;
    private static String MANAGED_RESOURCE_PARAMS = "managedResourceParameters";
    private static String RESOURCE_ADAPTER_PARAMS = "resourceAdapterParameters";
    private static final String ATTRIBUTE_PARAMS = "attributeParameters";
    private static final String EVENT_PARAMS = "eventParameters";

    static final CompositeType CONFIG_PARAMETER_DESCRIPTOR;
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String DESCRIPTION = "description";
    private static final String INPUT_PATTERN = "inputPattern";
    private static final String REQUIRED = "required";
    private static final String SUGGESTIONS_SUPPORTED = "suggestionSupported";
    private static final String ASSOCIATED = "associated";
    private static final String EXTENDS = "extends";
    private static final String EXCLUDES = "notCompatibleWith";

    static final TabularType CONFIG_ENTITY_SCHEMA;


    static{
        try {
            //CONFIG_PARAMETER_DESCRIPTOR
            CONFIG_PARAMETER_DESCRIPTOR = new CompositeType("com.itworks.management.ConfigParameter",
                    "Configuration parameter descriptor",
                    new String[]{
                            DEFAULT_VALUE,
                            DESCRIPTION,
                            INPUT_PATTERN,
                            REQUIRED,
                            SUGGESTIONS_SUPPORTED,
                            ASSOCIATED,
                            EXTENDS,
                            EXCLUDES
                    },
                    new String[]{
                            "The default value of the configuration parameter",
                            "The description of the configuration parameter",
                            "Regexp that can be used to validate configuration parameter value",
                            "Determines whether this configuration parameter should be specified in the configuration",
                            "Determines whether the SNAMP can suggest possible values of this configuration parameter",
                            "A collection of related configuration parameters",
                            "A collection of configuration parameters that may extends the effect f this configuration parameter",
                            "A collection of configuration parameters that cannot be combined with this configuration parameter"
                    },
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.STRING,
                            SimpleType.STRING,
                            SimpleType.BOOLEAN,
                            SimpleType.BOOLEAN,
                            new ArrayType<String[]>(SimpleType.STRING, false),
                            new ArrayType<String[]>(SimpleType.STRING, false),
                            new ArrayType<String[]>(SimpleType.STRING, false)
                    });
            //CONFIG_ENTITY_SCHEMA
            CONFIG_ENTITY_SCHEMA = new TabularType("com.itworks.management.ConfigEntitySchema",
                    "Configuration entity schema",
                    new CompositeType("com.itworks.management.ConfigEntitySchemaEntry",
                            "Configuration parameter description",
                            new String[]{"parameter", "description"},
                            new String[]{"Parameter name", "Parameter descriptor"},
                            new OpenType<?>[]{SimpleType.STRING, CONFIG_PARAMETER_DESCRIPTOR}),
                    new String[]{"parameter"}
                    );
            //COMPONENT_CONFIG_SCHEMA
            COMPONENT_CONFIG_SCHEMA = new CompositeType("com.itworks.management.ConnectorConfigSchema",
                    "SNAMP Connector Configuration Schema",
                    new String[]{
                            MANAGED_RESOURCE_PARAMS,
                            RESOURCE_ADAPTER_PARAMS,
                            ATTRIBUTE_PARAMS,
                            EVENT_PARAMS},
                    new String[]{
                            "A set of managed resource related parameters",
                            "A set of resource adapter related parameters",
                            "Attribute configuration parameters",
                            "Event configuration parameters"},
                    new OpenType<?>[]{
                            CONFIG_ENTITY_SCHEMA,
                            CONFIG_ENTITY_SCHEMA,
                            CONFIG_ENTITY_SCHEMA,
                            CONFIG_ENTITY_SCHEMA
                    }
            );
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private SnampCoreMBeanUtils(){

    }

    private static String getRelationshipKey(final ConfigurationEntityDescription.ParameterRelationship rel){
        switch (rel){
            case EXCLUSION: return EXCLUDES;
            case EXTENSION: return EXTENDS;
            default: return ASSOCIATED;
        }
    }

    private static TabularData getConfigurationSchema(final ConfigurationEntityDescription<?> description, final Locale loc) throws OpenDataException{
        final TabularDataSupport result = new TabularDataSupport(CONFIG_ENTITY_SCHEMA);
        if(description != null)
            for(final String parameterName: description){
                final Map<String, Object> parameter = new HashMap<>();
                final ConfigurationEntityDescription.ParameterDescription descriptor = description.getParameterDescriptor(parameterName);
                parameter.put(DEFAULT_VALUE, descriptor.getDefaultValue(loc));
                parameter.put(DESCRIPTION, descriptor.getDescription(loc));
                parameter.put(INPUT_PATTERN, descriptor.getValuePattern(loc));
                parameter.put(REQUIRED, descriptor.isRequired());
                parameter.put(SUGGESTIONS_SUPPORTED, description instanceof SelectableAdapterParameterDescriptor || description instanceof SelectableConnectorParameterDescriptor);
                parameter.put(EXCLUDES, new String[0]);
                parameter.put(EXTENDS, new String[0]);
                parameter.put(ASSOCIATED, new String[0]);
                //related params
                for(final ConfigurationEntityDescription.ParameterRelationship rel: ConfigurationEntityDescription.ParameterRelationship.values()){
                    final Set<String> relationship = new HashSet<>();
                    for(final String relatedParameter: descriptor.getRelatedParameters(rel))
                        relationship.add(relatedParameter);
                    parameter.put(getRelationshipKey(rel), ArrayUtils.toArray(relationship, String.class));
                }
                result.put(new CompositeDataSupport(result.getTabularType().getRowType(),
                        ImmutableMap.<String, Object>of(
                                "parameter", parameterName,
                                "description", new CompositeDataSupport(CONFIG_PARAMETER_DESCRIPTOR, parameter))));
            }
        return result;
    }

    private static CompositeData getConfigurationSchema(final ConfigurationEntityDescriptionProvider schemaProvider,
                                                        final Locale loc) throws OpenDataException {
        final Map<String, TabularData> schema = Maps.newHashMapWithExpectedSize(COMPONENT_CONFIG_SCHEMA.keySet().size());
        schema.put(MANAGED_RESOURCE_PARAMS, getConfigurationSchema(schemaProvider.getDescription(AgentConfiguration.ManagedResourceConfiguration.class), loc));
        schema.put(RESOURCE_ADAPTER_PARAMS, getConfigurationSchema(schemaProvider.getDescription(AgentConfiguration.ResourceAdapterConfiguration.class), loc));
        schema.put(ATTRIBUTE_PARAMS, getConfigurationSchema(schemaProvider.getDescription(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class), loc));
        schema.put(EVENT_PARAMS, getConfigurationSchema(schemaProvider.getDescription(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class), loc));
        return new CompositeDataSupport(COMPONENT_CONFIG_SCHEMA, schema);
    }

    private static CompositeData getConfigurationSchema(final SnampComponentDescriptor component,
                                                 final String locale) throws OpenDataException {
        final Box<CompositeData> result = new Box<>();
        component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, OpenDataException>() {
            @Override
            public void accept(final ConfigurationEntityDescriptionProvider input) throws OpenDataException {
                result.set(getConfigurationSchema(input, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
            }
        });
        return result.get();
    }

    static SnampComponentDescriptor getResourceConnector(final SnampManager snampManager,
                                                                 final String connectorName){
        for(final SnampComponentDescriptor connector: snampManager.getInstalledResourceConnectors())
            if(Objects.equals(connectorName, connector.get(SnampComponentDescriptor.CONNECTOR_SYSTEM_NAME_PROPERTY)))
                return connector;
        return null;
    }

    static CompositeData getConnectorConfigurationSchema(final SnampManager snampManager,
                                                         final String connectorName,
                                                         final String locale) throws OpenDataException{
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        return connector != null ? getConfigurationSchema(connector, locale) : null;
    }
}
