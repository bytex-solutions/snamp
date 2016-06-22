package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.*;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents SNAMP configuration manager that uses {@link ConfigurationAdmin}
 * to store and read SNAMP configuration.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@ThreadSafe
public final class PersistentConfigurationManager extends AbstractAggregator implements ConfigurationManager {
    private final ConfigurationAdmin admin;
    private final Logger logger;
    private final ReadWriteLock configurationSynchronizer;
    @Aggregation
    private final CMManagedResourceParserImpl resourceParser;
    @Aggregation
    private final CMResourceAdapterParserImpl adapterParser;

    /**
     * Initializes a new configuration manager.
     * @param configAdmin OSGi configuration admin. Cannot be {@literal null}.
     */
    public PersistentConfigurationManager(final ConfigurationAdmin configAdmin){
        admin = Objects.requireNonNull(configAdmin, "configAdmin is null.");
        logger = Logger.getLogger(getClass().getName());
        configurationSynchronizer = new ReentrantReadWriteLock();
        resourceParser = new CMManagedResourceParserImpl();
        adapterParser = new CMResourceAdapterParserImpl();
    }

    private void save(final SerializableAgentConfiguration config) throws IOException {
        if (config.isEmpty()) {
            resourceParser.removeAll(admin);
            adapterParser.removeAll(admin);
        } else {
            adapterParser.saveChanges(config, admin);
            resourceParser.saveChanges(config, admin);
        }
    }

    /**
     * Returns the currently loaded configuration.
     *
     * @return The currently loaded configuration.
     */
    @Override
    @Aggregation
    @Deprecated
    public AgentConfiguration getCurrentConfiguration() {
        try {
            return transformConfiguration(Function.identity());
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Unable to read configuration", e);
            return new SerializableAgentConfiguration();
        }
    }

    private <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler, final Lock synchronizer) throws E, IOException {
        //obtain lock on configuration
        try {
            synchronizer.lockInterruptibly();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
        //Process configuration protected by lock.
        try {
            final SerializableAgentConfiguration config = new SerializableAgentConfiguration();
            adapterParser.readAdapters(admin, config.getResourceAdaptersImpl());
            resourceParser.readResources(admin, config.getManagedResourcesImpl());
            if(handler.process(config))
                save(config);
        } finally {
            synchronizer.unlock();
        }
    }

    /**
     * Process SNAMP configuration.
     * @param handler A handler used to process configuration. Cannot be {@literal null}.
     * @param <E> Type of user-defined exception that can be thrown by handler.
     * @throws E An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     * @since 1.2
     */
    @Override
    public <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler) throws E, IOException {
        //configuration may be changed by handler so we protect it with exclusive lock.
        processConfiguration(handler, configurationSynchronizer.writeLock());
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
    public <E extends Throwable> void readConfiguration(final Consumer<? super AgentConfiguration, E> handler) throws E, IOException {
        //reading configuration doesn't require exclusive lock
        processConfiguration(config -> {
            handler.accept(config);
            return false;
        }, configurationSynchronizer.readLock());
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
        final Box<O> result = new Box<>();
        readConfiguration(result.changeConsumingType(handler));
        return result.get();
    }

    @Override
    @Deprecated
    public void reload() {
    }

    @Override
    @Deprecated
    public void sync() {
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public Logger getLogger() {
        return logger;
    }
}
