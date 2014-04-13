package com.itworks.snamp;

import org.junit.Assert;
import static com.itworks.snamp.licensing.LicenseReader.LICENSE_FILE_PROPERTY;

/**
 * Represents a base class for all SNAMP-specific tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTest extends Assert {
    /**
     * Represents relative path to the test license file.
     */
    public static final String TEST_LICENCE_FILE = "unlimited.lic";

    static {
        if (System.getProperty(LICENSE_FILE_PROPERTY) == null)
            System.setProperty(LICENSE_FILE_PROPERTY, TEST_LICENCE_FILE);
    }
}
