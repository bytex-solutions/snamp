package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.configuration.AbstractConfigurationBundleActivator;

import java.util.Map;

/**
 * Represents activator for the configuration manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationBundleActivator extends AbstractConfigurationBundleActivator {
    /**
     * Represents name of the logger for the configuration service.
     */
    public static final String LOGGER_NAME = "itworks.snamp.configuration";

    private static final class XmlConfigurationManagerProvider extends ConfigurationManagerProvider<XmlConfigurationManager>{

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected XmlConfigurationManager activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return new XmlConfigurationManager(getLogger());
        }
    }

    /**
     * Initializes a new instance of the configuration bundle activator.
     */
    public ConfigurationBundleActivator(){
        super(LOGGER_NAME, new XmlConfigurationManagerProvider());
    }
}
