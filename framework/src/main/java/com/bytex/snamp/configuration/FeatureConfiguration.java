package com.bytex.snamp.configuration;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.putValue;

/**
 * Represents a feature of the managed resource.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
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
        return getValue(this, AUTOMATICALLY_ADDED_KEY, Boolean::parseBoolean).orElse(false);
    }

    default void setAutomaticallyAdded(final boolean value) {
        if (value)
            putValue(this, AUTOMATICALLY_ADDED_KEY, Boolean.TRUE, Object::toString);
        else
            remove(AUTOMATICALLY_ADDED_KEY);
    }

    @Override
    FeatureConfiguration asReadOnly();
}
