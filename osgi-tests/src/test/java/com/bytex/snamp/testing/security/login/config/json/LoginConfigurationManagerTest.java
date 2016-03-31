package com.bytex.snamp.testing.security.login.config.json;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.security.LoginConfigurationManager;
import com.bytex.snamp.security.auth.login.json.JsonConfiguration;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;

import javax.inject.Inject;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class LoginConfigurationManagerTest extends AbstractSnampIntegrationTest {
    @Inject
    @SpecialUse
    private LoginConfigurationManager manager = null;

    @Test
    public void jaasTest() throws InterruptedException, TimeoutException, InvalidSyntaxException {
        assertNotNull(manager);
        final JsonConfiguration conf = new JsonConfiguration();
        manager.dumpConfiguration(conf);
        assertEquals(2, conf.size());
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
