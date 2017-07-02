package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.FeatureConfiguration;

import java.util.Collection;

/**
 * Represents information about scripted managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface ManagedResourceInfo extends AutoCloseable {
    <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType);
}
