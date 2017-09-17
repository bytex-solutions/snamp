package com.bytex.snamp.supervision;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.AbstractWeakEventListenerList;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceTracker;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.elasticity.ScalingEvent;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.WillNotClose;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents basic infrastructure for custom supervisors.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AbstractSupervisor extends AbstractAggregator implements Supervisor {
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
        void add(final SupervisionEventListener listener, final Object handback) {
            add(new WeakSupervisionEventListener(listener, handback));
        }
    }

    private final class ResourceInGroupTracker extends ManagedResourceTracker {
        private ResourceInGroupTracker(final String groupName) {
            super(Utils.getBundleContextOfObject(AbstractSupervisor.this),
                    ManagedResourceConnectorClient.selector().setGroupName(groupName));
        }

        @Override
        protected void addResource(final ManagedResourceConnectorClient resource) throws Exception {
            try {
                resourceAdded(resource);
            } finally {
                resourceAdded(resource.getManagedResourceName());
            }
        }

        @Override
        protected void removeResource(final ManagedResourceConnectorClient resource) throws Exception {
            try {
                resourceRemoved(resource);
            } finally {
                resourceRemoved(resource.getManagedResourceName());
            }
        }
    }

    /**
     * Represents group name.
     */
    protected final String groupName;
    /**
     * Represents type of supervisor.
     */
    protected final String supervisorType;
    private final SupervisionEventListenerList listeners;
    private final ManagedResourceTracker groupTracker;

    protected AbstractSupervisor(final String groupName) {
        this.groupName = nullToEmpty(groupName);
        supervisorType = Supervisor.getSupervisorType(getClass());
        listeners = new SupervisionEventListenerList();
        groupTracker = new ResourceInGroupTracker(groupName);
    }

    /**
     * Executes supervision of the resource group.
     * @since 2.1
     */
    @OverridingMethodsMustInvokeSuper
    protected void start(){
        groupTracker.open();
    }

    private void resourceAdded(final String resourceName){
        listeners.fire(new ResourceAddedEvent(this, resourceName, groupName));
    }

    protected abstract void resourceAdded(@WillNotClose final ManagedResourceConnectorClient resourceInGroup) throws Exception;

    private void resourceRemoved(final String resourceName){
        listeners.fire(new ResourceRemovedEvent(this, resourceName, groupName));
    }

    protected abstract void resourceRemoved(@WillNotClose final ManagedResourceConnectorClient resourceInGroup) throws Exception;

    /**
     * Raises an event indicating that health status of the supervised group was changed.
     * @param event Event object.
     */
    protected final void healthStatusChanged(final HealthStatusChangedEvent event){
        listeners.fire(event);
    }

    /**
     * Raises an event indicating some scaling activity.
     * @param event Event object.
     */
    protected final void scalingHappens(final ScalingEvent event) {
        listeners.fire(event);
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
        return groupTracker.getTrackedResources();
    }

    @Override
    public String toString() {
        return supervisorType + ':' + groupName;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        groupTracker.close();
        clearCache();
    }
}
