package com.bytex.snamp.moa.watching;

import java.util.EventObject;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributeWatchEvent extends EventObject {
    private static final long serialVersionUID = 534626118854608203L;
    private String resourceName;
    private AttributeState state;

    public AttributeWatchEvent(final AttributeWatcherSettings source) {
        super(source);
        state = AttributeState.OK;
        resourceName = "";
    }

    public String getResourceName(){
        return resourceName;
    }

    public void setResourceName(final String value){
        resourceName = Objects.requireNonNull(value);
    }

    public AttributeState getState(){
        return state;
    }

    public void setState(final AttributeState value){
        state = Objects.requireNonNull(value);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public AttributeWatcherSettings getSource() {
        return (AttributeWatcherSettings) super.getSource();
    }
}
