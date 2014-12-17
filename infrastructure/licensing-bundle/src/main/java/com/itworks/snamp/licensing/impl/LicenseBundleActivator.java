package com.itworks.snamp.licensing.impl;

import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicenseReader;

import java.util.Collection;
import java.util.Map;

/**
 * Represents implementation of the SNAMP licensing bundle. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LicenseBundleActivator extends AbstractServiceLibrary {


    private static final class XmlLicenseReaderProvider extends ProvidedService<LicenseReader, XmlLicenseReader>{

        /**
         * Initializes a new holder for the provided service.
         */
        private XmlLicenseReaderProvider() {
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
            final XmlLicenseReader reader = new XmlLicenseReader();
            reader.reload();
            return reader;
        }
    }

    public LicenseBundleActivator(){
        super(new XmlLicenseReaderProvider());
    }


    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     */
    @Override
    @MethodStub
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {

    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     */
    @Override
    @MethodStub
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) {

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
