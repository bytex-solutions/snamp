package com.bytex.snamp.supervision;

import javax.annotation.Nonnull;

/**
 * Indicates that the new resource was introduced into group.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceAddedEvent extends GroupCompositionChangedEvent {
    private static final long serialVersionUID = 6072272918044187599L;

    public ResourceAddedEvent(@Nonnull final Object source, @Nonnull final String resourceName, @Nonnull final String groupName) {
        super(source, resourceName, groupName);
    }
}
