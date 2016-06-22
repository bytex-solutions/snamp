package com.bytex.snamp.configuration;

import com.bytex.snamp.core.FrameworkService;

import java.io.IOException;

/**
 * Represents SNAMP configuration manager that is accessible as OSGi service.
 * <p>
 *     This interface must return an instance of {@link com.bytex.snamp.configuration.internal.CMManagedResourceParser} or
 *     {@link com.bytex.snamp.configuration.internal.CMResourceAdapterParser} when {@link com.bytex.snamp.Aggregator#queryObject(Class)} is called
 *     with suitable arguments.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ConfigurationManager extends FrameworkService {
    /**
     * A functional interface used to process SNAMP configuration.
     * @param <E> Type of exception that can be thrown by custom processor.
     * @since 1.2
     */
    @FunctionalInterface
    interface ConfigurationProcessor<E extends Throwable>{
        /**
         * Process SNAMP configuration.
         * @param config A configuration to process.
         * @return {@literal true} to save changes; otherwise, {@literal false}.
         * @throws E An exception thrown by custom processor.
         */
        boolean process(final AgentConfiguration config) throws E;
    }

    /**
     * Process SNAMP configuration.
     * @param handler A handler used to process configuration. Cannot be {@literal null}.
     * @param <E> Type of user-defined exception that can be thrown by handler.
     * @throws E An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     */
    default <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler) throws E, IOException {
        synchronized (this) {
            AgentConfiguration config = getCurrentConfiguration();
            if (config == null) {
                reload();
                config = getCurrentConfiguration();
            }
            if (config == null)
                throw new IOException("Configuration is not available");
            else if(handler.process(config))
                sync();
        }
    }

    /**
     * Returns the currently loaded configuration.
     * @return The currently loaded configuration.
     * @deprecated Use {@link #processConfiguration(ConfigurationProcessor)} instead. Deprecated since 1.2
     */
    @Deprecated
    AgentConfiguration getCurrentConfiguration();

    /**
     * Reload agent configuration from the persistent storage.
     * @deprecated Use {@link #processConfiguration(ConfigurationProcessor)} instead. Deprecated since 1.2
     */
    @Deprecated
    void reload();

    /**
     * Dumps the agent configuration into the persistent storage.
     * Use {@link #processConfiguration(ConfigurationProcessor)} instead. Deprecated since 1.2
     */
    @Deprecated
    void sync();
}
