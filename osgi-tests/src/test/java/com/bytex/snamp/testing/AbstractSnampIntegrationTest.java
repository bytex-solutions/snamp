package com.bytex.snamp.testing;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.*;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.PersistentConfigurationManager;
import com.bytex.snamp.configuration.SerializableAgentConfiguration;
import com.bytex.snamp.SpecialUse;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

/**
 * Represents an abstract class for all SNAMP-based integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.PLATFORM})
@PropagateSystemProperties({
        SnampSystemProperties.JAAS_CONFIG_FILE,
        "java.security.egd",
        "com.sun.management.jmxremote.authenticate",
        "com.sun.management.jmxremote.port",
        "com.sun.management.jmxremote.ssl",
        "pax.exam.osgi.unresolved.fail"
})
@ImportPackages("com.bytex.snamp;version=\"[1.0,2)\"")
public abstract class AbstractSnampIntegrationTest extends AbstractIntegrationTest {

    private static final class AdapterStartedSynchronizationEvent extends SynchronizationEvent<ResourceAdapter> implements ResourceAdapterEventListener {

        @Override
        public void handle(final ResourceAdapterEvent e) {
            if(e instanceof ResourceAdapterStartedEvent)
                fire(e.getSource());
        }
    }

    private static final class AdapterUpdatedSynchronizationEvent extends SynchronizationEvent<ResourceAdapter> implements ResourceAdapterEventListener{

        @Override
        public void handle(final ResourceAdapterEvent e) {
            if(e instanceof ResourceAdapterUpdatedEvent)
                fire(e.getSource());
        }
    }

    private PersistentConfigurationManager configManager = null;
    @Inject
    @SpecialUse
    private ConfigurationAdmin configAdmin = null;

    protected AbstractSnampIntegrationTest(){
        super(new EnvironmentBuilder() {
            @Override
            public Collection<KarafFeaturesOption> getFeatures(final Class<? extends AbstractIntegrationTest> testType) {
                final Collection<KarafFeaturesOption> result = new LinkedList<>();
                for (final SnampDependencies deps : TestUtils.getAnnotations(testType, SnampDependencies.class))
                    for (final SnampFeature feature : deps.value())
                        try {
                            result.add(new SnampFeatureOption(feature));
                        } catch (final MalformedURLException e) {
                            fail(e.getMessage());
                        }
                return result;
            }
        });
        //WORKAROUND for system properties with relative path
        if(!isInTestContainer()){
            expandSystemPropertyFileName(SnampSystemProperties.JAAS_CONFIG_FILE);
        }
    }

    private static void expandSystemPropertyFileName(final String propertyName) {
        String fileName = System.getProperty(propertyName);
        if (fileName != null && fileName.startsWith("./")) {
            fileName = fileName.substring(2);
            fileName = getPathToFileInProjectRoot(fileName);
            System.setProperty(propertyName, fileName);
        }
    }

    private PersistentConfigurationManager getTestConfigurationManager() throws IOException{
        if(configManager == null){
            configManager = new PersistentConfigurationManager(configAdmin);
            configManager.load();
        }
        return configManager;
    }

    /**
     * Creates a new configuration for running this test.
     * @param config The configuration to set.
     */
    protected abstract void setupTestConfiguration(final AgentConfiguration config);

    protected final <E extends Throwable> void processConfiguration(final Consumer<? super SerializableAgentConfiguration, E> handler,
                                                                    final boolean saveChanges) throws E, IOException {
        getTestConfigurationManager().processConfiguration(handler, saveChanges);
    }

    protected void beforeStartTest(final BundleContext context) throws Exception{

    }

    protected void afterStartTest(final BundleContext context) throws Exception{

    }

    /**
     * Saves SNAMP configuration into the output stream.
     * @throws IOException
     */
    @Before
    public final void prepare() throws Exception {
        beforeStartTest(getTestBundleContext());
        //read SNAMP configuration
        setupTestConfiguration(getTestConfigurationManager().getCurrentConfiguration());
        getTestConfigurationManager().save();
        afterStartTest(getTestBundleContext());
    }

    protected void beforeCleanupTest(final BundleContext context) throws Exception{

    }

    protected void afterCleanupTest(final BundleContext context) throws Exception{

    }

    @After
    public final void cleanup() throws Exception{
        beforeCleanupTest(getTestBundleContext());
        getTestConfigurationManager().getCurrentConfiguration().clear();
        getTestConfigurationManager().save();
        afterCleanupTest(getTestBundleContext());
    }

    protected static <V, E extends Exception> V syncWithAdapterStartedEvent(final String adapterName,
                                                                          final ExceptionalCallable<V, E> handler,
                                                                          final TimeSpan timeout) throws E, TimeoutException, InterruptedException {
        final AdapterStartedSynchronizationEvent synchronizer = new AdapterStartedSynchronizationEvent();
        ResourceAdapterClient.addEventListener(adapterName, synchronizer);
        try {
            final V result = handler.call();
            synchronizer.getAwaitor().await(timeout);
            return result;
        }
        finally {
            ResourceAdapterClient.removeEventListener(adapterName, synchronizer);
        }
    }

    protected static <V, E extends Exception> V syncWithAdapterUpdatedEvent(final String adapterName,
                                                                            final ExceptionalCallable<V, E> handler,
                                                                            final TimeSpan timeout) throws E, TimeoutException, InterruptedException {
        final AdapterUpdatedSynchronizationEvent synchronizer = new AdapterUpdatedSynchronizationEvent();
        ResourceAdapterClient.addEventListener(adapterName, synchronizer);
        try {
            final V result = handler.call();
            synchronizer.getAwaitor().await(timeout);
            return result;
        }
        finally {
            ResourceAdapterClient.removeEventListener(adapterName, synchronizer);
        }
    }
}
