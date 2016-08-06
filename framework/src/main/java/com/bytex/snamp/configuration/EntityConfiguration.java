package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents a root interface for all agent configuration entities.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface EntityConfiguration {
    /**
     * The name of the parameter which contains description of the configuration entity.
     */
    String DESCRIPTION_KEY = "description";

    /**
     * Gets configuration parameters of this entity.
     * @return A map of configuration parameters.
     */
    Map<String, String> getParameters();

    default void setDescription(final String value){
        getParameters().put(DESCRIPTION_KEY, value);
    }

    default String getDescription(){
        return getParameters().get(DESCRIPTION_KEY);
    }

    default void setParameters(final Map<String, String> parameters){
        getParameters().clear();
        getParameters().putAll(parameters);
    }
}
