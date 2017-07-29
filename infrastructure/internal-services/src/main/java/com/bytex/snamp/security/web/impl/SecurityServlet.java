package com.bytex.snamp.security.web.impl;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.web.JWTAuthFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Provides access to login endpoint.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
public final class SecurityServlet extends ServletContainer {

    private static final long serialVersionUID = 1107487431672546167L;
    public static final String CONTEXT = "/snamp/security";

    public SecurityServlet(final ClusterMember clusterMember){
        super(createAppConfig(clusterMember));
    }

    private static class InternalAuthFilter extends JWTAuthFilter {
        private InternalAuthFilter(final ClusterMember clusterMember) {
            super(clusterMember);
        }

        @Override
        protected boolean authenticationRequired(final HttpRequestContext request) {
            return !request.getPath().equalsIgnoreCase(WebAuthenticator.PATH);
        }
    }

    private static Application createAppConfig(final ClusterMember clusterMember){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new WebAuthenticator(clusterMember));
        result.getContainerRequestFilters().add(new InternalAuthFilter(clusterMember));
        return result;
    }
}
