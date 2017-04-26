package com.bytex.snamp.supervision.def;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.checkers.AttributeChecker;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.ResourceConnectorMalfunction;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ExperimentalHealthStatusProvider implements Stateful {
    private static final class AggregatedResourceGroupHealthStatus extends ResourceGroupHealthStatus{
        private static final long serialVersionUID = -411291568337973940L;
        private final HealthStatus groupStatus;

        private AggregatedResourceGroupHealthStatus(final HealthStatus groupStatus){
            this.groupStatus = groupStatus;
        }

        private AggregatedResourceGroupHealthStatus() {
            this((HealthStatus) null);
        }

        private AggregatedResourceGroupHealthStatus(final AggregatedResourceGroupHealthStatus proto){
            super(proto);
            groupStatus = proto.groupStatus;
        }

        @Override
        public HealthStatus getSummaryStatus() {
            return groupStatus == null ? super.getSummaryStatus() : super.getSummaryStatus().worst(groupStatus);
        }

        boolean like(final AggregatedResourceGroupHealthStatus other) {
            if (other.keySet().equals(keySet()) && groupStatus == other.groupStatus) {
                for (final Entry<String, HealthStatus> thisEntry : entrySet())
                    if (!thisEntry.getValue().like(other.get(thisEntry.getKey())))
                        return false;
                return groupStatus == null || other.groupStatus.like(groupStatus);
            } else
                return false;
        }

        void setResourceStatus(final String resourceName,
                               final ManagedResourceConnector connector,
                               final Map<String, AttributeChecker> checkers){
            //1. Using health check provided by connector itself
            HealthStatus newStatus = connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
            if (!(newStatus instanceof OkStatus)) {
                put(resourceName, newStatus);
                return;
            }
            //2. read attributes from connector
            final AttributeList attributes;
            {
                final Optional<AttributeSupport> support = connector.queryObject(AttributeSupport.class);
                if (support.isPresent())
                    try {
                        attributes = support.get().getAttributes();
                    } catch (final JMException e) {
                        put(resourceName, new ResourceConnectorMalfunction(resourceName, e));
                        return;
                    }
                else
                    attributes = new AttributeList();
            }
            //3. update health status using attribute checkers
            for (final Attribute attribute : attributes.asList()) {
                final AttributeChecker checker = checkers.get(attribute.getName());
                if (checker != null)
                    newStatus = checker.getStatus(attribute).createStatus(resourceName, attribute).worst(newStatus);
            }
            put(resourceName, newStatus);
        }
    }

    private volatile AggregatedResourceGroupHealthStatus status = new AggregatedResourceGroupHealthStatus();
    private final ConcurrentMap<String, AttributeChecker> checkers = new ConcurrentHashMap<>();

    private void updateStatus(AggregatedResourceGroupHealthStatus newStatus){
        AggregatedResourceGroupHealthStatus previous;
        synchronized (this){
            previous = status;
            if(previous.like(newStatus))
                return; //nothing changed
            status = newStatus;
        }
        //fire listeners
    }

    protected final void updateStatus(final BundleContext context,
                                      final Set<String> resources,
                                      final HealthStatus groupStatus){
        final AggregatedResourceGroupHealthStatus newStatus = new AggregatedResourceGroupHealthStatus(groupStatus);
        for (final String resourceName : resources)
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                try {
                    newStatus.setResourceStatus(client.getManagedResourceName(), client, checkers);
                } finally {
                    client.close();
                }
            });
        updateStatus(newStatus);
    }

    protected synchronized final void removeResource(final String resourceName){
        final AggregatedResourceGroupHealthStatus newStatus = new AggregatedResourceGroupHealthStatus(status);
        newStatus.remove(resourceName);
        updateStatus(newStatus);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        status = new AggregatedResourceGroupHealthStatus();
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    public ResourceGroupHealthStatus getStatus() {
        return status;
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     * @param handback Handback object that will be returned into listener.
     */
    public void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener, final Object handback) {

    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    public void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener) {

    }

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    public void removeHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener) {

    }
}
