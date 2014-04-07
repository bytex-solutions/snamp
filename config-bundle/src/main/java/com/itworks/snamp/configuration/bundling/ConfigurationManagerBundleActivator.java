package com.itworks.snamp.configuration.bundling;

import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.configuration.xml.XmlConfigurationManager;
import org.osgi.framework.*;

/**
 * Represents activator for the configuration manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConfigurationManagerBundleActivator implements BundleActivator {
    private final XmlConfigurationManager configManager = new XmlConfigurationManager();
    private ServiceRegistration<ConfigurationManager> registrationInfo;

    /**
     * Initializes configuration manager.
     * @param context Configuration manager activation context.
     * @throws Exception
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        registrationInfo = context.registerService(ConfigurationManager.class, configManager, null);
    }


    /**
     * Stops
     * @param context
     * @throws Exception
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        if(registrationInfo != null)
            registrationInfo.unregister();
    }
}
