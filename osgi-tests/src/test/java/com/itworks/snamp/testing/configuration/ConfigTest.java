package com.itworks.snamp.testing.configuration;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.testing.AbstractIntegrationTest;
import com.itworks.snamp.testing.SnampArtifact;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ConfigTest extends AbstractIntegrationTest {
    public ConfigTest(){
        super(mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.0"),
                mavenBundle("org.apache.felix", "org.apache.felix.log", "1.0.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.2"),
                mavenBundle("com.google.guava", "guava", "18.0"),
                SnampArtifact.CORLIB.getReference());
    }

    private static final class Factory implements ManagedServiceFactory{
        private static final String PID = "tests.factorypid";
        /**
         * Return a descriptive name of this factory.
         *
         * @return the name for the factory, which might be localized
         */
        @Override
        public String getName() {
            return PID;
        }

        @Override
        public void updated(final String pid, final Dictionary<String, ?> properties) throws ConfigurationException {
            System.out.println("Updated");
        }

        @Override
        public void deleted(final String pid) {
            System.out.println("Deleted");
        }
    }

    @Test
    public void testConfig() throws IOException, InterruptedException {
        final BundleContext context = getTestBundleContext();
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef = new ServiceReferenceHolder<>(context, ConfigurationAdmin.class);
        try{
            final ConfigurationAdmin admin = adminRef.getService();
            final Configuration config = admin.createFactoryConfiguration(Factory.PID);
            final Hashtable<String, String> table = new Hashtable<>(2);
            table.put("prop1", "value1");
            table.put("prop2", "value2");
            config.update(table);
            final Hashtable<String, String> props = new Hashtable<>(1);
            props.put(Constants.SERVICE_PID, Factory.PID);
            final ServiceRegistration<ManagedServiceFactory> fact = context.registerService(ManagedServiceFactory.class, new Factory(), props);
            fact.unregister();
        }
        finally {
            adminRef.clear(context);
        }
    }
}
