package com.bytex.snamp.supervision.discovery.rest;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.supervision.discovery.AbstractResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceAddedEvent;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryEvent;
import com.bytex.snamp.supervision.discovery.ResourceRemovedEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents default resource discovery service
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultResourceDiscoveryService extends AbstractResourceDiscoveryService {
    private Object discoveryEventSource;
    private final ConfigurationManager configurationManager;
    private final String groupName;

    public DefaultResourceDiscoveryService(@Nonnull final String groupName,
                                           @Nonnull final ConfigurationManager configManager){
        discoveryEventSource = null;
        configurationManager = configManager;
        this.groupName = groupName;
    }

    /**
     * Overrides source for all outbound events of type {@link ResourceDiscoveryEvent}.
     * @param eventSource A new event source.
     */
    public final void setSource(@Nonnull final Object eventSource){
        discoveryEventSource = eventSource;
    }

    /**
     * Gets source of all outbound events of type {@link ResourceDiscoveryEvent}.
     * @return Source of all outbound discovery events.
     */
    protected final Object getSource(){
        return firstNonNull(discoveryEventSource, this);
    }

    /**
     * Used for detecting new resources via {@link com.bytex.snamp.supervision.AbstractSupervisor#addResource(String, ManagedResourceConnector)}
     * @param resourceName Name of the added resource.
     * @implSpec This method just raises an event.
     */
    public final void resourceRegistered(final String resourceName){
        fireDiscoveryEvent(new ResourceAddedEvent(getSource(), resourceName));
    }

    /**
     * Used for removing existing resources via {@link com.bytex.snamp.supervision.AbstractSupervisor#removeResource(String, ManagedResourceConnector)}
     * @param resourceName Name of the removed resource.
     * @implSpec This method just raises an event.
     */
    public final void resourceRemoved(final String resourceName) {
        fireDiscoveryEvent(new ResourceRemovedEvent(getSource(), resourceName));
    }

    private boolean registerResource(final EntityMap<? extends ManagedResourceConfiguration> resources,
                                        final ManagedResourceGroupConfiguration groupConfig,
                                        final String resourceName,
                                        final String connectionString,
                                        final Map<String, String> parameters) throws InvalidResourceGroupException {
        ManagedResourceConfiguration resourceConfig;
        if (resources.containsKey(resourceName)) {
            resourceConfig = resources.get(resourceName);
            checkGroupName(resourceName, resourceConfig);
        } else
            resourceConfig = resources.getOrAdd(resourceName);
        groupConfig.fillResourceConfig(resourceConfig);
        resourceConfig.setGroupName(groupName);
        resourceConfig.setConnectionString(connectionString);
        resourceConfig.putAll(parameters);
        return true;
    }

    private static ResourceDiscoveryException configurationCrashed(final IOException e){
        return new ResourceDiscoveryException("Configuration subsystem crashed", e);
    }

    private void checkGroupName(final String resourceName,
                                final ManagedResourceConfiguration resourceConfig) throws InvalidResourceGroupException{
        if(!Objects.equals(groupName, resourceConfig.getGroupName()))
            throw new InvalidResourceGroupException(resourceName, resourceConfig.getGroupName(), groupName);
    }

    /**
     * Registers a new resource using discovery service.
     * @param resourceName A new resource name.
     * @param connectionString A new connection string.
     * @param parameters Additional parameters to be associated with the resource.
     * @throws ResourceDiscoveryException Unable to register resource using discovery service.
     */
    public final void registerResource(@Nonnull final String resourceName,
                                       @Nonnull final String connectionString,
                                       @Nonnull final Map<String, String> parameters) throws ResourceDiscoveryException {
        //we assume than modification of SNAMP configuration causes instantiation of a new resource connector
        //and this fact will raise resourceRegistered event through supervisor
        try {
            configurationManager.processConfiguration(config -> {
                final Optional<? extends ManagedResourceGroupConfiguration> groupConfiguration =
                        config.getResourceGroups().getIfPresent(groupName);
                if (groupConfiguration.isPresent())
                    return registerResource(config.getResources(), groupConfiguration.get(), resourceName, connectionString, parameters);
                else
                    throw new ResourceGroupNotFoundException(groupName);
            });
        } catch (final IOException e) {
            throw configurationCrashed(e);
        }
    }

    /**
     * Removes the specified resource from the group.
     * @param resourceName Name of the resource to remove.
     * @throws ResourceDiscoveryException Unable to remove resource.
     */
    public final void removeResource(@Nonnull final String resourceName) throws ResourceDiscoveryException {
        try {
            configurationManager.processConfiguration(config -> {
                final ManagedResourceConfiguration resourceConfig = config.getResources().get(resourceName);
                if (resourceConfig == null)
                    return false;
                else
                    checkGroupName(resourceName, resourceConfig);
                return config.getResources().remove(resourceName) != null;
            });
        } catch (final IOException e) {
            throw configurationCrashed(e);
        }
    }

    /**
     * Removes all resources from the group.
     * @throws ResourceDiscoveryException Unable to remove resources.
     */
    public final void removeAllResources() throws ResourceDiscoveryException {
        try {
            configurationManager.processConfiguration(config -> {
                final Set<String> resourcesToRemove = new HashSet<>(10);
                config.getResources().forEach((resourceName, resourceConfig) -> {
                    if (Objects.equals(resourceConfig.getGroupName(), groupName))
                        resourcesToRemove.add(resourceName);
                });
                resourcesToRemove.forEach(config.getResources()::remove);
                return !resourcesToRemove.isEmpty();
            });
        } catch (final IOException e) {
            throw configurationCrashed(e);
        }
    }
}
