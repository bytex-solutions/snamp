package com.bytex.snamp.supervision;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.FrameworkServiceState;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents basic infrastructure for custom supervisors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractSupervisor extends AbstractAggregator implements Supervisor, ServiceListener {
    private static final class SupervisorLoggingScope extends LoggingScope{
        private SupervisorLoggingScope(final Supervisor requester, final String operationName){
            super(requester, operationName);
        }

        static SupervisorLoggingScope connectorChangesDetected(final Supervisor requester) {
            return new SupervisorLoggingScope(requester, "processResourceConnectorChanges");
        }
    }
    @Immutable
    private static final class InternalState {
        final SupervisorInfo parameters;
        final FrameworkServiceState state;

        private InternalState(final FrameworkServiceState state, final SupervisorInfo params) {
            this.state = state;
            this.parameters = Objects.requireNonNull(params);
        }

        private InternalState(){
            this(FrameworkServiceState.CREATED, new EmptySupervisorConfiguration());
        }

        InternalState setParameters(final SupervisorInfo value) {
            return new InternalState(state, value);
        }

        InternalState transition(final FrameworkServiceState value) {
            switch (value) {
                case CLOSED:
                    return null;
                default:
                    return new InternalState(value, parameters);
            }
        }

        boolean parametersAreEqual(final Map<String, String> newParameters) {
            if (parameters.size() == newParameters.size()) {
                for (final Map.Entry<String, String> entry : newParameters.entrySet())
                    if (!Objects.equals(parameters.get(entry.getKey()), entry.getValue()))
                        return false;
                return true;
            } else return false;
        }

        private boolean equals(final InternalState other) {
            return state.equals(other.state) && parametersAreEqual(other.parameters);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof InternalState && equals((InternalState) other);
        }

        @Override
        public String toString() {
            return state.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, parameters);
        }
    }

    private final Set<String> resources;
    protected final String groupName;
    private final String supervisorType;
    private SupervisorInfo configuration;

    protected AbstractSupervisor(final String groupName) {
        this.groupName = nullToEmpty(groupName);
        resources = Collections.newSetFromMap(new ConcurrentHashMap<>());
        supervisorType = Supervisor.getSupervisorType(getClass());
        configuration = new EmptySupervisorConfiguration();
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        if (ManagedResourceConnector.isResourceConnector(event.getServiceReference())) {
            final BundleContext context = getBundleContext();
            @SuppressWarnings("unchecked")
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, (ServiceReference<ManagedResourceConnector>) event.getServiceReference());
            try(final SupervisorLoggingScope logger = SupervisorLoggingScope.connectorChangesDetected(this)){
                switch (event.getType()){
                    case ServiceEvent.UNREGISTERING:
                    case ServiceEvent.MODIFIED_ENDMATCH:
                        resources.remove(client.getManagedResourceName());
                        break;
                    case ServiceEvent.REGISTERED:
                        resources.add(client.getManagedResourceName());
                        break;
                    default:
                        logger.info(String.format("Unexpected event %s captured by supervisor %s for resource %s",
                                event.getType(),
                                supervisorType,
                                client.getManagedResourceName()));
                }
            } finally {
                client.release(context);
            }
        }
    }

    /**
     * Gets immutable set of group members.
     *
     * @return Immutable set of group members.
     */
    @Override
    @Nonnull
    public Set<String> getResources() {
        return resources;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        clearCache();
    }

    @Override
    public String toString() {
        return groupName;
    }
}
