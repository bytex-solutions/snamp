package com.bytex.snamp.gateway;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.osgi.framework.BundleContext;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents activator of a gateway that can be automatically configured at boot time.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public abstract class BootConfigGatewayActivator<G extends Gateway> extends GatewayActivator<G> {
    protected BootConfigGatewayActivator(GatewayFactory<G> factory, SupportServiceManager<?, ?>[] optionalServices) {
        super(factory, optionalServices);
    }

    protected BootConfigGatewayActivator(GatewayFactory<G> factory, RequiredService<?>[] gatewayDependencies, SupportServiceManager<?, ?>[] optionalServices) {
        super(factory, gatewayDependencies, optionalServices);
    }

    protected abstract String bootInstanceName();

    protected abstract void bootConfiguration(final Map<String, String> configuration);

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        super.start(context, bundleLevelDependencies);
        bundleLevelDependencies.add(ConfigurationManager.class, context);
    }

    private boolean bootConfiguration(final AgentConfiguration configuration) {
        final String instanceName = bootInstanceName();
        if (isNullOrEmpty(instanceName))
            return false;
        final GatewayConfiguration gateway = configuration.getGateways().getOrAdd(instanceName);
        gateway.setType(gatewayType);
        bootConfiguration(gateway);
        return true;
    }

    private void bootConfiguration(final ConfigurationManager manager) {
        try {
            manager.processConfiguration(this::bootConfiguration);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, String.format("Failed to boot configuration for gateway %s", gatewayType), e);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        dependencies.getService(ConfigurationManager.class).ifPresent(this::bootConfiguration);
        super.activate(context, activationProperties, dependencies);
    }
}
