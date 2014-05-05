package com.itworks.snamp.licensing;

import com.itworks.snamp.SnampTestSet;
import org.junit.Test;

/**
 * Represents SNAMP licensing infrastructure tests.
 * @author Roman Sakno
 */
public final class LicensingTest extends SnampTestSet {

    @Test
    public void jmxConnectorLicenseLimitations() throws ClassNotFoundException {
        JmxConnectorLimitations.current().verifyMaxAttributeCount(10);
        JmxConnectorLimitations.current().verifyMaxInstanceCount(10);
        //JmxConnectorLimitations.current().verifyPluginVersion((Class<AbstractManagementConnectorFactory>)Class.forName("com.itworks.snamp.connectors.tests.jmx.JmxConnectorFactory"));
    }

    @Test(expected = LicensingException.class)
    public void limitationFailures(){
        JmxConnectorLimitations.current().verifyMaxInstanceCount(1000000);
    }
}
