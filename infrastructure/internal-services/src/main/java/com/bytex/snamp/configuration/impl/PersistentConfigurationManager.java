package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Box;
import com.bytex.snamp.Consumer;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
    private final ReadWriteLock configSynchronizer;
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
        configSynchronizer = new ReentrantReadWriteLock();
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
        final Box<AgentConfiguration> result = new Box<>();
        try {
            processConfiguration(result::set, false);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Unable to read configuration", e);
            result.set(new SerializableAgentConfiguration());
        }
        return result.get();
    }

    private <E extends Throwable> void processConfigurationImpl(final Consumer<? super SerializableAgentConfiguration, E> handler,
                                                           final boolean saveChanges) throws E, IOException {
        final Lock lock = saveChanges ? configSynchronizer.writeLock() : configSynchronizer.readLock();
        try {
            lock.lockInterruptibly();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
        //lock is obtained. Let's process the configuration
        try {
            final SerializableAgentConfiguration config = new SerializableAgentConfiguration();
            adapterParser.readAdapters(admin, config.adapters);
            resourceParser.readResources(admin, config.resources);

            handler.accept(config);
            if (saveChanges)
                save(config);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Process SNAMP configuration.
     *
     * @param handler     A handler used to process configuration. Cannot be {@literal null}.
     * @param saveChanges {@literal true} to save configuration changes after processing; otherwise, {@literal false}.
     * @throws E           An exception thrown by handler.
     * @throws IOException Unrecoverable exception thrown by configuration infrastructure.
     */
    @Override
    public <E extends Throwable> void processConfiguration(final Consumer<? super AgentConfiguration, E> handler,
                                                                        final boolean saveChanges) throws E, IOException {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationAdmin> configAdmin = ServiceHolder.tryCreate(context, ConfigurationAdmin.class);
        if (configAdmin == null)
            throw new IOException("ConfigurationAdmin is not available.");
        else
            try {
                processConfigurationImpl(handler, saveChanges);
            } finally {
                configAdmin.release(context);
            }
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
