package com.itworks.snamp.testing;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.ResourceAdapter;
import com.itworks.snamp.adapters.ResourceAdapterClient;
import com.itworks.snamp.adapters.ResourceAdapterEvent;
import com.itworks.snamp.adapters.ResourceAdapterEventListener;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.AbstractLicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
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
@SnampDependencies({SnampFeature.PLATFORM, SnampFeature.LICENSING})
@PropagateSystemProperties({
        SnampSystemProperties.JAAS_CONFIG_FILE,
        "java.security.egd",
        "com.sun.management.jmxremote.authenticate",
        "com.sun.management.jmxremote.port",
        "com.sun.management.jmxremote.ssl",
        SnampSystemProperties.LICENSING_FILE,
        "pax.exam.osgi.unresolved.fail",
        SnampSystemProperties.WEB_CONSOLE_HOST,
        SnampSystemProperties.WEB_CONSOLE_PORT
})
@ImportPackages("com.itworks.snamp;version=\"[1.0,2)\"")
public abstract class AbstractSnampIntegrationTest extends AbstractIntegrationTest {

    private static final class AdapterStartedSynchronizationEvent extends SynchronizationEvent<ResourceAdapter> implements ResourceAdapterEventListener {

        @Override
        public void adapterStarted(final ResourceAdapterEvent e) {
            fire(e.getSource());
        }

        @Override
        @MethodStub
        public void adapterStopped(final ResourceAdapterEvent e) {

        }
    }

    private static final class AdapterStoppedSynchronizationEvent extends SynchronizationEvent<ResourceAdapter> implements ResourceAdapterEventListener{
        @Override
        @MethodStub
        public void adapterStarted(final ResourceAdapterEvent e) {

        }

        @Override
        public void adapterStopped(final ResourceAdapterEvent e) {
            fire(e.getSource());
        }
    }

    private PersistentConfigurationManager configManager = null;
    @Inject
    private LicenseReader licenseReader = null;
    @Inject
    private ConfigurationAdmin configAdmin = null;

    protected AbstractSnampIntegrationTest(){
        super(new EnvironmentBuilder() {
            @Override
            public Collection<KarafFeaturesOption> getFeatures(final Class<? extends AbstractIntegrationTest> testType) {
                final Collection<KarafFeaturesOption> result = new LinkedList<>();
                for(final SnampDependencies deps: TestUtils.getAnnotations(testType, SnampDependencies.class))
                    for(final SnampFeature feature: deps.value())
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
            expandSystemPropertyFileName(SnampSystemProperties.LICENSING_FILE);
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

    private PersistentConfigurationManager getTestConfigurationManager() throws Exception{
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


    /**
     * Reads SNAMP configuration from temporary storage.
     * @return Deserialized SNAMP configuration.
     * @throws IOException
     */
    protected final AgentConfiguration readSnampConfiguration() throws Exception{
        return getTestConfigurationManager().getCurrentConfiguration();
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
    public final void prepare() throws Exception{
        beforeStartTest(getTestBundleContext());
        //read SNAMP configuration
        setupTestConfiguration(getTestConfigurationManager().getCurrentConfiguration());
        getTestConfigurationManager().save();
        //verify licensing engine
        assertNotNull(licenseReader);
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

    protected final <L extends AbstractLicenseLimitations> L getLicenseLimitation(final Class<L> limitationType, final Supplier<L> fallback){
        assertNotNull("Licensing service is not available.", licenseReader);
        return licenseReader.getLimitations(limitationType, fallback);
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

    protected static <V, E extends Exception> V syncWithAdapterStoppedEvent(final String adapterName,
                                                                            final ExceptionalCallable<V, E> handler,
                                                                            final TimeSpan timeout) throws E, TimeoutException, InterruptedException{
        final AdapterStoppedSynchronizationEvent synchronizer = new AdapterStoppedSynchronizationEvent();
        ResourceAdapterClient.addEventListener(adapterName, synchronizer);
        try{
            final V result = handler.call();
            synchronizer.getAwaitor().await(timeout);
            return result;
        }
        finally {
            ResourceAdapterClient.removeEventListener(adapterName, synchronizer);
        }
    }
}
