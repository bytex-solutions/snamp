package com.bytex.snamp.testing.supervision;

import com.bytex.snamp.concurrent.SpinWait;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Represents integration test for OpenStack supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.STUB_CONNECTOR, SnampFeature.OS_SUPERVISOR})
public final class OpenStackSupervisorTest extends AbstractSnampIntegrationTest {
    private static final String OS_AUTH_URL = "http://192.168.100.3:5000/v3";   //keystone V3
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "secret";
    private static final String GROUP_NAME = "os_nodes";

    private static void assumeThatDevStackIsAvailable(final URL authUrl) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) authUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            Assume.assumeTrue("Keystone is not OK", connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (final IOException e) {
            Assume.assumeNoException("DevStack is not available", e);
        }
    }

    private static SupervisorClient getSupervisor(final BundleContext context, final String groupName){
        return SupervisorClient.tryCreate(context, groupName).orElse(null);
    }

    private static boolean waitForResources(final HealthStatusProvider provider){
        return provider.getStatus().size() < 3;
    }

    @Test
    public void healthStatusTest() throws TimeoutException, InterruptedException {
        try(final SupervisorClient client = SpinWait.untilNull(getTestBundleContext(), GROUP_NAME, OpenStackSupervisorTest::getSupervisor, Duration.ofSeconds(3))){
            final HealthStatusProvider provider = client.queryObject(HealthStatusProvider.class).orElseThrow(AssertionError::new);
            //wait for resources discovery (expected 3 nodes)
            SpinWait.until(() -> waitForResources(provider), Duration.ofSeconds(10));
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        //check whether the VM with DevStack is available
        assumeThatDevStackIsAvailable(new URL(OS_AUTH_URL));
        super.beforeStartTest(context);
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        config.getResourceGroups().addAndConsume("os_nodes", group -> {
            group.setType("stub");
        });
        config.getSupervisors().addAndConsume(GROUP_NAME, supervisor -> {
            supervisor.setType("openstack");
            supervisor.put("authURL", OS_AUTH_URL);
            supervisor.put("userName", USERNAME);
            supervisor.put("password", PASSWORD);
            supervisor.put("checkNodes", Boolean.FALSE.toString());
            supervisor.getDiscoveryConfig().setConnectionStringTemplate("{first(addresses.private).addr}");
        });
    }
}
