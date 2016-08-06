package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Acceptor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;

import java.io.IOException;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Represents in-memory configuration manager.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public final class InMemoryConfigurationManager extends AbstractAggregator implements ConfigurationManager {
    private final Logger logger = Logger.getLogger("InMemoryConfigurationManager");
    private final ConcurrentResourceAccessor<SerializableAgentConfiguration> currentConfiguration =
            new ConcurrentResourceAccessor<>(new SerializableAgentConfiguration());

    /**
     * Process SNAMP configuration.
     *
     * @param handler A handler used to process configuration. Cannot be {@literal null}.
     * @throws E           An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    @Override
    public <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler) throws E, IOException {
        currentConfiguration.changeResource(config -> {
            final SerializableAgentConfiguration copy = config.clone();
            return handler.process(copy) ? copy : config;
        });
    }

    /**
     * Read SNAMP configuration.
     *
     * @param handler A handler used to read configuration. Cannot be {@literal null}.
     * @throws E           An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    @Override
    public <E extends Throwable> void readConfiguration(final Acceptor<? super AgentConfiguration, E> handler) throws E, IOException {
        currentConfiguration.read(config -> {
            handler.accept(config);
            return null;
        });
    }

    /**
     * Read SNAMP configuration and transform it into custom object.
     *
     * @param handler A handler used to read configuration. Cannot be {@literal null}.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    @Override
    public <O> O transformConfiguration(final Function<? super AgentConfiguration, O> handler) throws IOException {
        return currentConfiguration.read(handler::apply);
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return logger;
    }
}
