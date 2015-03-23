package com.itworks.snamp.connectors;

import java.util.EventObject;

/**
 * Represents an event associated with the managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceEvent extends EventObject {
    private static final long serialVersionUID = -5789097108681245831L;

    ResourceEvent(final Object sender){
        super(sender);
    }
}
