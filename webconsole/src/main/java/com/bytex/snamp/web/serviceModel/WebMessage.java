package com.bytex.snamp.web.serviceModel;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.EventObject;

/**
 * Represents an event that can be thrown by web console services.
 * <p>
 *     These events will be propagated through WebSocket to the browser-based application.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$messageType")
public abstract class WebMessage extends EventObject {
    private static final long serialVersionUID = 3426260188036037856L;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public WebMessage(final WebConsoleService source) {
        super(source);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    @JsonIgnore
    public final WebConsoleService getSource() {
        return (WebConsoleService) super.getSource();
    }
}
