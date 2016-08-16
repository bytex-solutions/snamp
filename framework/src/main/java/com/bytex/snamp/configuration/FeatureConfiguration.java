package com.bytex.snamp.configuration;

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
        getParameters().put(NAME_KEY, value);
    }

    default String getAlternativeName(){
        return getParameters().get(NAME_KEY);
    }

    default boolean isAutomaticallyAdded(){
        return getParameters().containsKey(AUTOMATICALLY_ADDED_KEY);
    }

    default void setAutomaticallyAdded(final boolean value){
        if(value)
            getParameters().put(AUTOMATICALLY_ADDED_KEY, Boolean.TRUE.toString());
        else
            getParameters().remove(AUTOMATICALLY_ADDED_KEY);
    }
}
