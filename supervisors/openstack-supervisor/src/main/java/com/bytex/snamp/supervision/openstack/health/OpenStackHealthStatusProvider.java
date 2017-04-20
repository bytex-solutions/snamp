package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import com.bytex.snamp.connector.health.ClusterRecoveryStatus;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.model.senlin.Cluster;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackHealthStatusProvider extends DefaultHealthStatusProvider {
    private final SenlinClusterService clusterService;
    private final String clusterID;

    public OpenStackHealthStatusProvider(@Nonnull final SenlinClusterService clusterService,
                                         @Nonnull final String clusterID){
        this.clusterService = clusterService;
        this.clusterID = clusterID;
    }

    private void updateClusterStatus(final Cluster cluster) { //this method can be called inside batch update only
        final ClusterMalfunctionStatus status;
        switch (cluster.getStatus()) {
            case RECOVERING:
                status = new ClusterRecoveryStatus(cluster.getName());
                break;
            case CRITICAL:
            case ERROR:
                status = new ProblemWithCluster(cluster.getName(), true);
                break;
            case WARNING:
                status = new ProblemWithCluster(cluster.getName(), false);
                break;
            default:
                return;
        }
        status.getData().putAll(cluster.getMetadata());
        updateStatus(status);
    }

    @Override
    public void updateStatus(final Set<String> resources) {
        try (final SafeCloseable batchUpdate = startBatchUpdate()) {
            for (final String resourceName : resources)
                ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName).ifPresent(client -> {
                    try {
                        updateStatus(resourceName, client);
                    } finally {
                        client.close();
                    }
                });
            //update health status of the cluster
            final Cluster cluster = clusterService.get(clusterID);
            if (cluster != null)
                updateClusterStatus(cluster);
        }
    }
}
