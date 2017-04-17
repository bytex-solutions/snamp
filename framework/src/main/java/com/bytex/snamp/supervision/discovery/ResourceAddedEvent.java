package com.bytex.snamp.supervision.discovery;

import javax.annotation.Nonnull;

/**
 * Indicates that the new resource was added.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceAddedEvent extends ResourceDiscoveryEvent {
    public ResourceAddedEvent(@Nonnull final Object source,
                                 @Nonnull final String resourceName) {
        super(source,resourceName);
    }
}
