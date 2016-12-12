package com.bytex.snamp.security.web.impl;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.logging.Logger;

/**
 * Provides access to login endpoint.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
public final class SecurityServlet extends ServletContainer {
    private static final long serialVersionUID = 1107487431672546167L;
    public static final String CONTEXT = "/snamp/security";

    public SecurityServlet(final Logger logger){
        super(createAppConfig(logger));
    }

    private static class InternalAuthFilter extends WebSecurityFilter {
        @Override
        protected boolean authenticationRequired(ContainerRequest request) {
            return !request.getPath().equalsIgnoreCase(WebAuthenticator.PATH);
        }
    }

    private static Application createAppConfig(final Logger logger){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new WebAuthenticator(logger));
        result.getContainerRequestFilters().add(new InternalAuthFilter());
        return result;
    }
}
