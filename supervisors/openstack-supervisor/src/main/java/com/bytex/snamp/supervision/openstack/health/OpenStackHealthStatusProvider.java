package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import com.bytex.snamp.connector.health.ClusterRecoveryStatus;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.openstack.SenlinClusterServiceHandler;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.model.senlin.Cluster;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackHealthStatusProvider extends DefaultHealthStatusProvider implements SenlinClusterServiceHandler {

    @Override
    public void handle(final String clusterID, final SenlinClusterService clusterService) {
        final Cluster cluster = clusterService.get(clusterID);
        if (cluster != null) {
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
    }
}
