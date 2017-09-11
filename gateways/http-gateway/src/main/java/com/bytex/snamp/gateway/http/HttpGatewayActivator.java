package com.bytex.snamp.gateway.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for HTTP gateway.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class HttpGatewayActivator extends GatewayActivator<HttpGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public HttpGatewayActivator() {
        super(HttpGatewayActivator::newGateway,
                configurationDescriptor(HttpGatewayConfigurationDescriptor::new),
                requiredBy(HttpGateway.class).require(HttpService.class));
    }

    @SuppressWarnings("unchecked")
    private static HttpGateway newGateway(final String instanceName,
                                          final DependencyManager dependencies) {
        return new HttpGateway(instanceName, dependencies.getService(HttpService.class));
    }
}
