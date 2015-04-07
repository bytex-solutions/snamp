package com.itworks.snamp.licensing;

import javax.xml.bind.JAXBException;

/**
 * Represents license provider.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface LicenseProvider {
    XmlLicense getLicense() throws JAXBException;
}
