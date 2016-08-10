package com.bytex.snamp.gateway.nagios;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NagiosGatewayActivator extends GatewayActivator<NagiosGateway> {

    @SpecialUse
    public NagiosGatewayActivator() {
        super(NagiosGatewayActivator::newGateway,
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportGatewayServiceManager<?, ?>[]{configurationDescriptor(NagiosGatewayConfigurationDescriptor::new)});
    }

    @SuppressWarnings("unchecked")
    private static NagiosGateway newGateway(final String instanceName,
                                            final RequiredService<?>... dependencies) {
        return new NagiosGateway(instanceName, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
    }
}
