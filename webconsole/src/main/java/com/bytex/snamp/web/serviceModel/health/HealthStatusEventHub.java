package com.bytex.snamp.web.serviceModel.health;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.supervision.Supervisor;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.SupervisorFilterBuilder;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.management.InstanceNotFoundException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HealthStatusEventHub extends AbstractStatefulFrameworkServiceTracker<Supervisor, SupervisorClient, Map<String, HealthStatusEventListener>> implements HealthStatusEventListener {
    HealthStatusEventHub(){
        super(Supervisor.class, new InternalState<>(ImmutableMap.of()));
    }

    @Override
    public void statusChanged(final HealthStatusChangedEvent event) {
        getConfiguration().values().forEach(listener -> listener.statusChanged(event));
    }

    /**
     * Invoked when new service is detected.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized void addService(@WillNotClose final SupervisorClient serviceClient) {
        if (!trackedServices.contains(getServiceId(serviceClient)))
            Aggregator.queryAndAccept(serviceClient, HealthStatusProvider.class, provider -> provider.addHealthStatusEventListener(this));
    }

    /**
     * Invoked when service is removed from OSGi Service registry.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized void removeService(@WillNotClose final SupervisorClient serviceClient) {
        if (trackedServices.contains(getServiceId(serviceClient)))
            Aggregator.queryAndAccept(serviceClient, HealthStatusProvider.class, provider -> provider.removeHealthStatusEventListener(this));
    }

    @Nonnull
    @Override
    protected SupervisorClient createClient(final ServiceReference<Supervisor> serviceRef) throws InstanceNotFoundException {
        return new SupervisorClient(getBundleContext(), serviceRef);
    }

    @Override
    protected String getServiceId(@WillNotClose final SupervisorClient client) {
        return client.getGroupName();
    }

    /**
     * Returns filter used to query services from OSGi Service Registry.
     *
     * @return A filter used to query services from OSGi Service Registry.
     */
    @Nonnull
    @Override
    protected SupervisorFilterBuilder createServiceFilter() {
        return SupervisorClient.filterBuilder();
    }

    @Override
    protected void stop() {
    }

    /**
     * Starts the tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param configuration Tracker startup parameters.
     * @throws Exception Unable to start tracking.
     */
    @Override
    protected void start(final Map<String, HealthStatusEventListener> configuration) throws Exception {

    }

    void startTracking(@Nonnull final HealthStatusEventListener destination) throws Exception {
        update(ImmutableMap.of("destination", destination));
    }
}
