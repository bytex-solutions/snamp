package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.Convert;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.supervision.def.DefaultElasticityManager;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.def.DefaultSupervisor;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.elasticity.MaxClusterSizeReachedEvent;
import com.bytex.snamp.supervision.elasticity.ScaleInEvent;
import com.bytex.snamp.supervision.elasticity.ScaleOutEvent;
import com.bytex.snamp.supervision.openstack.discovery.OpenStackDiscoveryService;
import com.bytex.snamp.supervision.openstack.elasticity.OpenStackElasticityManager;
import com.bytex.snamp.supervision.openstack.elasticity.OpenStackScalingEvaluationContext;
import com.bytex.snamp.supervision.openstack.health.OpenStackHealthStatusProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor implements OpenStackScalingEvaluationContext {
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

    private static String getClusterIdByName(final SenlinClusterService clusterService,
                                             final String clusterName) {
        return clusterService.list()
                .stream()
                .filter(cluster -> cluster.getName().equals(clusterName))
                .map(Cluster::getId)
                .findFirst()
                .orElseThrow(() -> new OS4JException(String.format("Cluster with name %s is not registered in Senlin", clusterName)));
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
        //Obtain cluster ID
        final String clusterID = parser.parseClusterID(configuration)
                .orElseGet(() -> getClusterIdByName(openStackClient.senlin().cluster(), groupName));
        final Cluster cluster = openStackClient.senlin().cluster().get(clusterID);
        if (cluster == null)
            throw new OS4JException(String.format("Cluster with ID %s is not registered in Senlin", clusterID));
        else
            getLogger().info(String.format("Cluster %s is associated with group %s. Cluster status: %s(%s)",
                    clusterID,
                    groupName,
                    cluster.getStatus(),
                    cluster.getStatusReason()));
        //setup supervisor
        openStackClientToken.set(openStackClient.getToken());  //according with http://openstack4j.com/learn/threads/
        overrideHealthStatusProvider(new OpenStackHealthStatusProvider(clusterMember, clusterID, parser.checkNodes(configuration)));
        //setup discovery service
        if (parser.isAutoDiscovery(configuration))
            overrideDiscoveryService(new OpenStackDiscoveryService(groupName, clusterID, configuration.getDiscoveryConfig().getConnectionStringTemplate()));
        //setup elasticity manager
        if(configuration.getAutoScalingConfig().isEnabled())
            overrideElasticityManager(new OpenStackElasticityManager(clusterID));
        super.start(configuration);
    }

    private void updateHealthStatus(@Nonnull final SenlinService senlin, @Nonnull final OpenStackHealthStatusProvider provider){
        provider.updateStatus(getBundleContext(), senlin, getResources(), this);
    }

    private void synchronizeNodes(@Nonnull final SenlinService senlin, @Nonnull final OpenStackDiscoveryService discoveryService) {
        if (clusterMember.isActive()) {  //synchronization nodes available only at active server node
            final ImmutableSet<String> resources = ImmutableSet.copyOf(getResources());
            try {
                discoveryService.synchronizeNodes(senlin.node(), resources);
            } catch (final ResourceDiscoveryException | TimeoutException e) {
                getLogger().log(Level.SEVERE, "Failed to synchronize cluster nodes from OpenStack to SNAMP", e);
            } catch (final InterruptedException ignored) {

            }
        }
    }

    private void autoScaling(@Nonnull final SenlinService senlin, @Nonnull final OpenStackElasticityManager manager) {
        if (clusterMember.isActive())//scaling available only at active server node
            manager.performScaling(this, senlin);
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
        final SenlinService senlin = openStackClient.senlin();
        assert senlin != null;

        //the first, update nodes
        queryObject(DefaultResourceDiscoveryService.class)
                .flatMap(Convert.toType(OpenStackDiscoveryService.class))
                .ifPresent(discovery -> synchronizeNodes(senlin, discovery));
        //only after updating node we should collect health checks
        queryObject(DefaultHealthStatusProvider.class)
                .flatMap(Convert.toType(OpenStackHealthStatusProvider.class))
                .ifPresent(provider -> updateHealthStatus(senlin, provider));

        //force scaling
        queryObject(DefaultElasticityManager.class)
                .flatMap(Convert.toType(OpenStackElasticityManager.class))
                .ifPresent(manager -> autoScaling(senlin, manager));

        //OSAuthenticator.reAuthenticate();
        this.openStackClientToken.compareAndSet(openStackClientToken, openStackClient.getToken()); //if re-authentication forced
    }

    @Override
    public void scaleIn(final double castingVoteWeight, final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        scalingHappens(new ScaleInEvent(this, groupName) {
            private static final long serialVersionUID = -7114648391882166130L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public OpenStackSupervisor getSource() {
                return OpenStackSupervisor.this;
            }

            @Override
            public ImmutableMap<String, Double> getPolicyEvaluationSnapshot() {
                return evaluation;
            }
        });
    }

    @Override
    public void scaleOut(final double castingVoteWeight, final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        scalingHappens(new ScaleOutEvent(this, groupName) {
            private static final long serialVersionUID = 3268384740398039079L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public OpenStackSupervisor getSource() {
                return OpenStackSupervisor.this;
            }

            @Override
            public ImmutableMap<String, Double> getPolicyEvaluationSnapshot() {
                return evaluation;
            }
        });
    }

    @Override
    public void maxClusterSizeReached(final double castingVoteWeight, final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        scalingHappens(new MaxClusterSizeReachedEvent(this, groupName) {
            private static final long serialVersionUID = 4539949257630314963L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public OpenStackSupervisor getSource() {
                return OpenStackSupervisor.this;
            }

            @Override
            public Map<String, Double> getPolicyEvaluationSnapshot() {
                return evaluation;
            }
        });
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
        openStackClientToken.set(null);
        super.stop();
    }
}
