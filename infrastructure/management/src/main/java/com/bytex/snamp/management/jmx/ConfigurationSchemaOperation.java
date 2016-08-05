package com.bytex.snamp.management.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Box;
import com.bytex.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;
import com.google.common.collect.ImmutableMap;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.*;
import java.util.*;

/**
 * The type Configuration schema operation.
 * @author Roman Sakno
 * @author Evgeniy Kirichenko
 * @version 1.2
 * @since 1.0
 */
abstract class ConfigurationSchemaOperation extends OpenMBean.OpenOperation<CompositeData, CompositeType> implements CommonOpenTypesSupport<MBeanOperationInfo> {
    private static final CompositeType COMPONENT_CONFIG_SCHEMA;
    private static final String MANAGED_RESOURCE_PARAMS = "ManagedResourceParameters";
    private static final String RESOURCE_ADAPTER_PARAMS = "ResourceAdapterParameters";
    private static final String ATTRIBUTE_PARAMS = "AttributeParameters";
    private static final String EVENT_PARAMS = "EventParameters";

    private static final CompositeType CONFIG_PARAMETER_DESCRIPTOR;
    private static final String DEFAULT_VALUE = "DefaultValue";
    private static final String DESCRIPTION = "Description";
    private static final String INPUT_PATTERN = "InputPattern";
    private static final String REQUIRED = "Required";
    private static final String SUGGESTIONS_SUPPORTED = "SuggestionSupported";
    private static final String ASSOCIATED = "Associated";
    private static final String EXTENDS = "Extends";
    private static final String EXCLUDES = "NotCompatibleWith";

    private static final TabularType CONFIG_ENTITY_SCHEMA;

    // Builders (helper fields).
    private static final CompositeTypeBuilder CONFIG_PARAM_BUILDER;
    private static final TabularTypeBuilder CONFIG_ENTITY_SCHEMA_BUILDER;
    private static final CompositeTypeBuilder COMPONENT_CONFIG_SCHEMA_BUILDER;

