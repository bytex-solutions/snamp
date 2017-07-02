package com.bytex.snamp.web;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.RBAC;
import com.bytex.snamp.security.web.HttpMethodAuthorizationFilter;
import com.bytex.snamp.security.web.JWTAuthFilter;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.annotation.Nonnull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Application;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents web console service which must be hosted as REST service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
final class WebConsoleServiceServlet extends ServletContainer {
    private static final class WebConsoleAuthorizationFilter extends HttpMethodAuthorizationFilter {
        private WebConsoleAuthorizationFilter() {
            addRestriction(HttpMethod.POST, RBAC.ADMIN_ROLE, RBAC.USER_ROLE);
            addRestriction(HttpMethod.PUT, RBAC.ADMIN_ROLE, RBAC.USER_ROLE);
            addRestriction(HttpMethod.DELETE, RBAC.ADMIN_ROLE, RBAC.USER_ROLE);
        }

        @Override
        protected boolean authorizationRequired(final HttpRequestContext request) {
            switch (request.getPath()) {
                case "compute":
                    return false;
                default:
                    return true;
            }
        }
    }

    WebConsoleServiceServlet(@Nonnull final RESTController service) {
        super(createConfig(service));
    }

    private static Application createConfig(final RESTController service) {
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(service);
        //authentication filter
        final JWTAuthFilter authenticationFilter = WebConsoleSecurityFilter.forRestAPI(ClusterMember.get(getBundleContextOfObject(service)));
        result.getContainerRequestFilters().add(authenticationFilter);
        result.getContainerResponseFilters().add(authenticationFilter);
        //authorization filter
        result.getContainerRequestFilters().add(new WebConsoleAuthorizationFilter());
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }
}
