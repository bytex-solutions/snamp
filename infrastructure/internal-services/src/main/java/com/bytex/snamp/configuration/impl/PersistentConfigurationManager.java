package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

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
    private final Logger logger;
    private final ReadWriteLock configurationLock;
    @Aggregation(cached = true)
    private final CMManagedResourceParserImpl resourceParser;
    @Aggregation(cached = true)
    private final CMGatewayParserImpl gatewayInstanceParser;
    private final CMThreadPoolParser threadPoolParser;
    private final CMManagedResourceGroupParser groupParser;

    /**
     * Initializes a new configuration manager.
     * @param configAdmin OSGi configuration admin. Cannot be {@literal null}.
     */
    public PersistentConfigurationManager(final ConfigurationAdmin configAdmin){
        admin = Objects.requireNonNull(configAdmin, "configAdmin is null.");
        logger = Logger.getLogger(getClass().getName());
        configurationLock = new ReentrantReadWriteLock();
        resourceParser = new CMManagedResourceParserImpl();
        gatewayInstanceParser = new CMGatewayParserImpl();
        threadPoolParser = new CMThreadPoolParser();
        groupParser = new CMManagedResourceGroupParser();
    }

    private void mergeResourcesWithGroups(final ConfigurationEntityList<SerializableManagedResourceConfiguration> resources,
                                          final ConfigurationEntityList<SerializableManagedResourceGroupConfiguration> groups) {
        //migrate attributes, events, operations and properties from modified groups into resources
        groups.modifiedEntries((groupName, groupConfig) -> {
            resources.values().parallelStream()                 //attempt to increase performance with many registered resources
                    .filter(resource -> resource.getGroupName().equals(groupName))
                    .forEach(resource -> {
                        //overwrite all properties in resource but hold user-defined properties
                        resource.getParameters().putAll(groupConfig.getParameters());
                        //overwrite all attributes
                        resource.getAttributes().putAll(groupConfig.getAttributes());
                        //overwrite all events
                        resource.getEvents().putAll(groupConfig.getEvents());
                        //overwrite all operations
                        resource.getOperations().putAll(groupConfig.getOperations());
                    });
            return true;
        });
    }

    private void save(final SerializableAgentConfiguration config) throws IOException {
        if (config.hasNoInnerItems()) {
            resourceParser.removeAll(admin);
            gatewayInstanceParser.removeAll(admin);
            threadPoolParser.removeAll(admin);
            groupParser.removeAll(admin);
        } else {
            mergeResourcesWithGroups(config.getManagedResources(), config.getManagedResourceGroups());
            gatewayInstanceParser.saveChanges(config, admin);
            resourceParser.saveChanges(config, admin);
            threadPoolParser.saveChanges(config, admin);
            groupParser.saveChanges(config, admin);
        }
        //save SNAMP config
        CMAgentParserImpl.saveParameters(admin, config);
    }

    private <E extends Throwable> void processConfiguration(final ConfigurationProcessor<E> handler, final Lock synchronizer) throws E, IOException {
        //obtain lock on configuration
        callAndWrapException(() -> {
            synchronizer.lockInterruptibly();
            return null;
        }, IOException::new);
        //Process configuration protected by lock.
        try {
            final SerializableAgentConfiguration config = new SerializableAgentConfiguration();
            gatewayInstanceParser.fill(admin, config.getGatewayInstances());
            resourceParser.fill(admin, config.getManagedResources());
            threadPoolParser.fill(admin, config.getThreadPools());
            groupParser.fill(admin, config.getManagedResourceGroups());
            CMAgentParserImpl.loadParameters(admin, config);
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
        final Box<O> result = BoxFactory.create(null);
        readConfiguration(result.changeConsumingType(handler));
        return result.get();
    }
}
