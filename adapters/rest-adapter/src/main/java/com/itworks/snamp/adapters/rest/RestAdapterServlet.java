package com.itworks.snamp.adapters.rest;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class RestAdapterServlet extends ServletContainer {
    private static Application createResourceConfig(final RestAdapterService serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    public RestAdapterServlet(final HttpAttributesModel attributes, final boolean securityEnabled){
        super(createResourceConfig(new RestAdapterService(attributes, securityEnabled)));
    }
}
