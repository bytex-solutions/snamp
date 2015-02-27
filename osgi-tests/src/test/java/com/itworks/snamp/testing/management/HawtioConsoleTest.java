package com.itworks.snamp.testing.management;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import org.junit.Test;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 * @date 26.02.2015
 */
@SnampDependencies({SnampFeature.SNMP_CONNECTOR, SnampFeature.JMX_ADAPTER})
public class HawtioConsoleTest  extends AbstractSnampIntegrationTest {

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    /**
     * Simple test.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void simpleTest() throws InterruptedException {
        while(true) {
            Thread.sleep(1000);
        }
    }

    /**
     * Leave it empty.
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(AgentConfiguration config) {

    }
}
