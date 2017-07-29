package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LockDecorator;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.wrapException;

/**
 * Represents SNAMP configuration manager that uses {@link ConfigurationAdmin}
 * to store and read SNAMP configuration.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@ThreadSafe
public final class PersistentConfigurationManager implements ConfigurationManager {
    private final ConfigurationAdmin admin;
    private final LockDecorator readLock, writeLock;

    /**
     * Initializes a new configuration manager.
     * @param configAdmin OSGi configuration admin. Cannot be {@literal null}.
     */
    public PersistentConfigurationManager(final ConfigurationAdmin configAdmin){
        admin = Objects.requireNonNull(configAdmin, "configAdmin is null.");
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = LockDecorator.readLock(rwLock);
        writeLock = LockDecorator.writeLock(rwLock);
    }

    private static void mergeResourcesWithGroups(final SerializableEntityMap<SerializableManagedResourceConfiguration> resources,
                                          final SerializableEntityMap<SerializableManagedResourceGroupConfiguration> groups) {
        //migrate attributes, events, operations and properties from modified groups into resources
        final Set<String> modifiedGroups = groups.modifiedEntries((groupName, groupConfig) -> {
            resources.values().stream()
                    .filter(resource -> resource.getGroupName().equals(groupName))
                    .forEach(groupConfig::fillResourceConfig);
            return true;
        });
        //migrate attributes, events, operations and properties from groups into modified resources
        resources.modifiedEntries((resourceName, resourceConfig) -> {
            final String groupName = resourceConfig.getGroupName();
            if (!modifiedGroups.contains(groupName))
                groups.getIfPresent(groupName).ifPresent(groupConfig -> groupConfig.fillResourceConfig(resourceConfig));
            return true;
        });
    }

    private static void save(final SerializableAgentConfiguration config, final ConfigurationAdmin admin) throws IOException {
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

    private static  <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler,
                                                            final ConfigurationAdmin admin,
                                                            final LockDecorator synchronizer) throws E, IOException {
        //TODO: Write lock on configuration should be distributed across cluster nodes
        try (final SafeCloseable lock = synchronizer.acquireLock(null)) {
            final SerializableAgentConfiguration config = new SerializableAgentConfiguration();
            DefaultGatewayParser.getInstance().populateRepository(admin, config);
            DefaultManagedResourceParser.getInstance().populateRepository(admin, config);
            DefaultThreadPoolParser.getInstance().populateRepository(admin, config);
            DefaultManagedResourceGroupParser.getInstance().populateRepository(admin, config);
            DefaultSupervisorParser.getInstance().populateRepository(admin, config);
            DefaultAgentParser.loadParameters(admin, config);
            config.reset();
            if (handler.process(config) && config.isModified())
                save(config, admin);
        } catch (final InterruptedException | TimeoutException e) {
            throw wrapException("Unable to acquire synchronization lock", e, InterruptedIOException::new);
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
        processConfiguration(handler, admin, writeLock);
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
        processConfiguration(ConfigurationProcessor.of(handler), admin, readLock);
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

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        final Optional<?> result;
        if (objectType.isInstance(this))
            result = Optional.of(this);
        else if (objectType.isAssignableFrom(DefaultSupervisorParser.class))
            result = Optional.of(DefaultSupervisorParser.getInstance());
        else if (objectType.isAssignableFrom(DefaultGatewayParser.class))
            result = Optional.of(DefaultGatewayParser.getInstance());
        else if (objectType.isAssignableFrom(DefaultThreadPoolParser.class))
            result = Optional.of(DefaultThreadPoolParser.getInstance());
        else if (objectType.isAssignableFrom(DefaultManagedResourceParser.class))
            result = Optional.of(DefaultManagedResourceParser.getInstance());
        else if (objectType.isAssignableFrom(DefaultManagedResourceGroupParser.class))
            result = Optional.of(DefaultManagedResourceGroupParser.getInstance());
        else
            result = Optional.empty();
        return result.map(objectType::cast);
    }
}
