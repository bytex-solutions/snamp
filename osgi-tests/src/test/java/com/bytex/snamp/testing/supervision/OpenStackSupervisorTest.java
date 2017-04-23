package com.bytex.snamp.testing.supervision;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents integration test for OpenStack supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.OS_SUPERVISOR})
public final class OpenStackSupervisorTest extends AbstractSnampIntegrationTest {
    private static final String OS_AUTH_URL = "http://192.168.100.3:5000/v3";   //keystone V3
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "secret";

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

    @Test
    public void simpleTest() throws InterruptedException {
        Thread.sleep(10_000_000);
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
            group.setType("jmx");
        });
        config.getSupervisors().addAndConsume("os_nodes", supervisor -> {
            supervisor.setType("openstack");
            supervisor.put("authURL", OS_AUTH_URL);
            supervisor.put("userName", USERNAME);
            supervisor.put("password", PASSWORD);
        });
    }
}
