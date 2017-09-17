package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.def.DefaultElasticityManager;
import com.bytex.snamp.supervision.def.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.def.DefaultSupervisor;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.openstack.discovery.OpenStackDiscoveryService;
import com.bytex.snamp.supervision.openstack.elasticity.OpenStackElasticityManager;
import com.bytex.snamp.supervision.openstack.elasticity.OpenStackScalingEvaluationContext;
import com.bytex.snamp.supervision.openstack.health.OpenStackHealthStatusProvider;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinService;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.openstack.OSFactory;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor implements OpenStackScalingEvaluationContext {
    private static final class OSClientFactory extends AtomicReference<Token> implements SafeCloseable {
        private static final long serialVersionUID = -1142857437167027233L;

        OSClientFactory(final OSClientV3 client) {
            super(client.getToken());
        }

        OSClientV3 createClient() {
            final Token token = get();
            return token == null ? null : OSFactory.clientFromToken(token);
        }

        @Override
        public void close() {
            set(null);
        }
    }

    private final OSClientFactory openStackClientFactory;
    private final String clusterID;
    private final Logger logger;
    private final OpenStackHealthStatusProvider healthStatusProvider;
    private OpenStackDiscoveryService discoveryService;
    private OpenStackElasticityManager elasticityManager;

    OpenStackSupervisor(final String groupName,
                        final OSClientV3 openStackClient,
                        final String clusterID) {
        super(groupName);
        openStackClientFactory = new OSClientFactory(openStackClient);
        this.clusterID = clusterID;
        logger = LoggerProvider.getLoggerForObject(this);
        healthStatusProvider = new OpenStackHealthStatusProvider(clusterID);
    }

    void enableAutoDiscovery(final String connectionStringTemplate) {
        if(isNullOrEmpty(connectionStringTemplate))
            throw new IllegalStateException("Connection string template cannot be empty");
        else if(discoveryService == null)
            discoveryService = new OpenStackDiscoveryService(groupName, clusterID, connectionStringTemplate);
        else
            throw new IllegalStateException("Automatic discovery is already enabled");
    }

    @Override
    @Aggregation
    protected DefaultResourceDiscoveryService getDiscoveryService() {
        return discoveryService == null ? super.getDiscoveryService() : discoveryService;
    }

    void enableAutoScaling(){
        if(elasticityManager == null)
            elasticityManager = new OpenStackElasticityManager(clusterID);
        else
            throw new IllegalStateException("Automatic scaling is already enabled");
    }

    @Override
    @Aggregation
    protected DefaultElasticityManager getElasticityManager() {
        return elasticityManager == null ? super.getElasticityManager() : elasticityManager;
    }

    @Override
    @Aggregation
    protected OpenStackHealthStatusProvider getHealthStatusProvider() {
        return healthStatusProvider;
    }

    private static boolean isClusteringSupported(final OSClientV3 client){
        return client.getSupportedServices().contains(ServiceType.CLUSTERING);
    }

    @Override
    protected void start() {
        final OSClientV3 openStackClient = openStackClientFactory.createClient();
        assert openStackClient != null;
        if (!isClusteringSupported(openStackClient)) {    //Compute is not supported. Shutting down.
            final String message = String.format("OpenStack installation %s doesn't support clustering via Senlin. Supervisor for group %s is not started", openStackClient.getEndpoint(), groupName);
            throw new OS4JException(message);
        }
        final Cluster cluster = openStackClient.senlin().cluster().get(clusterID);
        if (cluster == null)
            throw new OS4JException(String.format("Cluster with ID %s is not registered in Senlin", clusterID));
        else
            logger.info(String.format("Cluster %s is associated with group %s. Cluster status: %s(%s)",
                    clusterID,
                    groupName,
                    cluster.getStatus(),
                    cluster.getStatusReason()));
        super.start();
    }

    /**
     * Executes automatically using scheduling time.
     */
    @Override
    protected void supervise() {
        final OSClientV3 openStackClient = openStackClientFactory.createClient();
        if (openStackClient == null) {
            logger.warning(String.format("OpenStack client for group %s is signed out", groupName));
            return;
        }
        final SenlinService senlin = openStackClient.senlin();
        final BundleContext context = Utils.getBundleContextOfObject(this);
        assert senlin != null;
        final boolean activeNode = ClusterMember.get(context).isActive();
        //synchronization nodes available only at active server node
        if (activeNode && discoveryService != null)
            try {
                if (!discoveryService.synchronizeNodes(senlin.node(), getResources()))
                    return;
            } catch (final ResourceDiscoveryException e) {
                logger.log(Level.SEVERE, "Failed to synchronize cluster nodes between OpenStack and SNAMP", e);
                return;
            }
        //only after updating node we should collect health checks
        healthStatusProvider.updateStatus(context, senlin, getResources(), this);
        //and then perform scaling
        if (activeNode && elasticityManager != null)
            elasticityManager.performScaling(this, senlin);
        //OSAuthenticator.reAuthenticate();
        openStackClientFactory.set(openStackClient.getToken()); //if re-authentication forced
    }

    @Override
    public void reportScaleIn(final Map<String, Double> policyEvaluation) {
        scaleIn(policyEvaluation);
    }

    @Override
    public void reportScaleOut(final Map<String, Double> policyEvaluation) {
        scaleOut(policyEvaluation);
    }

    @Override
    public void reportMaxClusterSizeReached(final Map<String, Double> policyEvaluation) {
        maxClusterSizeReached(policyEvaluation);
    }

    @Override
    public void close() throws Exception {
        openStackClientFactory.close();
        super.close();
    }
}
