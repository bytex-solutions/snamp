package com.itworks.snamp.connectors.groovy;

import java.util.Collection;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;

/**
 * Represents information about scripted managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourceInfo extends AutoCloseable {
    <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType);
}
