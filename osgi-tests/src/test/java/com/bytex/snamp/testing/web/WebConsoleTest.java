package com.bytex.snamp.testing.web;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.STANDARD_TOOLS})
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    private final TestAuthenticator authenticator;

    public WebConsoleTest(){
        authenticator = new TestAuthenticator();
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
