package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.configuration.AbstractConfigurationBundleActivator;
import com.itworks.snamp.configuration.ConfigurationManager;

/**
 * Represents activator for the configuration manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationBundleActivator extends AbstractConfigurationBundleActivator {
    public static final String LOGGER_NAME = "itworks.snamp.configuration";

    /**
     * Initializes a new instance of the configuration bundle activator.
     */
    public ConfigurationBundleActivator(){
        super(LOGGER_NAME);
    }

    /**
     * Creates a new instance of the SNAMP configuration management service.
     *
     * @return A new instance of the SNAMP configuration management service.
     */
    @Override
    protected ConfigurationManager createConfigurationManager() {
        return new XmlConfigurationManager(logger);
    }
}
