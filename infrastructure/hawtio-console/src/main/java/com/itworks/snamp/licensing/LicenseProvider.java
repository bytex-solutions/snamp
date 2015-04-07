package com.itworks.snamp.licensing;

/**
 * Represents license provider.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface LicenseProvider {
    XmlLicense getLicense();
}
