package com.itworks.snamp;

import com.itworks.snamp.licensing.LicenseReader;
import org.junit.Assert;

/**
 * Represents an abstract class for building SNAMP tests.
 * @author Roman Sakno
 */
public abstract class SnampTestSet extends Assert {
    public static final String TEST_LICENCE_FILE = "unlimited.lic";

    static {
        if (System.getProperty(LicenseReader.LICENSE_FILE_PROPERTY) == null)
            System.setProperty(LicenseReader.LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE);
        LicenseReader.reloadCurrentLicense();
    }
}
