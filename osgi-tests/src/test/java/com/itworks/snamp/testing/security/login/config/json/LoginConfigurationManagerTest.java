package com.itworks.snamp.testing.security.login.config.json;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.security.LoginConfigurationManager;
import com.itworks.snamp.security.auth.login.json.JsonConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LoginConfigurationManagerTest extends AbstractSnampIntegrationTest {


    @Test
    public void jaasTest() throws InterruptedException {
        final ServiceReferenceHolder<LoginConfigurationManager> managerRef = new ServiceReferenceHolder<>(getTestBundleContext(), LoginConfigurationManager.class);
        try{
            JsonConfiguration conf = new JsonConfiguration();
            managerRef.getService().dumpConfiguration(conf);
            assertEquals(2, conf.size());
        }
        finally {
            managerRef.release(getTestBundleContext());
        }
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
