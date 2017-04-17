package com.bytex.snamp.supervision.discovery;

import javax.annotation.Nonnull;

/**
 * Indicates that the existing resource was removed.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceRemovedEvent extends ResourceDiscoveryEvent {
    public ResourceRemovedEvent(@Nonnull final Object source, @Nonnull final String resourceName) {
        super(source, resourceName);
    }
}
