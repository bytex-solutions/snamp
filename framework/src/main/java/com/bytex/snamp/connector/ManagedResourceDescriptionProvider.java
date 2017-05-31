package com.bytex.snamp.connector;

import com.bytex.snamp.configuration.ThreadPoolResolver;

/**
 * Provides parser of connector-related configuration parameters.
 * <p>
 *     Derived class should be placed in the same bundle where connector located.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface ManagedResourceDescriptionProvider extends ThreadPoolResolver {
}
