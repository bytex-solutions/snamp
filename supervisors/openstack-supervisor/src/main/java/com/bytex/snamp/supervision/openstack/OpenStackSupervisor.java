package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.def.DefaultSupervisor;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.OS4JException;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.api.types.ServiceType;

import java.time.Duration;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor {

    OpenStackSupervisor(final String groupName) {
        super(groupName);
    }

    @Override
    protected Duration getCheckPeriod(final SupervisorInfo configuration) {
        return OpenStackSupervisorDescriptionProvider.getInstance().parseCheckPeriod(configuration);
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
        final OpenStackSupervisorDescriptionProvider parser = OpenStackSupervisorDescriptionProvider.getInstance();
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

        final boolean enableElastMan = parser.isElasticityManagementEnabled(configuration);
        super.start(configuration);
    }

    private void stopElasticityManager() throws Exception{

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
        Utils.closeAll(() -> super.stop(), this::stopElasticityManager);
    }
}
