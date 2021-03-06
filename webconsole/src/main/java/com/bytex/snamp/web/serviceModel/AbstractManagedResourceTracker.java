package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceSelector;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
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
    protected final String getServiceId(@WillNotClose final ManagedResourceConnectorClient client) {
        return client.getManagedResourceName();
    }

    protected abstract void addResource(@WillNotClose final ManagedResourceConnectorClient client);

    /**
     * Invoked when new service is detected.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized final void addService(@WillNotClose final ManagedResourceConnectorClient serviceClient) {
        if(!trackedServices.contains(getServiceId(serviceClient)))
            addResource(serviceClient);
    }

    protected abstract void removeResource(@WillNotClose final ManagedResourceConnectorClient client);

    /**
     * Invoked when service is removed from OSGi Service registry.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized final void removeService(@WillNotClose final ManagedResourceConnectorClient serviceClient) {
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
    protected ManagedResourceSelector createServiceFilter() {
        return ManagedResourceConnectorClient.selector();
    }
}
