package com.itworks.snamp.testing.licensing;

import com.itworks.snamp.licensing.LicensingException;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.internal.MethodStub;
import com.itworks.snamp.testing.licensing.limitations.TestLicenseLimitation;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class LicenseReaderTest extends AbstractSnampIntegrationTest {
    /**
     * Creates a new configuration for running this test.
     *
     * @param config
     * @return A new SNAMP configuration used for executing SNAMP bundles.
     */
    @Override
    @MethodStub
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }

    @Test
    public final void successVerificationTest(){
        final TestLicenseLimitation lims = getLicenseLimitation(TestLicenseLimitation.class,
                TestLicenseLimitation.fallbackFactory);
        assertNotNull(lims);
        lims.verifyMaxAttributeCount(10);
        lims.verifyMaxInstanceCount(10);
    }

    @Test()
    public final void faultVerificationTest(){
        final TestLicenseLimitation lims = getLicenseLimitation(TestLicenseLimitation.class,
                TestLicenseLimitation.fallbackFactory);
        assertNotNull(lims);
        try {
            lims.verifyMaxAttributeCount(10000);
            fail("Limitation of verification not failed.");
        }
        catch (final LicensingException e){
            assertNotNull(e);
        }
        try {
            lims.verifyMaxInstanceCount(10000);
            fail("Limitation of verification not failed.");
        }
        catch (final LicensingException e){
            assertNotNull(e);
        }
    }
}
