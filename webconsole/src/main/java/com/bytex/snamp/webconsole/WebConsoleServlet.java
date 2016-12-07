package com.bytex.snamp.webconsole;

import com.bytex.snamp.security.web.WebSecurityFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents customized servlet container.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleServlet extends ServletContainer {
    private static final long serialVersionUID = 5710139261115306229L;
    static final String CONTEXT = "/snamp/console";

    WebConsoleServlet(){
        super(createAppConfig());
    }

    // We ignore unchecked warning because we know that ContainerRequestFilters
    // contains instances of ContainerRequestFilter class
    private static Application createAppConfig() {
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new WebConsoleService());
        final WebSecurityFilter filter = new WebSecurityFilter();
        result.getContainerResponseFilters().add(filter);
        result.getContainerRequestFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }
}
