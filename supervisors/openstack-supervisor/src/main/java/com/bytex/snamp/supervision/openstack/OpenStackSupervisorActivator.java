package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.supervision.def.DefaultSupervisorActivator;
import com.bytex.snamp.supervision.elasticity.policies.InvalidScalingPolicyException;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.api.senlin.SenlinClusterService;
import org.openstack4j.model.senlin.Cluster;
import org.openstack4j.openstack.OSFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents activator of {@link OpenStackSupervisor}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class OpenStackSupervisorActivator extends DefaultSupervisorActivator<OpenStackSupervisor> {
    private static final class OpenStackSupervisorLifecycleManager extends DefaultSupervisorLifecycleManager<OpenStackSupervisor>{
        @Override
        protected OpenStackSupervisorDescriptionProvider getDescriptionProvider() {
            return OpenStackSupervisorDescriptionProvider.getInstance();
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

        @Override
        protected void configureDiscovery(final OpenStackSupervisor supervisor, final SupervisorInfo.ResourceDiscoveryInfo configuration) {
            final String connectionStringTemplate = configuration.getConnectionStringTemplate();
            if (!isNullOrEmpty(connectionStringTemplate))
                supervisor.enableAutoDiscovery(configuration.getConnectionStringTemplate());
        }

        @Override
        protected void configureElasticity(final OpenStackSupervisor supervisor, final SupervisorInfo.AutoScalingInfo configuration) throws InvalidScalingPolicyException {
            if (configuration.isEnabled())
                supervisor.enableAutoScaling();
            super.configureElasticity(supervisor, configuration);
        }

        @Nonnull
        @Override
        protected OpenStackSupervisor createSupervisor(@Nonnull final String groupName,
                                                       @Nonnull final Map<String, String> configuration) throws Exception {
            final OpenStackSupervisorDescriptionProvider parser = getDescriptionProvider();
            final OSClient.OSClientV3 openStackClient = OSFactory.builderV3()
                    .provider(parser.parseCloudProvider(configuration))
                    .endpoint(parser.parseApiEndpoint(configuration))
                    .scopeToProject(parser.parseProject(configuration), parser.parseProjectDomain(configuration))
                    .credentials(parser.parseUserName(configuration), parser.parsePassword(configuration), parser.parseUserDomain(configuration))
                    .authenticate();
            //Obtain cluster ID
            final String clusterID = parser.parseClusterID(configuration)
                    .orElseGet(() -> getClusterIdByName(openStackClient.senlin().cluster(), groupName));
            final OpenStackSupervisor supervisor = new OpenStackSupervisor(groupName, openStackClient, clusterID);
            supervisor.getHealthStatusProvider().setCheckNodes(parser.checkNodes(configuration));
            return supervisor;
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public OpenStackSupervisorActivator(){
        super(new OpenStackSupervisorLifecycleManager());
    }
}
