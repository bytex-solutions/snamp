package com.bytex.snamp.webconsole.serviceModel;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.security.Principal;
import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class WebEvent extends EventObject {
    private static final long serialVersionUID = 3426260188036037856L;
    private Principal principal;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public WebEvent(final WebConsoleService source) {
        super(source);
    }

    @JsonIgnore
    public final Principal getPrincipal(){
        return principal;
    }

    @JsonIgnore
    public final void setPrincipal(final Principal value){
        this.principal = value;
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
