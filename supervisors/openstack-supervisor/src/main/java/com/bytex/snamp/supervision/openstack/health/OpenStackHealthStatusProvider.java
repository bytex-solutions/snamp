package com.bytex.snamp.supervision.openstack.health;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.ClusterMalfunctionStatus;
import com.bytex.snamp.connector.health.ClusterRecoveryStatus;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.openstack.SenlinHelpers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.model.senlin.Cluster;
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

    public OpenStackHealthStatusProvider(@Nonnull final ClusterMember clusterMember,
                                         @Nonnull final String clusterID,
                                         final boolean checkNodes) {
        super(clusterMember);
        this.clusterID = clusterID;
        this.checkNodes = checkNodes;
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

    public void updateStatus(final BundleContext context, final SenlinService senlin, final Set<String> resources) {
        final Cluster cluster = senlin.cluster().get(clusterID);
        if(cluster == null)
            throw new OS4JException(String.format("Cluster %s doesn't exist", clusterID));
        final BiMap<String, String> nodes = SenlinHelpers.getNodes(senlin.node(), clusterID);
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
        if(checkNodes){
            final NodeActionCreate checkAction = SenlinNodeActionCreate.build().check(ImmutableMap.of()).build();
            for(final String nodeID: nodes.keySet())
                senlin.node().action(nodeID, checkAction);
        }
    }
}
