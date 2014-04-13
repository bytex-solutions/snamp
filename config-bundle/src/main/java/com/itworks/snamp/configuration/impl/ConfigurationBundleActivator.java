package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.configuration.AbstractConfigurationBundleActivator;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractBundleActivator;

/**
 * Represents activator for the configuration manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationBundleActivator extends AbstractConfigurationBundleActivator {
    public static final String LOGGER_NAME = "itworks.snamp.configuration";

    protected static final class XmlConfigurationManagerProvider extends ConfigurationManagerProvider<XmlConfigurationManager>{

        @Override
        protected XmlConfigurationManager activateService(final RequiredService<?>... dependencies) {
            return new XmlConfigurationManager(getLogger());
        }
    }

    /**
     * Initializes a new instance of the configuration bundle activator.
     */
    public ConfigurationBundleActivator(){
        super(LOGGER_NAME, XmlConfigurationManagerProvider.class);
    }
}
