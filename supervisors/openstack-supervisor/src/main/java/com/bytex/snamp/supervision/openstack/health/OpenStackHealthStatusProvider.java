package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import com.bytex.snamp.connector.health.ClusterRecoveryStatus;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import org.openstack4j.model.senlin.Cluster;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackHealthStatusProvider extends DefaultHealthStatusProvider {

    public OpenStackHealthStatusProvider(@Nonnull final ClusterMember clusterMember) {
        super(clusterMember);
    }

    private void updateClusterStatus(final Cluster cluster) { //this method can be called inside batch update only
        final ClusterMalfunctionStatus status;
        switch (cluster.getStatus()) {
            case RECOVERING:
                status = new ClusterRecoveryStatus(cluster.getName());
                break;
            case CRITICAL:
            case ERROR:
            case WARNING:
                status = new ProblemWithCluster(cluster.getName(), cluster);
                break;
            default:
                return;
        }
        status.getData().putAll(cluster.getMetadata());
        updateStatus(status);
    }

    public void updateStatus(final BundleContext context, final Cluster cluster, final Set<String> resources) {
        try (final SafeCloseable batchUpdate = startBatchUpdate()) {
            for (final String resourceName : resources)
                ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                    try {
                        updateStatus(resourceName, client);
                    } finally {
                        client.close();
                    }
                });
            //update health status of the cluster
            updateClusterStatus(cluster);
        }
    }
}
