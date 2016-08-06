package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;

import java.io.IOException;

/**
 * Represents an exception happens when persistent configuration manager cannot
 * restore SNAMP configuration entity from OSGi persistent configuration store.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
abstract class PersistentConfigurationException extends IOException {
    private static final long serialVersionUID = -518115699501252969L;

    /**
     * Represents PID that references the invalid configuration.
     */
    final String persistenceID;
    /**
     * Represents type of the configuration entity that cannot be restored from storage.
     */
    final Class<? extends EntityConfiguration> entityType;

    PersistentConfigurationException(final String pid,
                                     final Class<? extends EntityConfiguration> entityType,
                                     final Throwable e){
        super(String.format("Unable to read SNAMP %s configuration", pid), e);
        this.persistenceID = pid;
        this.entityType = entityType;
    }
}
