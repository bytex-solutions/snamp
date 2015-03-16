package com.itworks.snamp.adapters.http;


import com.itworks.snamp.internal.annotations.SpecialUse;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.Servlet3Continuation;

import javax.ws.rs.core.Application;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class HttpAdapterServlet extends ServletContainer {
    private static final long serialVersionUID = -4507714971473917629L;

    private static Application createResourceConfig(final AdapterRestService serviceInstance){
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    HttpAdapterServlet(final AttributeSupport attributes,
                       final NotificationSupport notifications){
        super(createResourceConfig(new AdapterRestService(attributes, notifications)));
    }

    //do not remove. It is necessary for Atmosphere and maven-bundle-plugin for correct import of Jetty package
    @SpecialUse
    private static Class<? extends Continuation> getJettyContinuationClass(){
        return Servlet3Continuation.class;
    }
}
