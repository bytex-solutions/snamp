package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;

/**
 * Represents in-memory representation of the agent configuration.
 * <p>The agent configuration consists of the following parts:
 * <ul>
 *     <li>Configuration of gateways</li>
 *     <li>Configuration of the managed resources.</li>
 * </ul><br/>
 * Gateway configuration describes configuration of the gateway instance, that exposes
 * the management information to the outside world.
 * This configuration part contains gateway type and
 * additional elements, such as port number and host name.<br/>
 * Each managed resource configuration contains information about management information source in the form
 * of the following elements:
 * <ul>
 *     <li>Connection string - source-specific string, that describes management information source.</li>
 *     <li>Connection type - name of the connector plug-in that is used to organize management information exchange with source.</li>
 *     <li>Management attributes - a set of atomic management entities that supplies management data.</li>
 * </ul><br/>
 * Each management attribute describes the single entry in the remote management information database. This
 * entry can have getter or setter for its value.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface AgentConfiguration extends Cloneable, EntityConfiguration {
    /**
     * Global configuration parameter used to setup timeout for features discovery.
     */
    String DISCOVERY_TIMEOUT_PROPERTY = "discoveryTimeout";

    /**
     * Creates clone of this configuration.
     * @return The cloned instance of this configuration.
     * @throws CloneNotSupportedException This configuration cannot be cloned.
     */
    AgentConfiguration clone() throws CloneNotSupportedException;

    @Nonnull
    EntityMap<? extends ManagedResourceConfiguration> getResources();
    @Nonnull
    EntityMap<? extends GatewayConfiguration> getGateways();
    @Nonnull
    EntityMap<? extends ManagedResourceGroupConfiguration> getResourceGroups();
    @Nonnull
    EntityMap<? extends ThreadPoolConfiguration> getThreadPools();

    /**
     * Creates a new instance of entity configuration.
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                  {@link GatewayConfiguration},
     *                  {@link AttributeConfiguration},
     *                  {@link EventConfiguration},
     *                  {@link OperationConfiguration},
     *                  {@link ManagedResourceGroupConfiguration},
     *                  {@link SupervisorConfiguration}.
     * @param <E> Type of requested entity.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    <E extends EntityConfiguration> E createEntityConfiguration(final Class<E> entityType);

    /**
     * Clears this configuration.
     */
    void clear();
}
