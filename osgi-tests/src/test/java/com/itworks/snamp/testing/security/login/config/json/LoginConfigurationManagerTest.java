package com.itworks.snamp.testing.security.login.config.json;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.security.LoginConfigurationManager;
import com.itworks.snamp.security.auth.login.JsonConfiguration;
import com.itworks.snamp.testing.AbstractIntegrationTest;
import com.itworks.snamp.testing.SnampArtifact;
import org.junit.Test;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LoginConfigurationManagerTest extends AbstractIntegrationTest {
    public LoginConfigurationManagerTest(){
        super(SnampArtifact.CORLIB.getReference(),
                SnampArtifact.JAAS_CONFIG.getReference().start(),
                mavenBundle("org.apache.felix", "org.apache.felix.log", "1.0.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.0"),
                mavenBundle("org.apache.karaf.jaas", "org.apache.karaf.jaas.config", "3.0.2"),
                mavenBundle("org.apache.karaf.jaas", "org.apache.karaf.jaas.boot", "3.0.2"),
                mavenBundle("org.apache.karaf", "org.apache.karaf.util", "3.0.2"),
                mavenBundle("org.springframework.osgi", "spring-osgi-core", "1.2.1"),
                mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.api", "1.0.1"),
                mavenBundle("org.apache.aries.blueprint", "org.apache.aries.blueprint.core", "1.4.1"),
                mavenBundle("org.apache.aries.proxy", "org.apache.aries.proxy.api", "1.0.0"),
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.1.0"),
                mavenBundle("org.springframework", "spring-aop", "3.1.4.RELEASE"),
                mavenBundle("com.google.guava", "guava", "18.0"),
                mavenBundle("com.google.code.gson", "gson", "2.2.4"));
    }

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
}
