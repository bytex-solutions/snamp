package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents a root interface for all agent configuration entities.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface EntityConfiguration extends Map<String, String> {
    /**
     * The name of the parameter which contains description of the configuration entity.
     */
    String DESCRIPTION_KEY = "description";

    default void setDescription(final String value){
        put(DESCRIPTION_KEY, value);
    }

    default String getDescription(){
        return get(DESCRIPTION_KEY);
    }

    default void load(final Map<String, String> parameters){
        clear();
        putAll(parameters);
    }

    /**
     * Creates read-only copy of this configuration.
     * @return Read-only copy of this configuration.
     */
    EntityConfiguration asReadOnly();
}
