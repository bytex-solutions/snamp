package com.itworks.snamp.testing.management;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ThreadPoolConfig;
import com.itworks.snamp.testing.*;
import org.junit.Test;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.osgi.framework.FrameworkUtil;

import java.util.ArrayList;
import java.util.Collection;

@SystemProperties({"hawtio.config.cloneOnStartup=false", "hawtio.config.pullOnStartup=false"})
public class HawtioTest extends AbstractIntegrationTest {

    public HawtioTest() {
        super(new EnvironmentBuilder() {
            @Override
            public Collection<KarafFeaturesOption> getFeatures(Class<? extends AbstractIntegrationTest> testType) {
                return new ArrayList<KarafFeaturesOption>(){{add(new KarafFeaturesOption(
                        "mvn:io.hawt/hawtio-karaf/1.4.46/xml/features","hawtio"));}};
            }
        });
    }

    @Test
    public void discoverMetadataTest() throws InterruptedException, ClassNotFoundException {
        final Class<?> cls = Class.forName("javax.servlet.Servlet");
        System.err.println(FrameworkUtil.getBundle(cls).getSymbolicName());
        while(true) Thread.sleep(10000);
    }
}
