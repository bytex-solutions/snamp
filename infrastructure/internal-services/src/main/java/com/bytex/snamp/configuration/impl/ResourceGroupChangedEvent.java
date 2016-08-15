package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import java.util.EventObject;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ResourceGroupChangedEvent extends EventObject {
    private static final long serialVersionUID = -4736956024889794205L;
    private final String oldGroupName;

    ResourceGroupChangedEvent(final ManagedResourceConfiguration source,
                              final String oldGroupName) {
        super(source);
        this.oldGroupName = Objects.requireNonNull(oldGroupName);
    }

    String getOldGroupName(){
        return oldGroupName;
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public ManagedResourceConfiguration getSource() {
        return (ManagedResourceConfiguration) super.getSource();
    }
}
