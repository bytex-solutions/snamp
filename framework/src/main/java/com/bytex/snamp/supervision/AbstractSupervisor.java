package com.bytex.snamp.supervision;

import com.bytex.snamp.AbstractWeakEventListenerList;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceFilterBuilder;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.core.FrameworkServiceState;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.WillNotClose;
import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents basic infrastructure for custom supervisors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractSupervisor extends AbstractStatefulFrameworkServiceTracker<ManagedResourceConnector, ManagedResourceConnectorClient, SupervisorInfo> implements Supervisor {
    private static final class WeakSupervisionEventListener extends WeakEventListener<SupervisionEventListener, SupervisionEvent> {
        private final Object handback;

        WeakSupervisionEventListener(@Nonnull final SupervisionEventListener listener, final Object handback) {
            super(listener);
            this.handback = handback;
        }

        @Override
        protected void invoke(@Nonnull final SupervisionEventListener listener, @Nonnull final SupervisionEvent event) {
            listener.handle(event, handback);
        }
    }

    private static final class SupervisionEventListenerList extends AbstractWeakEventListenerList<SupervisionEventListener, SupervisionEvent> {
        boolean add(final SupervisionEventListener listener, final Object handback) {
            return add(new WeakSupervisionEventListener(listener, handback));
        }
    }


    private static final class SupervisorInternalState extends InternalState<SupervisorInfo>{
        private SupervisorInternalState(@Nonnull final FrameworkServiceState state, @Nonnull final SupervisorInfo params) {
            super(state, params);
        }

        private SupervisorInternalState() {
            super(EMPTY_CONFIGURATION);
        }

        @Override
        public SupervisorInternalState setConfiguration(@Nonnull final SupervisorInfo value) {
            return new SupervisorInternalState(state, value);
        }

        @Override
        public SupervisorInternalState transition(final FrameworkServiceState value) {
            switch (value) {
                case CLOSED:
                    return null;
                default:
                    return new SupervisorInternalState(value, configuration);
            }
        }

        private boolean configurationAreEqual(final SupervisorInfo other){
            return configuration.equals(other);
        }

        @Override
        protected boolean configurationAreEqual(final Map<String, ?> other) {
            return other instanceof SupervisorInfo && configurationAreEqual((SupervisorInfo) other);
        }
    }

    protected final String groupName;
    protected final String supervisorType;
    private final SupervisionEventListenerList listeners;

    protected AbstractSupervisor(final String groupName) {
        super(ManagedResourceConnector.class, new SupervisorInternalState());
        this.groupName = nullToEmpty(groupName);
        supervisorType = Supervisor.getSupervisorType(getClass());
        listeners = new SupervisionEventListenerList();
    }

    /**
     * Returns filter used to query managed resource connectors in the same group.
     * @return A filter used to query managed resource connectors in the same group.
     */
    @Nonnull
    @Override
    protected final ManagedResourceFilterBuilder createServiceFilter() {
        return ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName);
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

    @OverridingMethodsMustInvokeSuper
    protected void addResource(final String resourceName, @WillNotClose final ManagedResourceConnector connector){
        listeners.fire(GroupCompositionChanged.resourceAdded(this, resourceName, groupName));
    }

    /**
     * Invoked when new service is detected.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected final synchronized void addService(@WillNotClose final ManagedResourceConnectorClient serviceClient) {
        final String resourceName = getServiceId(serviceClient);
        if (trackedServices.contains(resourceName))
            getLogger().info(String.format("Resource %s is already attached to supervisor %s", resourceName, groupName));
        else
            addResource(resourceName, serviceClient);
    }

    @OverridingMethodsMustInvokeSuper
    protected void removeResource(final String resourceName, @WillNotClose final ManagedResourceConnector connector){
        listeners.fire(GroupCompositionChanged.resourceRemoved(this, resourceName, groupName));
    }

    /**
     * Invoked when service is removed from OSGi Service registry.
     *
     * @param serviceClient Service client.
     */
    @Override
    protected final synchronized void removeService(@WillNotClose final ManagedResourceConnectorClient serviceClient) {
        final String resourceName = getServiceId(serviceClient);
        if (trackedServices.contains(resourceName))
            removeResource(resourceName, serviceClient);
        else
            getLogger().info(String.format("Resource %s is already detached from supervisor %s", resourceName, groupName));
    }

    @Override
    public final void addSupervisionEventListener(@Nonnull final SupervisionEventListener listener, final Object handback) {
        listeners.add(listener, handback);
    }

    @Override
    public final void addSupervisionEventListener(@Nonnull final SupervisionEventListener listener) {
        addSupervisionEventListener(listener, null);
    }

    @Override
    public final void removeSupervisionEventListener(@Nonnull final SupervisionEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Gets immutable set of group members.
     *
     * @return Immutable set of group members.
     */
    @Override
    @Nonnull
    public final Set<String> getResources() {
        return trackedServices;
    }

    @Override
    public String toString() {
        return supervisorType + ':' + groupName;
    }

    @Override
    public final void close() throws IOException {
        try {
            super.close();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(String.format("Unable to terminate supervisor %s", toString()), e);
        } finally {
            listeners.clear();
        }
    }
}
