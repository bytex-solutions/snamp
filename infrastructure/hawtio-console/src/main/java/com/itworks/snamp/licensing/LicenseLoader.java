package com.itworks.snamp.licensing;

import org.osgi.service.cm.ManagedService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents license tracker.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface LicenseLoader extends ManagedService {
    /**
     * Represents persistence identifier used to read and write license content.
     */
    String LICENSE_PID = "com.itworks.snamp.license";

    /**
     * Load license content.
     * @param licenseContent The stream with license content.
     * @throws IOException Unable to load license content.
     */
    void loadLicense(final InputStream licenseContent) throws IOException;
}
