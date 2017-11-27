package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents activator of {@link ActuatorConnector}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ActuatorConnectorActivator extends ManagedResourceActivator {
    private static final class ActuatorConnectorFactory extends ManagedResourceLifecycleManager<ActuatorConnector>{

        @Nonnull
        @Override
        protected ActuatorConnector createConnector(final String resourceName, final ManagedResourceConfiguration configuration) throws URISyntaxException {
            return new ActuatorConnector(resourceName, new ActuatorConnectionOptions(new URI(configuration.getConnectionString()), configuration));
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public ActuatorConnectorActivator() {
        super(new ActuatorConnectorFactory(),
                configurationDescriptor(ActuatorConnectorDescriptionProvider::getInstance));
    }
}
