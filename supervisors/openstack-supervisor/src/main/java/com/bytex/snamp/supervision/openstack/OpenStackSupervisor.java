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
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor {
    private final AtomicReference<Token> openStackClientToken;

    OpenStackSupervisor(final String groupName) {
        super(groupName);
        openStackClientToken = new AtomicReference<>();
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
        openStackClientToken.set(openStackClient.getToken());  //according with http://openstack4j.com/learn/threads/
        final String clusterID = parser.parseClusterID(configuration).orElseGet(() -> {
            //resolve cluster ID by cluster name
            for (final Cluster cluster : openStackClient.senlin().cluster().list())
                if (Objects.equals(cluster.getName(), groupName))
                    return cluster.getId();
            throw new OS4JException(String.format("Cluster with name %s is not registered in Senlin", groupName));
        });
        final Cluster cluster = openStackClient.senlin().cluster().get(clusterID);
        if (cluster == null)
            throw new OS4JException(String.format("Cluster with ID %s is not registered in Senlin", clusterID));
        else
            getLogger().info(String.format("Cluster %s is associated with group %s. Cluster status: %s(%s)",
                    clusterID,
                    groupName,
                    cluster.getStatus(),
                    cluster.getStatusReason()));
        overrideHealthStatusProvider(new OpenStackHealthStatusProvider(clusterMember, clusterID));
        //setup discovery service
        if (parser.isAutoDiscovery(configuration))
            overrideDiscoveryService(new OpenStackDiscoveryService(groupName, clusterID));
        final boolean enableElastMan = parser.isElasticityManagementEnabled(configuration);
        super.start(configuration);
    }

    private void stopElasticityManager() throws Exception{

    }

    private void updateHealthStatus(@Nonnull final SenlinClusterService clusterService, @Nonnull final OpenStackHealthStatusProvider provider){
        provider.updateStatus(getBundleContext(), clusterService, getResources());
    }

    /**
     * Executes automatically using scheduling time.
     */
    @Override
    protected void supervise() {
        final Token openStackClientToken = this.openStackClientToken.get();
        if(openStackClientToken == null) {
            getLogger().warning(String.format("OpenStack client for group %s is signed out", groupName));
            return;
        }
        final OSClientV3 openStackClient = OSFactory.clientFromToken(openStackClientToken);
        final SenlinClusterService clusterService = openStackClient.senlin().cluster();
        assert clusterService != null;

        queryObject(DefaultHealthStatusProvider.class)
                .flatMap(p -> Convert.toType(p, OpenStackHealthStatusProvider.class))
                .ifPresent(provider -> updateHealthStatus(clusterService, provider));


        //OSAuthenticator.reAuthenticate();
        this.openStackClientToken.compareAndSet(openStackClientToken, openStackClient.getToken()); //if re-authentication forced
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
            openStackClientToken.set(null);
        }
    }
}
