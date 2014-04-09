package com.itworks.snamp.configuration;

import com.itworks.snamp.core.AbstractPlatformService;

import java.util.logging.Logger;

/**
 * Represents a base class for constructing configuration manager services.
 * @param <T> Type of the configuration object model implementer.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractConfigurationManager<T extends AgentConfiguration> extends AbstractPlatformService implements ConfigurationManager {
    private T currentConfiguration;

    /**
     * Initializes a new configuration management service.
     * @param serviceLogger OSGi logging service wrapped into {@link Logger} instance. Can be obtained
     *                      from {@link com.itworks.snamp.core.AbstractBundleActivator#logger} field.
     */
    protected AbstractConfigurationManager(final Logger serviceLogger){
        super(serviceLogger);
        currentConfiguration = null;
    }

    /**
     * Creates a new instance of the agent configuration and initializes its state
     * from the underlying persistent storage.
     * @return A new initialized instance of the agent configuration.
     */
    protected abstract T restore();

    /**
     * Returns the currently loaded configuration.
     *
     * @return The currently loaded configuration.
     */
    @Override
    public final AgentConfiguration getCurrentConfiguration() {
        if(currentConfiguration == null)
            currentConfiguration = restore();
        return currentConfiguration;
    }

    /**
     * Reload agent configuration from the persistent storage.
     */
    @Override
    public final void reload() {
        currentConfiguration = restore();
    }

    /**
     * Saves the current configuration into the underlying persistent storage.
     * @param currentConfig The current configuration to save.
     */
    protected abstract void save(final T currentConfig);

    /**
     * Dumps the agent configuration into the persistent storage.
     */
    @Override
    public final void sync() {
        if(currentConfiguration != null) save(currentConfiguration);
    }
}
