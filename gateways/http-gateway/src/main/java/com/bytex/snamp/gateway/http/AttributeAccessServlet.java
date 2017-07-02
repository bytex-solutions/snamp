package com.bytex.snamp.gateway.http;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AttributeAccessServlet extends ServletContainer {
    private static final long serialVersionUID = 1446026354420742643L;


    AttributeAccessServlet(final AttributeSupport registeredAttributes){
        super(createConfig(registeredAttributes));
    }

    private static Application createConfig(final AttributeSupport registeredAttributes){
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new AttributeAccessService(registeredAttributes));
        return result;
    }
}
