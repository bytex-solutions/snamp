package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Represents SNAMP configuration manager that uses {@link ConfigurationAdmin}
 * to store and read SNAMP configuration.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public final class PersistentConfigurationManager extends AbstractAggregator implements ConfigurationManager {
    private final ConfigurationAdmin admin;
    private final ReadWriteLock configurationLock;

    /**
     * Initializes a new configuration manager.
     * @param configAdmin OSGi configuration admin. Cannot be {@literal null}.
     */
    public PersistentConfigurationManager(final ConfigurationAdmin configAdmin){
        admin = Objects.requireNonNull(configAdmin, "configAdmin is null.");
        configurationLock = new ReentrantReadWriteLock();
    }

    private static void mergeResourcesWithGroups(final SerializableEntityMap<SerializableManagedResourceConfiguration> resources,
                                          final SerializableEntityMap<SerializableManagedResourceGroupConfiguration> groups) {
        //migrate attributes, events, operations and properties from modified groups into resources
        groups.modifiedEntries((groupName, groupConfig) -> {
            resources.values().stream()
                    .filter(resource -> resource.getGroupName().equals(groupName))
                    .forEach(groupConfig::fillResourceConfig);
            return true;
        });
    }

    private void save(final SerializableAgentConfiguration config) throws IOException {
        if (config.hasNoInnerItems()) {
            DefaultSupervisorParser.getInstance().removeAll(admin);
            DefaultManagedResourceParser.getInstance().removeAll(admin);
            DefaultGatewayParser.getInstance().removeAll(admin);
            DefaultThreadPoolParser.getInstance().removeAll(admin);
            DefaultManagedResourceGroupParser.getInstance().removeAll(admin);
        } else {
            mergeResourcesWithGroups(config.getResources(), config.getResourceGroups());
            DefaultGatewayParser.getInstance().saveChanges(config, admin);
            DefaultManagedResourceParser.getInstance().saveChanges(config, admin);
            DefaultThreadPoolParser.getInstance().saveChanges(config, admin);
            DefaultManagedResourceGroupParser.getInstance().saveChanges(config, admin);
            DefaultSupervisorParser.getInstance().saveChanges(config, admin);
        }
        //save SNAMP config
        DefaultAgentParser.saveParameters(admin, config);
    }

    private <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler, final Lock synchronizer) throws E, IOException {
        //TODO: Write lock on configuration should be distributed across cluster nodes
        //obtain lock on configuration
        Utils.callAndWrapException(() -> {
            synchronizer.lockInterruptibly();
            return null;
        }, IOException::new);   
        //Process configuration protected by lock.
        try {
            final SerializableAgentConfiguration config = new SerializableAgentConfiguration();
            DefaultGatewayParser.getInstance().populateRepository(admin, config);
            DefaultManagedResourceParser.getInstance().populateRepository(admin, config);
            DefaultThreadPoolParser.getInstance().populateRepository(admin, config);
            DefaultManagedResourceGroupParser.getInstance().populateRepository(admin, config);
            DefaultSupervisorParser.getInstance().populateRepository(admin, config);
            DefaultAgentParser.loadParameters(admin, config);
            if (handler.process(config))
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
        processConfiguration(handler, configurationLock.writeLock());
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
        //reading configuration doesn't require exclusive lock
        processConfiguration(config -> {
            handler.accept(config);
            return false;
        }, configurationLock.readLock());
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
        final Box<O> result = Box.of(null);
        readConfiguration(result.changeConsumingType(handler));
        return result.get();
    }

    @Nonnull
    @Override
    public ImmutableMap<String, String> getConfiguration() {
        try {
            return transformConfiguration(ImmutableMap::copyOf);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> Optional<T> queryParser(final Class<T> parserType) {
        final Optional<?> result;
        if (parserType.isInstance(DefaultSupervisorParser.getInstance()))
            result = Optional.of(DefaultSupervisorParser.getInstance());
        else if (parserType.isInstance(DefaultGatewayParser.getInstance()))
            result = Optional.of(DefaultGatewayParser.getInstance());
        else if (parserType.isInstance(DefaultThreadPoolParser.getInstance()))
            result = Optional.of(DefaultThreadPoolParser.getInstance());
        else if (parserType.isInstance(DefaultManagedResourceParser.getInstance()))
            result = Optional.of(DefaultManagedResourceParser.getInstance());
        else if (parserType.isInstance(DefaultManagedResourceGroupParser.getInstance()))
            result = Optional.of(DefaultManagedResourceGroupParser.getInstance());
        else
            result = Optional.empty();
        return result.map(parserType::cast);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return queryObject(objectType, PersistentConfigurationManager::queryParser);
    }
}
