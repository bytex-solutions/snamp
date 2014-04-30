package com.itworks.snamp.configuration;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents an exception occurred when SNAMP entity is not configured correctly.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ConfigurationException extends RuntimeException {
    /**
     * Represents an entity that is not configured correctly.
     */
    protected final ConfigurationEntity configurationEntity;

    /**
     * Initializes a new exception that represents invalid SNAMP configuration.
     * @param message A human-readable message that describes configuration trouble.
     * @param entity An entity that is not configured correctly.
     * @param cause Inner exception that caused by configuration validator.
     */
    protected ConfigurationException(final String message, final ConfigurationEntity entity, final Throwable cause){
        super(message, cause);
        this.configurationEntity = entity;
    }
}