    static{
        try {
            //CONFIG_PARAMETER_DESCRIPTOR
            CONFIG_PARAM_BUILDER = new CompositeTypeBuilder("com.bytex.management.ConfigParameter", "Configuration parameter descriptor")
                    .addItem(DEFAULT_VALUE, "The default value of the configuration parameter", SimpleType.STRING)
                    .addItem(DESCRIPTION, "The description of the configuration parameter", SimpleType.STRING)
                    .addItem(INPUT_PATTERN, "Regexp that can be used to validate configuration parameter value", SimpleType.STRING)
                    .addItem(REQUIRED, "Determines whether this configuration parameter should be specified in the configuration", SimpleType.BOOLEAN)
                    .addItem(SUGGESTIONS_SUPPORTED, "Determines whether the SNAMP can suggest possible values of this configuration parameter", SimpleType.BOOLEAN)
                    .addItem(ASSOCIATED, "A collection of related configuration parameters", ArrayType.getArrayType(SimpleType.STRING))
                    .addItem(EXTENDS, "A collection of configuration parameters that may extends the effect for this configuration parameter", ArrayType.getArrayType(SimpleType.STRING))
                    .addItem(EXCLUDES, "A collection of configuration parameters that cannot be combined with this configuration parameter", ArrayType.getArrayType(SimpleType.STRING));

            CONFIG_PARAMETER_DESCRIPTOR = CONFIG_PARAM_BUILDER.build();


            //CONFIG_ENTITY_SCHEMA
            CONFIG_ENTITY_SCHEMA_BUILDER = new TabularTypeBuilder("com.bytex.management.ConfigEntitySchema", "Configuration entity schema")
                    .addColumn("Parameter", "Parameter name", SimpleType.STRING, true)
                    .addColumn("Description", "Parameter descriptor", CONFIG_PARAMETER_DESCRIPTOR, false);

            CONFIG_ENTITY_SCHEMA = CONFIG_ENTITY_SCHEMA_BUILDER.build();


            //COMPONENT_CONFIG_SCHEMA
            COMPONENT_CONFIG_SCHEMA_BUILDER = new CompositeTypeBuilder("com.bytex.management.ConnectorConfigSchema", "SNAMP Connector Configuration Schema")
                    .addItem(MANAGED_RESOURCE_PARAMS, "A set of managed resource related parameters", CONFIG_ENTITY_SCHEMA)
                    .addItem(RESOURCE_ADAPTER_PARAMS, "A set of resource adapter related parameters", CONFIG_ENTITY_SCHEMA)
                    .addItem(ATTRIBUTE_PARAMS, "Attribute configuration parameters", CONFIG_ENTITY_SCHEMA)
                    .addItem(EVENT_PARAMS, "Event configuration parameters", CONFIG_ENTITY_SCHEMA);

            COMPONENT_CONFIG_SCHEMA = COMPONENT_CONFIG_SCHEMA_BUILDER.build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The Snamp manager.
     */
    protected final AbstractSnampManager snampManager;

    /**
     * Instantiates a new Configuration schema operation.
     *
     * @param snampManager the snamp manager
     * @param operationName the operation name
     * @param parameters the parameters
     */
    protected ConfigurationSchemaOperation(final AbstractSnampManager snampManager,
                                           final String operationName,
                                           final TypedParameterInfo<?>... parameters) {
        super(operationName, COMPONENT_CONFIG_SCHEMA, parameters);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    private static String getRelationshipKey(final ConfigurationEntityDescription.ParameterRelationship rel){
        switch (rel){
            case EXCLUSION: return EXCLUDES;
            case EXTENSION: return EXTENDS;
            default: return ASSOCIATED;
        }
    }

    private static TabularData getConfigurationSchema(final ConfigurationEntityDescription<?> description, final Locale loc) throws OpenDataException{
        final TabularDataBuilderRowFill builder = new TabularDataBuilderRowFill(CONFIG_ENTITY_SCHEMA);
        if(description != null)
            for(final String parameterName: description){
                final Map<String, Object> parameter = new HashMap<>();
                final ConfigurationEntityDescription.ParameterDescription descriptor = description.getParameterDescriptor(parameterName);
                parameter.put(DEFAULT_VALUE, descriptor.getDefaultValue(loc));
                parameter.put(DESCRIPTION, descriptor.toString(loc));
                parameter.put(INPUT_PATTERN, descriptor.getValuePattern(loc));
                parameter.put(REQUIRED, descriptor.isRequired());
                parameter.put(SUGGESTIONS_SUPPORTED, description instanceof SelectableAdapterParameterDescriptor || description instanceof SelectableConnectorParameterDescriptor);
                parameter.put(EXCLUDES, ArrayUtils.emptyArray(String[].class));
                parameter.put(EXTENDS, ArrayUtils.emptyArray(String[].class));
                parameter.put(ASSOCIATED, ArrayUtils.emptyArray(String[].class));
                //related params
                for(final ConfigurationEntityDescription.ParameterRelationship rel: ConfigurationEntityDescription.ParameterRelationship.values()){
                    final Set<String> relationship = new HashSet<>();
                    relationship.addAll(descriptor.getRelatedParameters(rel));
                    parameter.put(getRelationshipKey(rel), relationship.stream().toArray(String[]::new));
                }
                builder.newRow()
                        .cell("Parameter", parameterName)
                        .cell("Description", CONFIG_PARAM_BUILDER.build(parameter))
                        .flush();
            }
        return builder.get();
    }

    private static CompositeData getConfigurationSchema(final ConfigurationEntityDescriptionProvider schemaProvider,
                                                        final Locale loc) throws OpenDataException {
        return COMPONENT_CONFIG_SCHEMA_BUILDER.build(
                ImmutableMap.of(
                    MANAGED_RESOURCE_PARAMS, getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.class), loc),
                    RESOURCE_ADAPTER_PARAMS, getConfigurationSchema(schemaProvider.getDescription(ResourceAdapterConfiguration.class), loc),
                    ATTRIBUTE_PARAMS, getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.AttributeConfiguration.class), loc),
                    EVENT_PARAMS, getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.EventConfiguration.class), loc)
                )
        );
    }

    /**
     * Gets configuration schema.
     *
     * @param component the component
     * @param locale the locale
     * @return the configuration schema
     * @throws OpenDataException the open data exception
     */
    protected static CompositeData getConfigurationSchema(final SnampComponentDescriptor component,
                                                        final String locale) throws OpenDataException {
        final Box<CompositeData> result = new Box<>();
        component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, input -> result.set(getConfigurationSchema(input, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale))));
        return result.get();
    }


}
