package com.itworks.snamp.configuration;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.jmx.AbstractCompositeData;

import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents a copy of configuration entity parameters (obtained from {@link com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity#getParameters()}
 * wrapped into {@link javax.management.openmbean.CompositeData}.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigParameters extends AbstractCompositeData<String> {
    /**
     * The name of the composite type.
     */
    public static final String TYPE_NAME = "com.itworks.snamp.configuration.ConfigParameters";

    /**
     * The type of all items in this composite data.
     */
    public static final SimpleType<String> ITEM_TYPE = SimpleType.STRING;

    private static final String PLACEHOLDER_PARAM = "$placeholder$";

    private static final long serialVersionUID = 8225426679292248903L;

    private static Map<String, String> fillWithPlaceholderIfNecessary(final Map<String, String> parameters){
        return parameters.isEmpty() ? ImmutableMap.of(PLACEHOLDER_PARAM, "") : parameters;
    }

    private ConfigParameters(final Map<String, String> parameters){
        super(fillWithPlaceholderIfNecessary(parameters));
    }

    /**
     * Initializes a new copy of configuration entity parameters
     * @param entity The configuration entity. Cannot be {@literal null}.
     */
    public ConfigParameters(final ConfigurationEntity entity){
        this(entity.getParameters());
    }

    /**
     * Creates a new instance of the empty set of configuration parameters.
     * @return An empty set of configuration parameters.
     */
    public static ConfigParameters empty(){
        return new ConfigParameters(ImmutableMap.<String, String>of());
    }

    /**
     * Gets name of the composite type.
     *
     * @return The name of the composite type.
     */
    @Override
    protected String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Gets description of the composite type.
     *
     * @return The description of the composite type.
     */
    @Override
    protected String getTypeDescription() {
        return "Configuration entity parameters";
    }

    /**
     * Gets type of all values in this composite data.
     *
     * @return The type of all values in this composite data.
     */
    @Override
    protected SimpleType<String> getItemType() {
        return ITEM_TYPE;
    }

    /**
     * Gets description of the item.
     *
     * @param itemName The name of the item.
     * @return The description of the item.
     */
    @Override
    protected String getItemDescription(final String itemName) {
        return "Configuration entity parameter";
    }
}
