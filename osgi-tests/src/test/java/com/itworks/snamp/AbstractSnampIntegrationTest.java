package com.itworks.snamp;

import com.itworks.snamp.configuration.*;
import com.itworks.snamp.licensing.*;
import org.junit.Before;
import org.ops4j.pax.exam.options.*;

import javax.inject.Inject;
import java.io.*;

import static com.itworks.snamp.configuration.ConfigurationManager.CONFIGURATION_FILE_PROPERTY;
import static com.itworks.snamp.licensing.LicenseReader.LICENSE_FILE_PROPERTY;

/**
 * Represents an abstract class for all SNAMP-based integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampIntegrationTest extends AbstractIntegrationTest {
    protected static final String SNAMP_GROUP_ID = "com.itworks.snamp";

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

    protected AbstractSnampIntegrationTest(final AbstractProvisionOption<?>... deps){
        super(concat(SnampArtifact.makeBasicSet(), deps));
    }

    /**
     * Creates a new configuration for running this test.
     * @return A new SNAMP configuration used for executing SNAMP bundles.
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

    /**
     * Saves SNAMP configuration into the output stream.
     * @throws IOException
     */
    @Before
    public final void prepare() throws IOException{
        //read SNAMP configuration
        assertNotNull(configManager);
        setupTestConfiguration(configManager.getCurrentConfiguration());
        //verify licensing engine
        assertNotNull(licenseReader);
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
                                                                                           final A actualValue) throws LicensingException{
        assertNotNull("Licensing service is not available.", licenseReader);
        final L lims = licenseReader.getLimitations(descriptor, NullProvider.<L>get());
        assertNotNull(String.format("Limitation %s is not described in license", descriptor), lims);
        lims.verify(limitationName, actualValue);
    }
}
