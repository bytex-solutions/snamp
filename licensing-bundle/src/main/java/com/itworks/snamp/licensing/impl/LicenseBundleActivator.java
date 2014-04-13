package com.itworks.snamp.licensing.impl;

import com.itworks.snamp.core.AbstractLoggableBundleActivator;
import com.itworks.snamp.licensing.LicenseReader;
import org.osgi.framework.BundleActivator;

/**
 * Represents implementation of the SNAMP licensing bundle. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LicenseBundleActivator extends AbstractLoggableBundleActivator implements BundleActivator {
    public static final String LOGGER_NAME = "itworks.snamp.licensing";

    protected static final class XmlLicenseReaderProvider extends LoggableProvidedService<LicenseReader, XmlLicenseReader>{

        /**
         * Initializes a new holder for the provided service.
         */
        protected XmlLicenseReaderProvider() {
            super(LicenseReader.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected XmlLicenseReader activateService(final RequiredService<?>... dependencies) {
            return new XmlLicenseReader(getLogger());
        }
    }

    public LicenseBundleActivator(){
        super(LOGGER_NAME, XmlLicenseReaderProvider.class);
    }
}
