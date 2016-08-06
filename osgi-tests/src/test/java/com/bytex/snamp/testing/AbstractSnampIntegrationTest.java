package com.bytex.snamp.testing;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.adapters.*;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.FeatureConfiguration;
import static com.bytex.snamp.configuration.ConfigurationManager.ConfigurationProcessor;

/**
 * Represents an abstract class for all SNAMP-based integration tests.
 * @author Roman Sakno
 * @version 2.0
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

    private static final class AdapterStartedSynchronizationEvent extends CompletableFuture<ResourceAdapter> implements ResourceAdapterEventListener {

        @Override
        public void handle(final ResourceAdapterEvent e) {
            if(e instanceof ResourceAdapterStartedEvent)
                complete(e.getSource());
        }
    }

    private static final class AdapterUpdatedSynchronizationEvent extends CompletableFuture<ResourceAdapter> implements ResourceAdapterEventListener{

        @Override
        public void handle(final ResourceAdapterEvent e) {
            if(e instanceof ResourceAdapterUpdatedEvent)
                complete(e.getSource());
        }
    }
    private static final EnvironmentBuilder SNAMP_ENV_BUILDER = new EnvironmentBuilder() {
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
            for(final MavenDependencies deps: TestUtils.getAnnotations(testType, MavenDependencies.class))
                for(final MavenFeature feature: deps.value())
                    result.add(new KarafFeaturesOption(new MavenArtifactUrlReference().artifactId(feature.artifactId()).groupId(feature.groupId()).version(feature.version()).type("xml").classifier("features"), feature.name()));
            return result;
        }
    };

    @Inject
    @SpecialUse
    private ConfigurationManager configAdmin = null;

    protected AbstractSnampIntegrationTest(){
        super(SNAMP_ENV_BUILDER);
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

    /**
     * Creates a new configuration for running this test.
     * @param config The configuration to set.
     */
    protected abstract void setupTestConfiguration(final AgentConfiguration config);

    protected final <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler) throws E, IOException {
        configAdmin.processConfiguration(handler);
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
        processConfiguration(config -> {
            setupTestConfiguration(config);
            return true;
        });
        afterStartTest(getTestBundleContext());
    }

    protected void beforeCleanupTest(final BundleContext context) throws Exception{

    }

    protected void afterCleanupTest(final BundleContext context) throws Exception{

    }

    @After
    public final void cleanup() throws Exception{
        beforeCleanupTest(getTestBundleContext());
        processConfiguration(config -> {
            config.clear();
            return true;
        });
        afterCleanupTest(getTestBundleContext());
    }

    protected static <V> V syncWithAdapterStartedEvent(final String adapterName,
                                                       final Callable<? extends V> handler,
                                                       final Duration timeout) throws Exception {
        final AdapterStartedSynchronizationEvent synchronizer = new AdapterStartedSynchronizationEvent();
        ResourceAdapterClient.addEventListener(adapterName, synchronizer);
        try {
            final V result = handler.call();
            assertNotNull(synchronizer.get(timeout.toNanos(), TimeUnit.NANOSECONDS));
            return result;
        }
        catch (final ExecutionException e){
            fail(e.getCause().getMessage());
            return null;
        }
        finally {
            ResourceAdapterClient.removeEventListener(adapterName, synchronizer);
        }
    }

    protected static <V> V syncWithAdapterUpdatedEvent(final String adapterName,
                                                       final Callable<? extends V> handler,
                                                       final Duration timeout) throws Exception {
        final AdapterUpdatedSynchronizationEvent synchronizer = new AdapterUpdatedSynchronizationEvent();
        ResourceAdapterClient.addEventListener(adapterName, synchronizer);
        try {
            final V result = handler.call();
            assertNotNull(synchronizer.get(timeout.toNanos(), TimeUnit.NANOSECONDS));
            return result;
        } catch (final ExecutionException e){
            fail(e.getCause().getMessage());
            return null;
        }
        finally {
            ResourceAdapterClient.removeEventListener(adapterName, synchronizer);
        }
    }

    protected static void setFeatureName(final FeatureConfiguration feature, final String name){
        feature.setAlternativeName(name);
    }
}
