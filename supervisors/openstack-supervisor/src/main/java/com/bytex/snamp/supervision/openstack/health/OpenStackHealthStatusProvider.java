package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import com.bytex.snamp.connector.health.ClusterRecoveryStatus;
import com.bytex.snamp.connector.health.ClusterResizingStatus;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.openstack.ClusterNodes;
import com.google.common.collect.ImmutableMap;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.model.senlin.Node;
import org.openstack4j.model.senlin.NodeActionCreate;
import org.openstack4j.openstack.senlin.domain.SenlinNodeActionCreate;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenStackHealthStatusProvider extends DefaultHealthStatusProvider {
    private final String clusterID;
    private final boolean checkNodes;
    private final ClusterMember clusterMember;

    public OpenStackHealthStatusProvider(@Nonnull final ClusterMember clusterMember,
                                         @Nonnull final String clusterID,
                                         final boolean checkNodes) {
        this.clusterID = clusterID;
        this.checkNodes = checkNodes;
        this.clusterMember = clusterMember;
    }

    private void updateClusterStatus(final Cluster cluster) { //this method can be called inside batch update only
        final ClusterMalfunctionStatus status;
        switch (cluster.getStatus()) {
            case RESIZING:
                status = new ClusterResizingStatus(cluster.getName());
                break;
            case RECOVERING:
                status = new ClusterRecoveryStatus(cluster.getName());
                break;
            case CRITICAL:
            case ERROR:
                status = ProblemWithCluster.critical(cluster);
                break;
            case WARNING:
                status = ProblemWithCluster.warning(cluster);
                break;
            default:
                return;
        }
        status.getData().putAll(cluster.getMetadata());
        status.getData().put("clusterStatus", cluster.getStatus());
        updateStatus(status);
    }

    private void updateNodeStatus(final Node node) {
        final ProblemWithClusterNode status;
        switch (Server.Status.forValue(node.getStatus())) {
            case ERROR:
                status = ProblemWithClusterNode.error(node);
                break;
            case MIGRATING:
                status = ProblemWithClusterNode.migrating(node);
                break;
            case REBOOT:
                status = ProblemWithClusterNode.reboot(node, false);
                break;
            case HARD_REBOOT:
                status = ProblemWithClusterNode.reboot(node, true);
                break;
            case SHUTOFF:
                status = ProblemWithClusterNode.shutoff(node);
                break;
            case PAUSED:
                status = ProblemWithClusterNode.paused(node);
                break;
            case RESIZE:
                status = ProblemWithClusterNode.resize(node);
                break;
            case SUSPENDED:
                status = ProblemWithClusterNode.suspended(node);
                break;
            default:
                return;
        }
        status.getData().putAll(node.getMetadata());
        status.getData().put("nodeStatus", node.getStatus());
        updateStatus(status);
    }

    public void updateStatus(final BundleContext context, final SenlinService senlin, final Set<String> resources) {
        final Cluster cluster = senlin.cluster().get(clusterID);
        if (cluster == null)
            throw new OS4JException(String.format("Cluster %s doesn't exist", clusterID));
        final ClusterNodes nodes = ClusterNodes.discover(senlin.node(), clusterID);
        try (final SafeCloseable batchUpdate = startBatchUpdate()) {
            for (final String resourceName : resources) {
                ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                    try {
                        updateStatus(resourceName, client);
                    } finally {
                        client.close();
                    }
                });
                //update node status
                nodes.getByName(resourceName).ifPresent(this::updateNodeStatus);
            }
            //update health status of the cluster
            updateClusterStatus(cluster);
        }
        //force check nodes only at active cluster node
        if (checkNodes && clusterMember.isActive()) {
            final NodeActionCreate checkAction = SenlinNodeActionCreate.build().check(ImmutableMap.of()).build();
            for (final String nodeID : nodes.ids())
                senlin.node().action(nodeID, checkAction);
        }
        nodes.clear();  //help GC
    }
}
