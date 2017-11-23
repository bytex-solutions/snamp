package com.bytex.snamp.supervision.def;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.NotThreadSafe;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents default implementation of {@link HealthStatusProvider}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class DefaultHealthStatusProvider implements HealthStatusProvider, AutoCloseable {

    private static final class DefaultResourceGroupHealthStatus extends HashMap<String, HealthStatus> implements ResourceGroupHealthStatus{
        private static final long serialVersionUID = -411291568337973940L;
        private final HealthStatus groupStatus;

        private DefaultResourceGroupHealthStatus(@Nonnull final HealthStatus groupStatus){
            this.groupStatus = Objects.requireNonNull(groupStatus);
        }

        private DefaultResourceGroupHealthStatus() {
            this(new OkStatus());
        }

        private DefaultResourceGroupHealthStatus(final DefaultResourceGroupHealthStatus proto, final HealthStatus groupStatus){
            super(proto);
            this.groupStatus = Objects.requireNonNull(groupStatus);
        }

        private DefaultResourceGroupHealthStatus(final DefaultResourceGroupHealthStatus proto){
            this(proto, proto.groupStatus);
        }

        @Override
        public HealthStatus getSummaryStatus() {
            return values().stream().reduce(HealthStatus::worst).orElse(groupStatus).worst(groupStatus);
        }

        boolean like(final DefaultResourceGroupHealthStatus other) {
            if (!groupStatus.like(other.groupStatus))
                return false;
            final OkStatus OK = new OkStatus();
            for (final String resourceName : Sets.union(keySet(), other.keySet())) {
                final HealthStatus thisStatus = getOrDefault(resourceName, OK),
                        otherStatus = other.getOrDefault(resourceName, OK);
                if (!thisStatus.like(otherStatus))
                    return false;
            }
            return true;
        }

        void setResourceStatus(final String resourceName,
                               final ManagedResourceConnector connector,
                               final Map<String, AttributeChecker> checkers) {
            //1. Using health check provided by connector itself
            HealthStatus newStatus = connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
            if (OkStatus.notOk(newStatus)) {
                put(resourceName, newStatus);
                return;
            }
            //2. read attributes from connector
            final AttributeList attributes;
            {
                final Optional<AttributeManager> support = connector.queryObject(AttributeManager.class);
                if (support.isPresent())
                    try {
                        attributes = support.get().getAttributes();
                    } catch (final JMException e) {
                        if (e.getCause() instanceof IOException)
                            put(resourceName, new ConnectionProblem((IOException) e.getCause()));
                        else
                            put(resourceName, new ResourceConnectorMalfunction(e));
                        return;
                    }
                else
                    attributes = new AttributeList();
            }
            //3. update health status using attribute checkers
            for (final Attribute attribute : attributes.asList()) {
                final AttributeChecker checker = checkers.get(attribute.getName());
                if (checker != null)
                    newStatus = checker.getStatus(attribute).createStatus(attribute).worst(newStatus);
            }
            put(resourceName, newStatus);
        }

        @Override
        public String toString() {
            return getSummaryStatus().toString();
        }
    }

    private final ConcurrentMap<String, AttributeChecker> checkers;
    private volatile DefaultResourceGroupHealthStatus status;

    /**
     * Initializes a new health status provider.
     */
    public DefaultHealthStatusProvider() {
        checkers = new ConcurrentHashMap<>();
        status = new DefaultResourceGroupHealthStatus();
    }

    private synchronized void updateStatus(final DefaultResourceGroupHealthStatus newStatus, final HealthStatusTrigger trigger) {
        final DefaultResourceGroupHealthStatus prevStatus = status;
        status = newStatus;
        if (!prevStatus.like(newStatus))    //if status was not changed then exit without any notifications.
            trigger.statusChanged(prevStatus, newStatus);
    }

    /**
     * Represents health status builder.
     */
    @NotThreadSafe
    protected static final class HealthStatusBuilder extends WeakReference<DefaultHealthStatusProvider> implements SafeCloseable, Stateful {
        private DefaultResourceGroupHealthStatus newStatus;

        private HealthStatusBuilder(final DefaultHealthStatusProvider provider){
            super(provider);
        }

        /**
         * Gets a reference to the underlying provider.
         * @return Underlying health status provider.
         * @throws IllegalStateException Health status provider is no longer accessible.
         */
        @Override
        @Nonnull
        public DefaultHealthStatusProvider get() {
            final DefaultHealthStatusProvider provider = super.get();
            if (provider == null)
                throw new IllegalStateException("Health status provider is no longer accessible");
            else
                return provider;
        }

        private DefaultResourceGroupHealthStatus getOrCreateStatus() {
            if (newStatus == null)
                newStatus = new DefaultResourceGroupHealthStatus();
            return newStatus;
        }

        public HealthStatusBuilder updateGroupStatus(final HealthStatus groupStatus) {
            newStatus = newStatus == null ?
                    new DefaultResourceGroupHealthStatus(groupStatus) :
                    new DefaultResourceGroupHealthStatus(newStatus, groupStatus);
            return this;
        }

        public HealthStatusBuilder updateResourceStatus(final String resourceName, final HealthStatus newStatus) {
            getOrCreateStatus().merge(resourceName, newStatus, HealthStatus::worst);
            return this;
        }

        public HealthStatusBuilder updateResourceStatus(final String resourceName, final ManagedResourceConnector connector) {
            getOrCreateStatus().setResourceStatus(resourceName, connector, get().checkers);
            return this;
        }

        private void updateAndClose(final ManagedResourceConnectorClient client) {
            try {
                updateResourceStatus(client.getManagedResourceName(), client);
            } finally {
                client.close();
            }
        }

        public HealthStatusBuilder updateResourcesStatuses(final BundleContext context,
                                                           final Set<String> resources) {
            for (final String resourceName : resources)
                ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(this::updateAndClose);
            return this;
        }

        /**
         * Resets internal state of the object.
         */
        @Override
        public void reset() {
            newStatus = null;
        }

        /**
         * Updates health status stored in the provider.
         *
         * @param trigger A trigger used to catch new health status if it was changed.
         * @return This builder.
         * @throws IllegalStateException Health status provider is no longer accessible.
         */
        public HealthStatusBuilder build(final HealthStatusTrigger trigger) throws IllegalStateException {
            if (newStatus != null)
                get().updateStatus(newStatus, trigger);
            reset();
            return this;
        }

        /**
         * Makes builder as non-reusable object.
         */
        @Override
        public void clear() {
            super.clear();
            reset();
        }

        /**
         * Makes builder as non-reusable object.
         */
        @Override
        public void close() {
            clear();
        }
    }

    /**
     * Creates a new builder for health status.
     * @return A new builder instance.
     */
    protected final HealthStatusBuilder statusBuilder() {
        return new HealthStatusBuilder(this);
    }

    final synchronized void removeResource(final String resourceName) {
        final DefaultResourceGroupHealthStatus newStatus = new DefaultResourceGroupHealthStatus(status);
        newStatus.remove(resourceName);
        updateStatus(newStatus, HealthStatusTrigger.NO_OP);
    }

    /**
     * Adds checker for the specified attribute.
     * @param attributeName Name of the attribute to check.
     * @param checker Attribute checker implementation. Cannot be {@literal null}.
     */
    public final void addChecker(final String attributeName, @Nonnull final AttributeChecker checker){
        checkers.put(attributeName, Objects.requireNonNull(checker));
    }

    /**
     * Removes checker for the specified attribute.
     * @param attributeName Name of the attribute.
     * @return Removed attribute checker.
     */
    public final AttributeChecker removeChecker(final String attributeName){
        return checkers.remove(attributeName);
    }

    /**
     * Removes all attribute checkers.
     */
    public final void removeCheckers(){
        checkers.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public synchronized void reset() {
        status = new DefaultResourceGroupHealthStatus();
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public final ResourceGroupHealthStatus getStatus() {
        return status;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        checkers.clear();
    }
}
