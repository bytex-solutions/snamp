package com.snamp;

import com.snamp.licensing.LicenseReader;
import org.junit.Assert;

/**
 * Represents an abstract class for building SNAMP tests.
 * @author roman
 */
public abstract class SnampTestSet extends Assert {
    public static final String TEST_LICENCE_FILE = "unlimited.lic";

    static {
        System.setProperty(LicenseReader.LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE);
        LicenseReader.reloadCurrentLicense();
    }
}
