package com.bytex.snamp.testing.web;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.testing.PropagateSystemProperties;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

/**
 * The type Web console test.
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({
        SnampFeature.WRAPPED_LIBS,
        SnampFeature.HTTP_GATEWAY,
        SnampFeature.GROOVY_GATEWAY,
        SnampFeature.NAGIOS_GATEWAY,
        SnampFeature.NRDP_GATEWAY,
        SnampFeature.SSH_GATEWAY,
        SnampFeature.STANDARD_TOOLS,
        SnampFeature.GROOVY_CONNECTOR,
        SnampFeature.COMPOSITE_CONNECTOR,
        SnampFeature.JMX_CONNECTOR,
        SnampFeature.STUB_CONNECTOR,
        SnampFeature.HTTP_ACCEPTOR
})
@PropagateSystemProperties(AbstractWebTest.DUMMY_TEST_PROPERTY)
public final class EmptyWebConsoleTest extends AbstractWebTest {

    @Test
    public void emptyDummyTest() throws InterruptedException, URISyntaxException, IOException, ResourceDiscoveryException, TimeoutException {
        Assume.assumeTrue("Dummy test for webconsole is disabled. Please check the profile if needed", isDummyTestEnabled());
        Thread.sleep(100000000000L);
    }


    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {}
}
