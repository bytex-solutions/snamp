package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents activator of {@link ActuatorConnector}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ActuatorConnectorActivator extends ManagedResourceActivator<ActuatorConnector> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public ActuatorConnectorActivator() {
        super(ActuatorConnectorActivator::createConnector,
                requiredBy(ActuatorConnector.class).require(ThreadPoolRepository.class),
                new SupportServiceManager<?, ?>[]{
                    configurationDescriptor(ActuatorConnectorDescriptionProvider::getInstance),
                    discoveryService(ActuatorConnectorActivator::createDiscoveryService)
        });
    }

    @Nonnull
    private static ActuatorConnector createConnector(final String resourceName,
                                                     final ManagedResourceInfo configuration,
                                                     final DependencyManager... dependencies) throws URISyntaxException {
        return new ActuatorConnector(resourceName,
                new URI(configuration.getConnectionString()),
                configuration);
    }

    private static ActuatorFeatureDiscoveryService createDiscoveryService(final DependencyManager dependencies){
        return new ActuatorFeatureDiscoveryService();
    }
}
