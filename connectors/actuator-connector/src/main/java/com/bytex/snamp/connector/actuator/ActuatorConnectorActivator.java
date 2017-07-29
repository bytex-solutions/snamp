package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;

import static com.bytex.snamp.ArrayUtils.toArray;

/**
 * Represents activator of {@link ActuatorConnector}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ActuatorConnectorActivator extends ManagedResourceActivator<ActuatorConnector> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public ActuatorConnectorActivator() {
        super(ActuatorConnectorActivator::createConnector,
                requiredBy(ActuatorConnector.class).require(ThreadPoolRepository.class),
                toArray(configurationDescriptor(ActuatorConnectorDescriptionProvider::getInstance)));
    }

    @Nonnull
    private static ActuatorConnector createConnector(final String resourceName,
                                                     final ManagedResourceInfo configuration,
                                                     final DependencyManager... dependencies) throws URISyntaxException {
        return new ActuatorConnector(resourceName, configuration);
    }
}
