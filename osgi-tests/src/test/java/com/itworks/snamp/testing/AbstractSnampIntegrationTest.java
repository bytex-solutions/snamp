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
import com.itworks.snamp.licensing.LicensingException;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.licensing.LicenseReader.LICENSE_FILE_PROPERTY;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents an abstract class for all SNAMP-based integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerClass.class)
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

    /**
     * Represents relative path to the test license file.
     */
    private static final String TEST_LICENCE_FILE = "unlimited.lic";

    private PersistentConfigurationManager configManager = null;
    @Inject
    private LicenseReader licenseReader = null;
    @Inject
    private ConfigurationAdmin configAdmin = null;

    static {
        try {
            final File licenseFile = new File(System.getProperty(LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE));
            if(!licenseFile.exists())
                throw new IOException("License file for tests is missed.");
            else System.setProperty(LICENSE_FILE_PROPERTY, licenseFile.getAbsolutePath());
        }
        catch (final IOException e) {
            fail(e.getMessage());
        }
    }

    private static AbstractProvisionOption<?>[] buildDependencies(AbstractProvisionOption<?>[] deps) {
        deps = concat(deps, mavenBundle("org.apache.felix", "org.apache.felix.log", "1.0.1"),
                mavenBundle("org.apache.felix", "org.apache.felix.eventadmin", "1.4.2"),
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.8.0"),
                mavenBundle("com.google.guava", "guava", "18.0"));
        return concat(SnampArtifact.makeBasicSet(), deps);
    }

    protected AbstractSnampIntegrationTest(final AbstractProvisionOption<?>... deps){
        super(buildDependencies(deps));
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

    /**
     * Verifies some limitation from SNAMP license.
     * @param descriptor
     * @param limitationName
     * @param actualValue
     * @param <L>
     * @param <A>
     * @throws LicensingException
     */
    protected final <L extends AbstractLicenseLimitations, A> void verifyLicenseLimitation(final Class<L> descriptor,
                                                                                           final String limitationName,
                                                                                           final A actualValue,
                                                                                           final Supplier<L> fallback) throws LicensingException{
        final L lims = getLicenseLimitation(descriptor, fallback);
        assertNotNull(String.format("Limitation %s is not described in license", descriptor), lims);
        lims.verify(limitationName, actualValue);
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
