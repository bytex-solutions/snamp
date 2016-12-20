package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractBundleActivator;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.GatewayActivator;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SysLogGatewayActivator extends GatewayActivator<SysLogGateway> {
    @SpecialUse
    public SysLogGatewayActivator(){
        super(SysLogGatewayActivator::newGateway, configurationDescriptor(SysLogConfigurationDescriptor::getInstance));
    }

    private static SysLogGateway newGateway(final String instanceName,
                                            final DependencyManager dependencies) {
        return new SysLogGateway(instanceName);
    }

    @Override
    protected void addDependencies(Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(ClusterMember.class));
    }
}
