package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.triggers.HealthStatusTrigger;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.health.ClusterMalfunctionStatus;
import com.bytex.snamp.supervision.health.ClusterRecoveryStatus;
import com.bytex.snamp.supervision.health.ClusterResizingStatus;
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

    private static HealthStatus getClusterStatus(final Cluster cluster) { //this method can be called inside batch update only
        final ClusterMalfunctionStatus status;
        switch (cluster.getStatus()) {
            case RESIZING:
                status = new ClusterResizingStatus();
                break;
            case RECOVERING:
                status = new ClusterRecoveryStatus();
                break;
            case CRITICAL:
            case ERROR:
                status = ProblemWithCluster.critical(cluster);
                break;
            case WARNING:
                status = ProblemWithCluster.warning(cluster);
                break;
            default:
                return new OkStatus();
        }
        status.getData().putAll(cluster.getMetadata());
        status.getData().put("clusterStatus", cluster.getStatus());
        return status;
    }

    private static HealthStatus getNodeStatus(final Node node) {
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
                return new OkStatus();
        }
        status.getData().putAll(node.getMetadata());
        status.getData().put("nodeStatus", node.getStatus());
        return status;
    }

    public void updateStatus(final BundleContext context,
                             final SenlinService senlin,
                             final Set<String> resources,
                             final HealthStatusTrigger callback) {
        final Cluster cluster = senlin.cluster().get(clusterID);
        if (cluster == null)
            throw new OS4JException(String.format("Cluster %s doesn't exist", clusterID));
        final ClusterNodes nodes = ClusterNodes.discover(senlin.node(), clusterID);
        try(final HealthStatusBuilder builder = statusBuilder()) {
            //update health status using resources in the group and status of the cluster
            builder.updateGroupStatus(getClusterStatus(cluster)).updateResourcesStatuses(context, resources);
            //extract health status for every cluster node
            for (final String resourceName : resources) {
                final HealthStatus nodeStatus = nodes.getByName(resourceName).map(OpenStackHealthStatusProvider::getNodeStatus).orElseGet(OkStatus::new);
                builder.updateResourceStatus(resourceName, nodeStatus);
            }
            builder.build(callback);
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
