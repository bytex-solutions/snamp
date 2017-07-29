package com.bytex.snamp.configuration;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * Represents a feature of the managed resource.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface FeatureConfiguration extends EntityConfiguration {
    /**
     * Represents configuration parameter containing alternative name of the feature.
     */
    String NAME_KEY = "name";

    /**
     * Represents configuration parameter indicating that this feature was created by machine, not by human.
     */
    String AUTOMATICALLY_ADDED_KEY = "automaticallyAdded";

    default void setAlternativeName(final String value){
        put(NAME_KEY, value);
    }

    default String getAlternativeName(){
        return get(NAME_KEY);
    }

    default boolean isAutomaticallyAdded() {
        return getValue(this, AUTOMATICALLY_ADDED_KEY, Boolean::parseBoolean).orElse(Boolean.FALSE);
    }

    default void setAutomaticallyAdded(final boolean value) {
        if (value)
            put(AUTOMATICALLY_ADDED_KEY, Boolean.TRUE.toString());
        else
            remove(AUTOMATICALLY_ADDED_KEY);
    }

    /**
     * Indicates that feature is overridden.
     * @return {@literal true}, if feature is overridden; otherwise, {@literal false}.
     */
    boolean isOverridden();

    void setOverridden(final boolean value);
}
