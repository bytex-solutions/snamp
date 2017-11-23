package com.bytex.snamp.connector.stub;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class StubConnectorActivator extends ManagedResourceActivator {
    private static final class StubConnectorLifecycleManager extends DefaultManagedResourceLifecycleManager<StubConnector>{

        @Nonnull
        @Override
        protected StubConnector createConnector(final String resourceName, final String connectionString, final Map<String, String> configuration) throws Exception {
            return new StubConnector(resourceName);
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public StubConnectorActivator(){
        super(new StubConnectorLifecycleManager());
    }
}
