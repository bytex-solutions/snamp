package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.def.DefaultSupervisor;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.OS4JException;

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
        final OSClient<?> openStackClient = parser
                .parseAuthenticator(configuration)
                .setCloudProvider(parser.parseCloudProvider(configuration))
                .setEndpoint(parser.parseApiEndpoint(configuration))
                .setUserName(parser.parseUserName(configuration))
                .setPassword(parser.parsePassword(configuration))
                .setDomain(parser.parseDomain(configuration))
                .setProject(parser.parseProject(configuration))
                .authenticate();
        if (!openStackClient.supportsCompute()) {    //Compute is not supported. Shutting down.
            final String message = String.format("OpenStack installation %s doesn't support Compute service. Supervisor for group %s is not started", openStackClient.getEndpoint(), groupName);
            throw new OS4JException(message);
        }
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
