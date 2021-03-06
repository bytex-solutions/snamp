package com.bytex.snamp.connector;

import javax.annotation.Nonnull;
import java.util.EventObject;

/**
 * Represents an event associated with the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ResourceEvent extends EventObject {
    private static final long serialVersionUID = -5789097108681245831L;
    private final String resourceName;

    ResourceEvent(@Nonnull final Object sender, final String resourceName){
        super(sender);
        this.resourceName = resourceName;
    }

    /**
     * Gets name of the managed resource that emits this event.
     * @return The name of the managed resource.
     */
    public final String getResourceName(){
        return resourceName;
    }
}
