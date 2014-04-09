package com.itworks.snamp.licensing.impl;

import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.licensing.LicenseReader;

/**
 * Represents implementation of the SNAMP licensing bundle. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LicenseBundleActivator extends AbstractBundleActivator {
    public static final String LOGGER_NAME = "itworks.snamp.licensing";

    public LicenseBundleActivator(){
        super(LOGGER_NAME);
    }

    /**
     * Exposes service into OSGi environment.
     *
     * @param publisher The service publisher.
     */
    @Override
    protected void registerServices(final ServicePublisher publisher) {
        publisher.publish(LicenseReader.class, new XmlLicenseReader(logger), null);
    }
}
