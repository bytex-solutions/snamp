package com.itworks.snamp.testing;

import com.itworks.snamp.configuration.*;
import com.itworks.snamp.licensing.*;
import org.apache.commons.collections4.Factory;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.options.*;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.*;

import static com.itworks.snamp.configuration.ConfigurationManager.CONFIGURATION_FILE_PROPERTY;
import static com.itworks.snamp.licensing.LicenseReader.LICENSE_FILE_PROPERTY;
import static org.ops4j.pax.exam.CoreOptions.bundle;

/**
 * Represents an abstract class for all SNAMP-based integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampIntegrationTest extends AbstractIntegrationTest {

    /**
     * Represents relative path to the test license file.
     */
    private static final String TEST_LICENCE_FILE = "unlimited.lic";

    @Inject
    private ConfigurationManager configManager = null;
    @Inject
    private LicenseReader licenseReader = null;

    static {
        try {
            final File configFile = File.createTempFile("snamp-config", ".xml");
            //noinspection ResultOfMethodCallIgnored
            configFile.delete();
            System.setProperty(CONFIGURATION_FILE_PROPERTY, configFile.getAbsolutePath());
            final File licenseFile = new File(System.getProperty(LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE));
            if(!licenseFile.exists())
                throw new IOException("License file for tests is missed.");
            else System.setProperty(LICENSE_FILE_PROPERTY, licenseFile.getAbsolutePath());
        }
        catch (final IOException e) {
            fail(e.getMessage());
        }
    }

    private static AbstractProvisionOption<?>[] buildDependencies(AbstractProvisionOption<?>[] deps){
        deps = concat(deps, bundle("http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.log-1.0.1.jar"),
                bundle("http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.eventadmin-1.4.2.jar"));
        return concat(SnampArtifact.makeBasicSet(), deps);
    }

    protected AbstractSnampIntegrationTest(final AbstractProvisionOption<?>... deps){
        super(buildDependencies(deps));
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
    protected final AgentConfiguration readSnampConfiguration() throws IOException{
        assertNotNull(configManager);
        return configManager.getCurrentConfiguration();
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
        assertNotNull(configManager);
        setupTestConfiguration(configManager.getCurrentConfiguration());
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
        configManager.getCurrentConfiguration().clear();
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
                                                                                           final Factory<L> fallback) throws LicensingException{
        final L lims = getLicenseLimitation(descriptor, fallback);
        assertNotNull(String.format("Limitation %s is not described in license", descriptor), lims);
        lims.verify(limitationName, actualValue);
    }

    protected final <L extends AbstractLicenseLimitations> L getLicenseLimitation(final Class<L> limitationType, final Factory<L> fallback){
        assertNotNull("Licensing service is not available.", licenseReader);
        return licenseReader.getLimitations(limitationType, fallback);
    }
}
