package com.itworks.snamp.configuration.xml;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.configuration.ConfigurationManager;

import java.io.*;

/**
 * Represents XML-based file configuration manager. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlConfigurationManager extends AbstractAggregator implements ConfigurationManager {
    private XmlAgentConfiguration currentConfig = null;

    /**
     * Returns the currently loaded configuration.
     *
     * @return The currently loaded configuration.
     */
    @Override
    public XmlAgentConfiguration getCurrentConfiguration() {
        if(currentConfig == null)
            reload();
        return currentConfig;
    }

    public String getConfigurationFileName(){
        return System.getProperty(CONFIGURATION_FILE_PROPERTY);
    }

    /**
     * Reload agent configuration from the persistent storage.
     */
    @Override
    public void reload() {
        currentConfig = new XmlAgentConfiguration();
        try(final InputStream fs = new FileInputStream(getConfigurationFileName())){
            currentConfig.load(fs);
        }
        catch (final IOException e) {
            //TODO: call logging bundle
        }
    }

    /**
     * Dumps the agent configuration into the persistent storage.
     */
    @Override
    public void sync() {
        if(currentConfig != null)
            try(final OutputStream fs = new FileOutputStream(getConfigurationFileName())){
                currentConfig.save(fs);
            }
            catch (final IOException e) {
                //TODO: call logging bundle
            }
    }
}
