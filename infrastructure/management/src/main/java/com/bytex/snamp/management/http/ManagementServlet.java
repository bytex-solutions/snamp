package com.bytex.snamp.management.http;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
public final class ManagementServlet extends ServletContainer {
    private static final long serialVersionUID = -2354174814566144236L;
    public static final String CONTEXT = "/snamp/management";

    public ManagementServlet(final ClusterMember clusterMember){
        super(createAppConfig(clusterMember));
    }

    private static Application createAppConfig(final ClusterMember clusterMember){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        final WebSecurityFilter filter = new WebSecurityFilter(clusterMember);
        result.getContainerRequestFilters().add(filter);
        result.getContainerResponseFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        result.getSingletons().add(new ResourceConfigurationService());
        result.getSingletons().add(new ManagementService());
        result.getSingletons().add(new GatewayConfigurationService());
        result.getSingletons().add(new ResourceGroupConfigurationService());
        result.getSingletons().add(new SupervisorConfigurationService());
        result.getSingletons().add(new ThreadPoolConfigurationService());
        return result;
    }
}
