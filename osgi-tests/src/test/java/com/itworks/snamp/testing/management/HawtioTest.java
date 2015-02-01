package com.itworks.snamp.testing.management;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ThreadPoolConfig;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import org.junit.Test;
@SnampDependencies(SnampFeature.HAWTIO)
public class HawtioTest extends AbstractSnampIntegrationTest {

    @Test
    public void discoverMetadataTest() throws InterruptedException {
        Thread.sleep(10000000);
    }

    @Override
    protected void setupTestConfiguration(AgentConfiguration config) {

    }
}
