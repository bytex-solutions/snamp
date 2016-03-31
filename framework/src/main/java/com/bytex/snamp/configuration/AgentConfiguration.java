package com.bytex.snamp.configuration;

import com.bytex.snamp.TimeSpan;

import java.util.Map;

/**
 * Represents in-memory representation of the agent configuration.
 * <p>The agent configuration consists of the following parts:
 * <ul>
 *     <li>Hosting configuration - contains configuration of the adapter.</li>
 *     <li>Configuration of the managed resources.</li>
 * </ul><br/>
 * Hosting configuration describes configuration of the adapter, that exposes
 * the management information to the outside world.
 * This configuration part contains adapter name (name of the Adapter Plug-in) and
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
 * @version 1.2
 */
public interface AgentConfiguration extends Cloneable {
    /**
     * Represents a root interface for all agent configuration entities.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    interface EntityConfiguration {
        /**
         * The name of the parameter which contains description of the configuration entity.
         */
        String DESCRIPTION_KEY = "description";

        /**
         * The name of the parameter which used for grouping entities.
         */
        String GROUP_KEY = "group";

        /**
         * Gets configuration parameters of this entity.
         * @return A map of configuration parameters.
         */
        Map<String, String> getParameters();
    }

    /**
     * Represents catalog of configuration entities.
     * @param <E> Type of the configuration entities in the catalog.
     */
    interface EntityMap<E extends EntityConfiguration> extends Map<String, E>{
        /**
         * Gets existing configuration entity; or creates and registers a new entity.
         * @param entityID Identifier of the configuration entity.
         * @return Configuration entity from the catalog.
         */
        E getOrAdd(final String entityID);
    }

    /**
     * Represents hosting configuration (front-end configuration).
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    interface ResourceAdapterConfiguration extends EntityConfiguration {
        /**
         * Gets the hosting adapter name.
         * @return The hosting adapter name.
         */
        String getAdapterName();

        /**
         * Sets the hosting adapter name.
         * @param adapterName The adapter name.
         */
        void setAdapterName(final String adapterName);
    }

    /**
     * Creates clone of this configuration.
     * @return The cloned instance of this configuration.
     */
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    AgentConfiguration clone();

    /**
     * Represents management target configuration (back-end management information providers).
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    interface ManagedResourceConfiguration extends EntityConfiguration {

        /**
         * Represents name of the configuration parameter that can be used to enable Smart mode of the connector.
         */
        String SMART_MODE_KEY = "smartMode";

        /**
         * Represents a feature of the managed resource.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.2
         */
        interface FeatureConfiguration extends EntityConfiguration {
            /**
             * Represents configuration parameter containing alternative name of the feature.
             */
            String NAME_KEY = "name";

            /**
             * Represents configuration parameter indicating that this feature was created by machine, not by human.
             */
            String AUTOMATICALLY_ADDED_KEY = "automaticallyAdded";
        }

        /**
         * Represents event configuration.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.2
         */
        interface EventConfiguration extends FeatureConfiguration {
        }

        /**
         * Represents attribute configuration.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.2
         */
        interface AttributeConfiguration extends FeatureConfiguration {
            /**
             * Recommended timeout for read/write of attribute in smart mode.
             */
            TimeSpan TIMEOUT_FOR_SMART_MODE = TimeSpan.ofSeconds(10);

            /**
             * Gets attribute value invoke/write operation timeout.
             * @return Gets attribute value invoke/write operation timeout.
             */
            TimeSpan getReadWriteTimeout();

            /**
             * Sets attribute value invoke/write operation timeout.
             * @param value A new value of the timeout.
             */
            void setReadWriteTimeout(final TimeSpan value);
        }

        /**
         * Represents configuration of the managed resource operation.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.2
         */
        interface OperationConfiguration extends FeatureConfiguration{
            /**
             * Recommended timeout for invocation of operation in smart mode.
             */
            TimeSpan TIMEOUT_FOR_SMART_MODE = TimeSpan.ofSeconds(10);

            /**
             * Gets timeout of operation invocation.
             * @return Timeout value.
             */
            TimeSpan getInvocationTimeout();

            /**
             * Sets timeout of operation invocation.
             * @param value A new timeout value.
             */
            void setInvocationTimeout(final TimeSpan value);
        }

        /**
         * Gets the management target connection string.
         * @return The connection string that is used to connect to the management server.
         */
        String getConnectionString();

        /**
         * Sets the management target connection string.
         * @param connectionString The connection string that is used to connect to the management server.
         */
        void setConnectionString(final String connectionString);

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         * @return The management connector type.
         */
        String getConnectionType();

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         * @param connectorType The management connector type.
         */
        void setConnectionType(final String connectorType);

        /**
         * Gets a collection of configured manageable elements for this target.
         * @param featureType The type of the manageable element.
         * @param <T> The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType);

        /**
         * Returns the dictionary of additional configuration parameters.
         * @return The dictionary of additional configuration parameters.
         */
        Map<String, String> getParameters();
    }

    /**
     * Gets a collection of resource adapters.
     * <p>
     *     The key represents user-defined unique name of the adapter.
     * </p>
     * @return A collection of resource adapters.
     */
    EntityMap<? extends ResourceAdapterConfiguration> getResourceAdapters();

    /**
     * Gets a collection of managed resources.
     * <p>
     *     The key represents user-defined name of the managed resource.
     * </p>
     * @return The dictionary of managed resources.
     */
    EntityMap<? extends ManagedResourceConfiguration> getManagedResources();

    /**
     * Imports the state of specified object into this object.
     * @param input The import source.
     */
    void load(final AgentConfiguration input);

    /**
     * Clears this configuration.
     */
    void clear();
}
