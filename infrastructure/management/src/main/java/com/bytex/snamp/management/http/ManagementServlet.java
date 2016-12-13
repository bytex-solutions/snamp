package com.bytex.snamp.management.http;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
public final class ManagementServlet extends ServletContainer {
    private static final long serialVersionUID = -2354174814566144236L;
    public static final String CONTEXT = "/snamp/management";

    public ManagementServlet(){
        super(createAppConfig());
    }

    private static Application createAppConfig(){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        final WebSecurityFilter filter = new WebSecurityFilter();
        result.getContainerRequestFilters().add(filter);
        result.getContainerResponseFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        result.getSingletons().add(new ResourceConfigurationService());
        result.getSingletons().add(new ManagementService());
        result.getSingletons().add(new GatewayConfigurationService());
        result.getSingletons().add(new ResourceGroupConfigurationService());
        return result;
    }
}
