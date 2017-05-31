package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents template of managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourceTemplate extends TypedEntityConfiguration, ThreadPoolConfigurationSupport {
    /**
     * Represents name of configuration parameter that can be used to enable Smart mode of the connector.
     */
    String SMART_MODE_KEY = "smartMode";

    @Nonnull
    EntityMap<? extends AttributeConfiguration> getAttributes();

    @Nonnull
    EntityMap<? extends EventConfiguration> getEvents();

    @Nonnull
    EntityMap<? extends OperationConfiguration> getOperations();

    /**
     * Determines whether the smart mode is enabled for managed resource.
     *
     * @return {@literal true}, if smart mode is enabled; otherwise, {@literal false}.
     */
    default boolean isSmartMode() {
        return isSmartModeEnabled(this);
    }

    default void setSmartMode(final boolean value){
        if(value)
            put(SMART_MODE_KEY, Boolean.TRUE.toString());
        else
            remove(SMART_MODE_KEY);
    }

    static boolean isSmartModeEnabled(final Map<String, String> parameters){
        switch (parameters.getOrDefault(SMART_MODE_KEY, Boolean.FALSE.toString())) {
            case "true":
            case "TRUE":
            case "yes":
            case "YES":
                return true;
            default:
                return false;
        }
    }
}
