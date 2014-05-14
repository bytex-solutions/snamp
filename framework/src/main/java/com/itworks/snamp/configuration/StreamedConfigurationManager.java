package com.itworks.snamp.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract configuration manager which persistence is based on streams.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class StreamedConfigurationManager<T extends AgentConfiguration> extends AbstractConfigurationManager {
    /**
     * Initializes a new persistent configuration manager.
     * @param serviceLogger OSGi logging service wrapped into {@link Logger} instance. Can be obtained
     *                      from {@link com.itworks.snamp.core.AbstractLoggableServiceLibrary.LoggableProvidedService#getLogger()} method.
     */
    protected StreamedConfigurationManager(final Logger serviceLogger){
        super(serviceLogger);
    }

    /**
     * Creates a new empty instance of the configuration.
     * @return A new empty instance of the configuration.
     */
    protected abstract T newConfiguration();

    /**
     * Creates a new stream for restoring the configuration.
     * @return A new stream for restoring the configuration.
     */
    protected abstract InputStream openInputStream() throws IOException;

    /**
     * Creates a new stream for saving the configuration.
     * @return A new stream for saving the configuration.
     */
    protected abstract OutputStream openOutputStream() throws IOException;

    /**
     * Creates a new instance of the agent configuration and initializes its state
     * from the underlying persistent storage.
     *
     * @return A new initialized instance of the agent configuration.
     */
    @Override
    protected final AgentConfiguration restore() {
        final T newInstance = newConfiguration();
        try(final InputStream is = openInputStream()){
            newInstance.load(is);
        }
        catch(final FileNotFoundException e){ //just re-create stream with empty config
            getLogger().log(Level.INFO, "SNAMP configuration is missing. Blank configuration is created.", e);
            save(newInstance);
        }
        catch (final IOException e){
            getLogger().log(Level.SEVERE, "Unable to read SNAMP configuration", e);
        }
        return newInstance;
    }

    /**
     * Saves the current configuration into the underlying persistent storage.
     *
     * @param currentConfig The current configuration to save.
     */
    @Override
    protected final void save(final AgentConfiguration currentConfig) {
        try(final OutputStream os = openOutputStream()){
            currentConfig.save(os);
        }
        catch(final IOException e){
            getLogger().log(Level.SEVERE, "Unable to store SNAMP configuration", e);
        }
    }


}
