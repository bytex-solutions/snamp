package com.bytex.snamp.webconsole;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.servlet.WebAppResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents customized servlet container.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JerseyServletContainer extends ServletContainer {
    private static final long serialVersionUID = 5710139261115306229L;

    JerseyServletContainer(final WebConsoleService consoleAPI, final ManagementService managementAPI){
        super(createAppConfig(consoleAPI, managementAPI));
    }

    // We ignore unchecked warning because we know that ContainerRequestFilters
    // contains instances of ContainerRequestFilter class
    @SuppressWarnings("unchecked")
    private static Application createAppConfig(final WebConsoleService consoleAPI, final ManagementService managementAPI){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(consoleAPI);
        result.getSingletons().add(managementAPI);
        result.getContainerRequestFilters().add(AuthenticationFilter.class);
        result.getContainerResponseFilters().add(TokenRefreshFilter.class);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }
}
