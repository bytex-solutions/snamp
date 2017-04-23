package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.def.DefaultSupervisor;
import com.bytex.snamp.supervision.openstack.discovery.OpenStackDiscoveryService;
import com.bytex.snamp.supervision.openstack.health.OpenStackHealthStatusProvider;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.openstack.OSFactory;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor {
    @Aggregation    //non-cached
    private SenlinClusterService clusterService;
    private String clusterID;

    OpenStackSupervisor(final String groupName) {
        super(groupName);
    }

    @Override
    protected OpenStackSupervisorDescriptionProvider getDescriptionProvider() {
        return OpenStackSupervisorDescriptionProvider.getInstance();
    }

    private static boolean isClusteringSupported(final OSClientV3 client){
        return client.getSupportedServices().contains(ServiceType.CLUSTERING);
    }

    /**
     * Starts the tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param configuration Tracker startup parameters.
     * @throws Exception Unable to start tracking.
     */
    @Override
    protected void start(final SupervisorInfo configuration) throws Exception {
        final OpenStackSupervisorDescriptionProvider parser = getDescriptionProvider();
        clusterID = parser.parseClusterID(configuration);
        final OSClientV3 openStackClient = OSFactory.builderV3()
                .provider(parser.parseCloudProvider(configuration))
                .endpoint(parser.parseApiEndpoint(configuration))
                .scopeToProject(parser.parseProject(configuration), parser.parseProjectDomain(configuration))
                .credentials(parser.parseUserName(configuration), parser.parsePassword(configuration), parser.parseUserDomain(configuration))
                .authenticate();
        if (!isClusteringSupported(openStackClient)) {    //Compute is not supported. Shutting down.
            final String message = String.format("OpenStack installation %s doesn't support clustering via Senlin. Supervisor for group %s is not started", openStackClient.getEndpoint(), groupName);
            throw new OS4JException(message);
        }
        clusterService = openStackClient.senlin().cluster();
        final Cluster cluster = clusterService.get(clusterID);
        getLogger().info(String.format("Cluster %s is associated with group %s. Cluster status: %s(%s)",
                clusterID,
                groupName,
                cluster.getStatus(),
                cluster.getStatusReason()));
        //setup discovery service
        if(parser.isAutoDiscovery(configuration))
            overrideDiscoveryService(new OpenStackDiscoveryService(groupName));
        final boolean enableElastMan = parser.isElasticityManagementEnabled(configuration);
        super.start(configuration);
    }

    private void stopElasticityManager() throws Exception{

    }

    /**
     * Executes automatically using scheduling time.
     */
    @Override
    protected void supervise() {
        final SenlinClusterService clusterService = this.clusterService;
        if (clusterService == null)
            return;
        final OpenStackHealthStatusProvider provider = queryObject(DefaultHealthStatusProvider.class)
                .flatMap(p -> Convert.toType(p, OpenStackHealthStatusProvider.class))
                .orElse(null);
        if (provider != null)
            provider.updateStatus(getBundleContext(), clusterService.get(clusterID), getResources());
    }

    /**
     * Stops tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     *
     * @throws Exception Unable to stop tracking resources.
     */
    @Override
    protected void stop() throws Exception {
        try {
            Utils.closeAll(() -> super.stop(), this::stopElasticityManager);
        } finally {
            clusterService = null;
        }
    }
}
