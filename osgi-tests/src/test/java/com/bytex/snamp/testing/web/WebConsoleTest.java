package com.bytex.snamp.testing.web;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.STANDARD_TOOLS})
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    private static final String WS_ENDPOINT = "ws://localhost:8181/snamp/console/events";
    private final TestAuthenticator authenticator;
    private WebSocketClient client;

    public WebConsoleTest(){
        authenticator = new TestAuthenticator();
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        client = new WebSocketClient();
        client.start();
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        client.stop();
        client = null;
    }

    private <E extends Exception> void runTest(final Acceptor<? super Session, E> testBody) throws Exception {
        try(final Session session = client.connect(null, new URI(WS_ENDPOINT)).get(10, TimeUnit.SECONDS)){

        } finally {
            client.stop();
        }
    }

    @Test
    public void logNotificationTest(){

    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }
}
