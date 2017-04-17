package com.bytex.snamp.supervision.def;

import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.supervision.SupervisionEvent;
import com.bytex.snamp.supervision.discovery.InvalidResourceGroupException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceGroupNotFoundException;

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
public class DefaultResourceDiscoveryService implements ResourceDiscoveryService {
    private Object discoveryEventSource;
    private final ConfigurationManager configurationManager;
    protected final String groupName;

    public DefaultResourceDiscoveryService(@Nonnull final String groupName,
                                           @Nonnull final ConfigurationManager configManager){
        discoveryEventSource = null;
        configurationManager = configManager;
        this.groupName = groupName;
    }

    /**
     * Overrides source for all outbound events of type {@link SupervisionEvent}.
     * @param eventSource A new event source.
     */
    protected final void setSource(@Nonnull final Object eventSource){
        discoveryEventSource = eventSource;
    }

    /**
     * Gets source of all outbound events of type {@link SupervisionEvent}.
     * @return Source of all outbound discovery events.
     */
    private Object getSource(){
        return firstNonNull(discoveryEventSource, this);
    }

    protected boolean processResource(final String resourceName,
                                       final ManagedResourceConfiguration resourceConfig) throws ResourceDiscoveryException{
        return true;
    }

    private boolean registerResource(final EntityMap<? extends ManagedResourceConfiguration> resources,
                                        final ManagedResourceGroupConfiguration groupConfig,
                                        final String resourceName,
                                        final String connectionString,
                                        final Map<String, String> parameters) throws ResourceDiscoveryException {
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
        return processResource(resourceName, resourceConfig);
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
            throw new ResourceDiscoveryIOException(e);
        }
    }

    protected boolean removeResource(final String resourceName,
                                     final ManagedResourceConfiguration resourceConfig) throws ResourceDiscoveryException{
        return true;
    }

    /**
     * Removes the specified resource from the group.
     * @param resourceName Name of the resource to remove.
     * @throws ResourceDiscoveryException Unable to remove resource.
     */
    public final boolean removeResource(@Nonnull final String resourceName) throws ResourceDiscoveryException {
        final BooleanBox result = BoxFactory.createForBoolean(false);
        try {
            configurationManager.processConfiguration(config -> {
                final ManagedResourceConfiguration resourceConfig = config.getResources().get(resourceName);
                final boolean saveChanges;
                if (resourceConfig == null)
                    saveChanges = false;
                else {
                    checkGroupName(resourceName, resourceConfig);
                    saveChanges = removeResource(resourceName, resourceConfig) && config.getResources().remove(resourceName) != null;
                }
                result.set(saveChanges);
                return saveChanges;
            });
        } catch (final IOException e) {
            throw new ResourceDiscoveryIOException(e);
        }
        return result.getAsBoolean();
    }

    /**
     * Removes all resources from the group.
     * @throws ResourceDiscoveryException Unable to remove resources.
     */
    public final void removeAllResources() throws ResourceDiscoveryException {
        try {
            configurationManager.processConfiguration(config -> {
                final Set<String> resourcesToRemove = new HashSet<>(10);
                for (final Map.Entry<String, ? extends ManagedResourceConfiguration> entry : config.getResources().entrySet())
                    if (Objects.equals(entry.getValue().getGroupName(), groupName) && removeResource(entry.getKey(), entry.getValue()))
                        resourcesToRemove.add(entry.getKey());
                //!!do not replace with config.getResources().keySet().removeAll(resourcesToRemove)
                //because modification tracking is based on map, not on sets produced by map
                //see ModifiableMap
                if (resourcesToRemove.isEmpty())
                    return false;
                else {
                    resourcesToRemove.forEach(config.getResources()::remove);
                    resourcesToRemove.clear();  //help GC
                    return true;
                }
            });
        } catch (final IOException e) {
            throw new ResourceDiscoveryIOException(e);
        }
    }
}
