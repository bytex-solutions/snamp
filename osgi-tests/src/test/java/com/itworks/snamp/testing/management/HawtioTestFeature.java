package com.itworks.snamp.testing.management;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

@SnampDependencies({SnampFeature.SNMP_ADAPTER, SnampFeature.RSHELL_CONNECTOR})
public class HawtioTestFeature extends AbstractSnampIntegrationTest {


    @Test
    public void discoverMetadataTest() throws InterruptedException, ClassNotFoundException {
        final Class<?> cls = Class.forName("javax.servlet.Servlet");
        System.err.println(FrameworkUtil.getBundle(cls).getSymbolicName());
        while(true) Thread.sleep(10000);
    }

    @Override
    protected void setupTestConfiguration(AgentConfiguration config) {

    }
}
