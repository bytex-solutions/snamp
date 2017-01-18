package com.bytex.snamp.configuration;

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

    /**
     * Obtains a repository of configuration entities.
     * @param entityType Type of entity. You can use {@link ManagedResourceConfiguration} or {@link GatewayConfiguration} as entities.
     * @param <E> Type of entity.
     * @return A repository of configuration entities; or {@literal null}, if entity type is not supported by SNAMP configuration subsystem.
     * @since 1.2
     */
    <E extends EntityConfiguration> EntityMap<? extends E> getEntities(final Class<E> entityType);

    /**
     * Creates a new instance of entity configuration.
     * @param entityType Type of entity. Can be {@link ManagedResourceConfiguration},
     *                  {@link GatewayConfiguration}. {@link AttributeConfiguration}, {@link EventConfiguration}, {@link OperationConfiguration}.
     * @param <E> Type of requested entity.
     * @return A new instance of entity configuration; or {@literal null}, if entity is not supported.
     */
    <E extends EntityConfiguration> E createEntityConfiguration(final Class<E> entityType);

    /**
     * Imports the state of specified object into this object.
     * @param input The import source.
     */
    default void load(final AgentConfiguration input) {
        copy(input, this);
    }

    /**
     * Clears this configuration.
     */
    void clear();

    /**
     * Copies configuration from one object to another object.
     * @param input The configuration import source.
     * @param output The configuration import destination.
     */
    static void copy(final AgentConfiguration input, final AgentConfiguration output) {
        if (input == null || output == null) return;
        //import parameters
        output.load(input);
        //import hosting configuration
        ConfigurationEntityCopier.copy(input.getEntities(GatewayConfiguration.class),
                output.getEntities(GatewayConfiguration.class),
                GatewayConfiguration::copy);
        //import management targets
        ConfigurationEntityCopier.copy(input.getEntities(ManagedResourceConfiguration.class),
                output.getEntities(ManagedResourceConfiguration.class),
                (ConfigurationEntityCopier<ManagedResourceConfiguration>) ManagedResourceConfiguration::copy);
        //import thread pools
        ConfigurationEntityCopier.copy(input.getEntities(ThreadPoolConfiguration.class),
                output.getEntities(ThreadPoolConfiguration.class),
                ThreadPoolConfiguration::copy);
        //import groups
        ConfigurationEntityCopier.copy(input.getEntities(ManagedResourceGroupConfiguration.class),
                output.getEntities(ManagedResourceGroupConfiguration.class),
                (ConfigurationEntityCopier<ManagedResourceGroupConfiguration>) ManagedResourceGroupConfiguration::copy);
    }
}
