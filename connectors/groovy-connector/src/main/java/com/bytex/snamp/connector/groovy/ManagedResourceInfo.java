package com.bytex.snamp.connector.groovy;

import java.util.Collection;

import com.bytex.snamp.configuration.FeatureConfiguration;

/**
 * Represents information about scripted managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ManagedResourceInfo extends AutoCloseable {
    <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType);
}
