package com.bytex.snamp.testing.web;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.STANDARD_TOOLS})
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    private static final String WS_ENDPOINT = "ws://localhost:8181/snamp/console/events";
    private final TestAuthenticator authenticator;

    public WebConsoleTest(){
        authenticator = new TestAuthenticator();
    }

    private WebSocketClient createWebSocketClient(){
        return null;
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
