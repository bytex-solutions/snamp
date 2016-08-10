package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * Represents bundle activator for REST adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class HttpGatewayActivator extends GatewayActivator<HttpGateway> {

    @SpecialUse
    public HttpGatewayActivator() {
        super(HttpGatewayActivator::newGateway,
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportGatewayServiceManager<?, ?>[]{
                        configurationDescriptor(HttpGatewayConfigurationDescriptor::new)
                });
    }

    @SuppressWarnings("unchecked")
    private static HttpGateway newGateway(final String instanceName,
                                          final RequiredService<?>... dependencies) {
        return new HttpGateway(instanceName, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
    }
}
