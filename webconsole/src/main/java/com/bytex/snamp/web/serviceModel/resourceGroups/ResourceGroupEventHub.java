package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.supervision.*;
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
final class ResourceGroupEventHub extends AbstractStatefulFrameworkServiceTracker<Supervisor, SupervisorClient, Map<String, SupervisionEventListener>> implements SupervisionEventListener {
    ResourceGroupEventHub(){
        super(Supervisor.class, new InternalState<>(ImmutableMap.of()));
    }

    @Override
    public void handle(@Nonnull final SupervisionEvent event, final Object handback) {
        getConfiguration().values().forEach(listener -> listener.handle(event, handback));
    }

    private void addSupervisor(final String groupName, @WillNotClose final Supervisor supervisor){
        supervisor.addSupervisionEventListener(this, groupName);
    }

    /**
     * Invoked when new service is detected.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized void addService(@WillNotClose final SupervisorClient serviceClient) {
        if (!trackedServices.contains(getServiceId(serviceClient)))
            addSupervisor(getServiceId(serviceClient), serviceClient);
    }

    private void removeSupervisor(final String groupName,
                                  @WillNotClose final Supervisor supervisor) {
        supervisor.removeSupervisionEventListener(this);
    }

    /**
     * Invoked when service is removed from OSGi Service registry.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected synchronized void removeService(@WillNotClose final SupervisorClient serviceClient) {
        if (trackedServices.contains(getServiceId(serviceClient)))
            removeSupervisor(getServiceId(serviceClient), serviceClient);
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
    protected SupervisorSelector createServiceFilter() {
        return SupervisorClient.selector();
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
    protected void start(final Map<String, SupervisionEventListener> configuration) throws Exception {

    }

    void startTracking(@Nonnull final SupervisionEventListener destination) throws Exception {
        update(ImmutableMap.of("destination", destination));
    }
}
