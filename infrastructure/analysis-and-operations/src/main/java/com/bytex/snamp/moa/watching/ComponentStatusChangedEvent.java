package com.bytex.snamp.moa.watching;

import com.bytex.snamp.supervision.GroupStatusDetails;

import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ComponentStatusChangedEvent extends EventObject {
    private static final long serialVersionUID = 2442700408424867171L;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    protected ComponentStatusChangedEvent(final ComponentWatcher source) {
        super(source);
    }

    public abstract GroupStatusDetails getStatusDetails();

    public abstract GroupStatusDetails getPreviousStatusDetails();
}
