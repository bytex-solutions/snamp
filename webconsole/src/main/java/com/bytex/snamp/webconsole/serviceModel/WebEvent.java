package com.bytex.snamp.webconsole.serviceModel;

import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class WebEvent extends EventObject {
    private static final long serialVersionUID = 3426260188036037856L;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public WebEvent(final Object source) {
        super(source);
    }
}
