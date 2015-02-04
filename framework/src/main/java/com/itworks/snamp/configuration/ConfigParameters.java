package com.itworks.snamp.configuration;

import com.itworks.snamp.jmx.AbstractCompositeData;

import javax.management.openmbean.SimpleType;
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

    /**
     * Initializes a new copy of configuration entity parameters
     * @param entity The configuration entity. Cannot be {@literal null}.
     */
    public ConfigParameters(final ConfigurationEntity entity){
        super(entity.getParameters());
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
