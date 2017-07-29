package com.bytex.snamp.connector.stub;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.beans.IntrospectionException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class StubConnectorActivator extends ManagedResourceActivator<StubConnector> {
    @SpecialUse(SpecialUse.Case.OSGi)
    public StubConnectorActivator(){
        super(StubConnectorActivator::createConnector);
    }

    @Nonnull
    private static StubConnector createConnector(final String resourceName,
                                        final ManagedResourceInfo configuration,
                                        final DependencyManager dependencies) throws IntrospectionException {
        return new StubConnector(resourceName, configuration);
    }
}
