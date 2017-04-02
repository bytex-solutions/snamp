package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceFilterBuilder;
import com.bytex.snamp.connector.StatefulManagedResourceTracker;
import com.bytex.snamp.core.FrameworkServiceState;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents basic infrastructure for custom supervisors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractSupervisor extends StatefulManagedResourceTracker<SupervisorInfo> implements Supervisor {
    private static final class SupervisorInternalState extends InternalState<SupervisorInfo>{
        private SupervisorInternalState(@Nonnull final FrameworkServiceState state, @Nonnull final SupervisorInfo params) {
            super(state, params);
        }

        private SupervisorInternalState() {
            super(new EmptySupervisorInfo());
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
        protected boolean configurationAreEqual(final Map<String, String> other) {
            return other instanceof SupervisorInfo && configurationAreEqual((SupervisorInfo) other);
        }
    }

    private final Set<String> resources;
    protected final String groupName;
    protected final String supervisorType;

    protected AbstractSupervisor(final String groupName) {
        super(new SupervisorInternalState());
        this.groupName = nullToEmpty(groupName);
        resources = Collections.newSetFromMap(new ConcurrentHashMap<>());
        supervisorType = Supervisor.getSupervisorType(getClass());
    }

    /**
     * Returns filter used to query managed resource connectors in the same group.
     * @return A filter used to query managed resource connectors in the same group.
     */
    @Nonnull
    @Override
    protected final ManagedResourceFilterBuilder createResourceFilter() {
        return ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void addResource(final ManagedResourceConnectorClient connector) {
        resources.add(connector.getManagedResourceName());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void removeResource(final ManagedResourceConnectorClient connector) {
        resources.remove(connector.getManagedResourceName());
    }

    /**
     * Gets immutable set of group members.
     *
     * @return Immutable set of group members.
     */
    @Override
    @Nonnull
    public final Set<String> getResources() {
        return resources;
    }

    @Override
    public String toString() {
        return supervisorType + ':' + groupName;
    }
}
