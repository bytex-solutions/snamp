package com.bytex.snamp.security.web.impl;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.logging.Logger;

/**
 * Provides access to login endpoint.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SecurityServlet extends ServletContainer {
    private static final long serialVersionUID = 1107487431672546167L;
    public static final String CONTEXT = "/snamp/security";

    public SecurityServlet(final Logger logger){
        super(createAppConfig(logger));
    }

    private static Application createAppConfig(final Logger logger){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new WebAuthenticator(logger));
        return result;
    }
}
