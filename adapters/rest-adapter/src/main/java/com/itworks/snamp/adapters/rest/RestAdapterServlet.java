package com.itworks.snamp.adapters.rest;


import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class RestAdapterServlet extends ServletContainer {

    private static ResourceConfig createResourceConfig(final RestAdapterService serviceInstance){
        final ResourceConfig result = new ResourceConfig();
        result.register(serviceInstance);
        return result;
    }

    public RestAdapterServlet(final HttpAttributesModel attributes, final boolean securityEnabled){
        super(createResourceConfig(new RestAdapterService(attributes, securityEnabled)));
    }
}
