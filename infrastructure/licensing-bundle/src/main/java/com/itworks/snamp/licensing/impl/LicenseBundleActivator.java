package com.itworks.snamp.licensing.impl;

import com.itworks.snamp.core.AbstractLoggableServiceLibrary;
import com.itworks.snamp.internal.semantics.MethodStub;
import com.itworks.snamp.licensing.LicenseReader;

import java.util.Map;

/**
 * Represents implementation of the SNAMP licensing bundle. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LicenseBundleActivator extends AbstractLoggableServiceLibrary {
    public static final String LOGGER_NAME = "itworks.snamp.licensing";

    private static final class XmlLicenseReaderProvider extends LoggableProvidedService<LicenseReader, XmlLicenseReader>{

        /**
         * Initializes a new holder for the provided service.
         */
        protected XmlLicenseReaderProvider() {
            super(LicenseReader.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected XmlLicenseReader activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            final XmlLicenseReader reader = new XmlLicenseReader(getLogger());
            reader.reload();
            return reader;
        }
    }

    public LicenseBundleActivator(){
        super(LOGGER_NAME, new XmlLicenseReaderProvider());
    }


    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {
    }
}
