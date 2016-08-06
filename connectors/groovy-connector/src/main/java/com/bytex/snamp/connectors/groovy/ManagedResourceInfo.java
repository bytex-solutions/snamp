package com.bytex.snamp.connectors.groovy;

import java.util.Collection;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.FeatureConfiguration;

/**
 * Represents information about scripted managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ManagedResourceInfo extends AutoCloseable {
    <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType);
}
