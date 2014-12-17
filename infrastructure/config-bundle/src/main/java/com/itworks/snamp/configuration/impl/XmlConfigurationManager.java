package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.configuration.StreamedConfigurationManager;

import java.io.*;
import java.util.logging.Logger;

/**
 * Represents XML-based file configuration manager. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XmlConfigurationManager extends StreamedConfigurationManager<XmlAgentConfiguration> {
    /**
     * Represents name of the logger for the configuration service.
     */
    private static final String LOGGER_NAME = "com.itworks.snamp.configuration";

    /**
     * Initializes a new XML-based configuration manager.
     */
    XmlConfigurationManager() {
    }

    public String getConfigurationFileName(){
        return System.getProperty(CONFIGURATION_FILE_PROPERTY);
    }

    /**
     * Creates a new empty instance of the configuration.
     *
     * @return A new empty instance of the configuration.
     */
    @Override
    protected XmlAgentConfiguration newConfiguration() {
        return new XmlAgentConfiguration();
    }

    /**
     * Creates a new stream for restoring the configuration.
     *
     * @return A new stream for restoring the configuration.
     */
    @Override
    protected InputStream openInputStream() throws IOException{
        return new FileInputStream(getConfigurationFileName());
    }

    /**
     * Creates a new stream for saving the configuration.
     *
     * @return A new stream for saving the configuration.
     */
    @Override
    protected OutputStream openOutputStream() throws IOException{
        return new FileOutputStream(getConfigurationFileName());
    }

    /**
     * Gets a logger associated with this platform service.
     *
     * @return A logger associated with this platform service.
     */
    @Override
    public Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }
}
