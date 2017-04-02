package com.bytex.snamp.gateway.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for HTTP gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class HttpGatewayActivator extends GatewayActivator<HttpGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public HttpGatewayActivator() {
        super(HttpGatewayActivator::newGateway,
                simpleDependencies(HttpService.class),
                new SupportServiceManager<?, ?>[]{
                        configurationDescriptor(HttpGatewayConfigurationDescriptor::new)
                });
    }

    @SuppressWarnings("unchecked")
    private static HttpGateway newGateway(final String instanceName,
                                          final DependencyManager dependencies) {
        return new HttpGateway(instanceName, dependencies.getDependency(HttpService.class));
    }
}
