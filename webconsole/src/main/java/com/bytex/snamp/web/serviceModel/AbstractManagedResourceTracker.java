package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceFilterBuilder;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import java.util.Map;

/**
 * Represents slim version of resource tracker without custom configuration.
 * @param <V> Type of configuration values.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public abstract class AbstractManagedResourceTracker<V> extends AbstractStatefulFrameworkServiceTracker<ManagedResourceConnector, ManagedResourceConnectorClient, Map<String, V>> {
    protected AbstractManagedResourceTracker() {
        super(ManagedResourceConnector.class, new InternalState<>(ImmutableMap.of()));
    }

    @Nonnull
    @Override
    protected final ManagedResourceConnectorClient createClient(final ServiceReference<ManagedResourceConnector> serviceRef) throws InstanceNotFoundException {
        return new ManagedResourceConnectorClient(getBundleContext(), serviceRef);
    }

    @Override
    protected final String getServiceId(final ManagedResourceConnectorClient client) {
        return client.getManagedResourceName();
    }

    protected abstract void addResource(final ManagedResourceConnectorClient client);

    /**
     * Invoked when new service is detected.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected final void addService(final ManagedResourceConnectorClient serviceClient) {
        if(!trackedServices.contains(getServiceId(serviceClient)))
            addResource(serviceClient);
    }

    protected abstract void removeResource(final ManagedResourceConnectorClient client);

    /**
     * Invoked when service is removed from OSGi Service registry.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected final void removeService(final ManagedResourceConnectorClient serviceClient) {
        if(trackedServices.contains(getServiceId(serviceClient)))
            removeResource(serviceClient);
    }

    /**
     * Returns filter used to query services from OSGi Service Registry.
     *
     * @return A filter used to query services from OSGi Service Registry.
     */
    @Nonnull
    @Override
    protected ManagedResourceFilterBuilder createServiceFilter() {
        return ManagedResourceConnectorClient.filterBuilder();
    }
}
