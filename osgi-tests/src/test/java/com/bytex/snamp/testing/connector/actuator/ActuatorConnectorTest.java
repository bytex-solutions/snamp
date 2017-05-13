package com.bytex.snamp.testing.connector.actuator;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.ResourceSubsystemDownStatus;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Assume;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies(SnampFeature.ACTUATOR_CONNECTOR)
public final class ActuatorConnectorTest extends AbstractResourceConnectorTest {
    private static final String CONNECTOR_TYPE = "actuator";
    private static final String ACTUATOR_URL = "http://localhost:9233/";

    public ActuatorConnectorTest() {
        super(CONNECTOR_TYPE, ACTUATOR_URL, ImmutableMap.of(
                "userName", "admin",
                "password", "secret",
                "authentication", "basic",
                "smartMode", "true"
        ));
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws MalformedURLException {
        final URL url = new URL(ACTUATOR_URL + "info.json");
        try {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            Assume.assumeTrue("Spring App is not OK", connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (final IOException e) {
            Assume.assumeNoException("Spring App is not available", e);
        }
    }

    @Test
    public void attributesTest() throws JMException {
        testAttribute("systemload.average", TypeToken.of(Double.class), 10D, (l, r) -> true, true);
        testAttribute("mem.free", TypeToken.of(Long.class), 10L, (l, r) -> true, true);
        testAttribute("httpsessions.active", TypeToken.of(Long.class), 0L, true);
    }

    @Test
    public void healthStatusTest() throws JMException{
        final ManagedResourceConnector connector = getManagementConnector();
        try{
            assertTrue(connector.queryObject(HealthCheckSupport.class).map(HealthCheckSupport::getStatus).orElseGet(OkStatus::new) instanceof ResourceSubsystemDownStatus);
        } finally {
            releaseManagementConnector();
        }
    }
}
