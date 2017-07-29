package com.bytex.snamp.supervision;

import javax.annotation.Nonnull;

/**
 * Indicates that the resources was removed from group.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class ResourceRemovedEvent extends GroupCompositionChangedEvent {
    private static final long serialVersionUID = -3746590266545029810L;

    public ResourceRemovedEvent(@Nonnull final Object source, @Nonnull final String resourceName, @Nonnull final String groupName) {
        super(source, resourceName, groupName);
    }
}
