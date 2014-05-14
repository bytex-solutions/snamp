package com.itworks.snamp.configuration;

import com.itworks.snamp.core.AbstractLoggableServiceLibrary;

/**
 * Represents an abstract class for SNAMP configuration bundle activator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractConfigurationBundleActivator extends AbstractLoggableServiceLibrary {

    /**
     * Represents an abstract class for {@link ConfigurationManager} factory.
     * @param <T> Type of the configuration manager implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ConfigurationManagerProvider<T extends ConfigurationManager> extends LoggableProvidedService<ConfigurationManager, T>{

        /**
         * Initializes a new holder for the configuration manager service.
         *
         * @param dependencies A collection of configuration manager dependencies.
         */
        protected ConfigurationManagerProvider(final RequiredService<?>... dependencies) {
            super(ConfigurationManager.class, dependencies);
        }
    }

    /**
     * Initializes a new SNAMP-specific bundle.
     *
     * @param loggerName The name of the logger that is used by all services published by the bundle.
     */
    protected AbstractConfigurationBundleActivator(final String loggerName, final ConfigurationManagerProvider<?> configManagerProvider) {
        super(loggerName, configManagerProvider);
    }
}
