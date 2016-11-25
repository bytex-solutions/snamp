package com.bytex.snamp.webconsole;

import com.bytex.snamp.security.web.WebSecurityFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents customized servlet container.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleServlet extends ServletContainer {
    /**
     * The constant AUTH_COOKIE.
     */
    static final String AUTH_COOKIE = "snamp-auth-token";
    static final String AUTHENTICATE_PATH = "auth";
    static final String CONTEXT = "/snamp/console";

    private static final class WebConsoleSecurityFilter extends WebSecurityFilter {
        private WebConsoleSecurityFilter() {
            super(AUTH_COOKIE);
        }

        @Override
        protected boolean authenticationRequired(final ContainerRequest request) {
            return !request.getPath().equalsIgnoreCase(AUTHENTICATE_PATH);
        }
    }

    private static final long serialVersionUID = 5710139261115306229L;

    WebConsoleServlet(final WebConsoleService consoleAPI, final ResourceService managementAPI,
                      final GatewayService gatewayService){
        super(createAppConfig(consoleAPI, managementAPI, gatewayService));
    }

    // We ignore unchecked warning because we know that ContainerRequestFilters
    // contains instances of ContainerRequestFilter class
    @SuppressWarnings("unchecked")
    private static Application createAppConfig(final WebConsoleService consoleAPI, final ResourceService managementAPI,
                                               final GatewayService gatewayService){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(consoleAPI);
        result.getSingletons().add(managementAPI);
        result.getSingletons().add(gatewayService);
        result.getContainerResponseFilters().add(new WebConsoleSecurityFilter());
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }
}
