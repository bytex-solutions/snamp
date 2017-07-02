package com.bytex.snamp.testing.supervision;

import com.bytex.snamp.concurrent.SpinWait;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.moa.ReduceOperation;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.elasticity.policies.AbstractWeightedScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.AttributeBasedScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.HealthStatusBasedScalingPolicy;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.google.common.collect.Range;
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
    private static final String USERNAME = "admin";
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
        //TimeUnit.DAYS.sleep(1);
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
            group.getAttributes().addAndConsume("stag", attribute -> attribute.setAlternativeName("staggeringValue"));
            group.getAttributes().addAndConsume("i", attribute -> attribute.setAlternativeName("intValue"));
        });
        config.getSupervisors().addAndConsume(GROUP_NAME, supervisor -> {
            supervisor.setType("openstack");
            supervisor.put("authURL", OS_AUTH_URL);
            supervisor.put("userName", USERNAME);
            supervisor.put("password", PASSWORD);
            supervisor.put("checkNodes", Boolean.FALSE.toString());
            supervisor.getAutoScalingConfig().setEnabled(false);
            supervisor.getAutoScalingConfig().setMaxClusterSize(5);
            supervisor.getAutoScalingConfig().setMinClusterSize(1);
            supervisor.getAutoScalingConfig().setCooldownTime(Duration.ofSeconds(1));
            AbstractWeightedScalingPolicy policy = new AttributeBasedScalingPolicy("stag",
                    0.5D,
                    Range.closed(-5D, 5D),
                    Duration.ofSeconds(1),
                    ReduceOperation.MEAN,
                    true,
                    Duration.ZERO);
            supervisor.getAutoScalingConfig().getPolicies().addAndConsume("stagPolicy", policy::configureScriptlet);
            policy = new AttributeBasedScalingPolicy("i",
                    1D,
                    Range.all(),
                    Duration.ZERO,
                    ReduceOperation.MEAN,
                    false,
                    Duration.ZERO);
            supervisor.getAutoScalingConfig().getPolicies().addAndConsume("ipol", policy::configureScriptlet);
            policy = new HealthStatusBasedScalingPolicy(1D, MalfunctionStatus.Level.CRITICAL);
            supervisor.getAutoScalingConfig().getPolicies().addAndConsume("hs", policy::configureScriptlet);
            supervisor.getDiscoveryConfig().setConnectionStringTemplate("{first(addresses.private).addr}");
        });
    }
}
