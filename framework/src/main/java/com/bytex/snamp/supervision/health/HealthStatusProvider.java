package com.bytex.snamp.supervision.health;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ManagedResourceFilterBuilder;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.supervision.SupervisorAggregatedService;
import com.bytex.snamp.supervision.SupervisorClient;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents health check service used to supervise groups of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusProvider extends SupervisorAggregatedService, Stateful {
    /**
     * Gets status of the managed resource group.
     * @return Gets status of the managed resource group.
     */
    @Nonnull
    ResourceGroupHealthStatus getStatus();

    static ResourceGroupHealthStatus getHealthStatus(final BundleContext context, final ManagedResourceFilterBuilder filter) {
        final class FakeResourceGroupHealthStatus extends HashMap<String, HealthStatus> implements ResourceGroupHealthStatus, Consumer<ManagedResourceConnectorClient> {
            private static final long serialVersionUID = 420503389377659109L;

            private void putStatus(final String resourceName, final ManagedResourceConnector connector) {
                put(resourceName,
                        connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new));
            }

            @Override
            public void accept(final ManagedResourceConnectorClient client) {
                try {
                    putStatus(client.getManagedResourceName(), client);
                } finally {
                    client.close();
                }
            }
        }

        final FakeResourceGroupHealthStatus status = new FakeResourceGroupHealthStatus();
        for (final String resourceName : filter.getResources(context))
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(status);
        return status;
    }

    static ResourceGroupHealthStatus getHealthStatus(final BundleContext context, final String groupName){
        final Optional<SupervisorClient> supervisor = SupervisorClient.tryCreate(context, groupName);
        if(supervisor.isPresent())
            try(final SupervisorClient client = supervisor.get()){
                return client.queryObject(HealthStatusProvider.class)
                        .map(HealthStatusProvider::getStatus)
                        .orElseGet(() -> getHealthStatus(context, ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName)));
            }
        else
            return getHealthStatus(context, ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName));
    }
}
