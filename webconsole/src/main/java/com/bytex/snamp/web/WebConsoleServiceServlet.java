package com.bytex.snamp.web;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Application;

/**
 * Represents web console service which must be hosted as REST service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
final class WebConsoleServiceServlet extends ServletContainer {

    WebConsoleServiceServlet(@Nonnull final RESTController service) {
        super(createConfig(service, WebConsoleSecurityFilter.forRestAPI(ClusterMember.get(Utils.getBundleContextOfObject(service)))));
    }

    private static Application createConfig(final RESTController service, final WebSecurityFilter filter) {
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(service);
        result.getContainerRequestFilters().add(filter);
        result.getContainerResponseFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }
}
