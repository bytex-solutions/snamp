package com.itworks.snamp.configuration;

import com.itworks.snamp.core.AbstractBundleActivator;

/**
 * Represents an abstract class for SNAMP configuration bundle activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractConfigurationBundleActivator extends AbstractBundleActivator {
    /**
     * Initializes a new SNAMP-specific bundle.
     *
     * @param loggerName The name of the logger that is used by all services published by the bundle.
     */
    protected AbstractConfigurationBundleActivator(final String loggerName) {
        super(loggerName);
    }

    /**
     * Creates a new instance of the SNAMP configuration management service.
     * @return A new instance of the SNAMP configuration management service.
     */
    protected abstract ConfigurationManager createConfigurationManager();

    /**
     * Exposes {@link ConfigurationManager} service to OSGi environment.
     *
     * @param publisher The service publisher.
     */
    @Override
    protected final void registerServices(final ServicePublisher publisher) {
        publisher.publish(ConfigurationManager.class, createConfigurationManager(), null);
    }
}
