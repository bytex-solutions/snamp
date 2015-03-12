package com.itworks.snamp.adapters.http;


import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class HttpAdapterServlet extends ServletContainer {

    private static Application createResourceConfig(final AdapterRestService serviceInstance){
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    HttpAdapterServlet(final AttributeSupport attributes,
                       final NotificationSupport notifications){
        super(createResourceConfig(new AdapterRestService(attributes, notifications)));
    }
}
