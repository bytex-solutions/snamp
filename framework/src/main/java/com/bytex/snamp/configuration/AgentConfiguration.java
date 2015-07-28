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
 * @version 1.0
 */
public interface AgentConfiguration extends Cloneable {
    /**
     * Represents a root interface for all agent configuration entities.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    interface EntityConfiguration {
        /**
         * The name of the parameter which contains description of the configuration entity.
         */
        String DESCRIPTION_KEY = "description";

        /**
         * Gets configuration parameters of this entity.
         * @return A map of configuration parameters.
         */
        Map<String, String> getParameters();
    }

    /**
     * Represents hosting configuration (front-end configuration).
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
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
     * @version 1.0
     */
    interface ManagedResourceConfiguration extends EntityConfiguration {

        /**
         * Represents a feature of the managed resource.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        interface FeatureConfiguration extends EntityConfiguration {
        }

        /**
         * Represents event configuration.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        interface EventConfiguration extends FeatureConfiguration {
            /**
             * Gets the event category.
             * @return The event category.
             */
            String getCategory();

            /**
             * Sets the category of the event to listen.
             * @param eventCategory The category of the event to listen.
             */
            void setCategory(final String eventCategory);
        }

        /**
         * Represents attribute configuration.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        interface AttributeConfiguration extends FeatureConfiguration {
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

            /**
             * Returns the attribute name.
             * @return The attribute name.
             */
            String getAttributeName();

            /**
             * Sets the attribute name.
             * @param attributeName The attribute name.
             */
            void setAttributeName(final String attributeName);
        }

        /**
         * Represents configuration of the managed resource operation.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        interface OperationConfiguration extends FeatureConfiguration{
            /**
             * Gets name of the managed resource operation.
             * @return The name of the managed resource operation.
             */
            String getOperationName();

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

            /**
             * Sets name of the managed resource operation.
             * @param operationName Name of the managed resource operation.
             */
            void setOperationName(final String operationName);
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
         * @param elementType The type of the manageable element.
         * @param <T> The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
         * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
         */
        <T extends FeatureConfiguration> Map<String, T> getElements(final Class<T> elementType);

        /**
         * Creates a new instances of the specified manageable element.
         * @param elementType Type of the required manageable element.
         * @param <T> Type of the required manageable element.
         * @return A new empty manageable element; or {@literal null},
         *      if the specified element type is not supported.
         */
        <T extends FeatureConfiguration> T newElement(final Class<T> elementType);

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
    Map<String, ResourceAdapterConfiguration> getResourceAdapters();

    /**
     * Gets a collection of managed resources.
     * <p>
     *     The key represents user-defined name of the managed resource.
     * </p>
     * @return The dictionary of managed resources.
     */
    Map<String, ManagedResourceConfiguration> getManagedResources();

    /**
     * Creates a new instance of the configuration entity.
     * @param entityType Type of the entity to instantiate.
     * @param <T> Type of the entity to instantiate.
     * @return A new instance of the configuration entity; or {@literal null}, if entity
     * is not supported.
     * @see com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration
     * @see com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration
     */
    <T extends EntityConfiguration> T newConfigurationEntity(final Class<T> entityType);

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
