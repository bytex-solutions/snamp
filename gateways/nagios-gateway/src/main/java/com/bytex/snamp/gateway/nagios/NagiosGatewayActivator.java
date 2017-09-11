package com.bytex.snamp.gateway.nagios;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.service.http.HttpService;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class NagiosGatewayActivator extends GatewayActivator<NagiosGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public NagiosGatewayActivator() {
        super(NagiosGatewayActivator::newGateway,
                configurationDescriptor(NagiosGatewayConfigurationDescriptor::new),
                requiredBy(NagiosGateway.class).require(HttpService.class));
    }

    @SuppressWarnings("unchecked")
    private static NagiosGateway newGateway(final String instanceName,
                                            final DependencyManager dependencies) {
        return new NagiosGateway(instanceName, dependencies.getService(HttpService.class));
    }
}
