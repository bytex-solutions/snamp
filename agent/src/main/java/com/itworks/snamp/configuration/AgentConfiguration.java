package com.itworks.snamp.configuration;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.BinarySerializable;

import java.io.*;
import java.util.*;

/**
 * Represents in-memory representation of the agent configuration.
 * <p>The agent configuration consists of the following parts:
 * <ul>
 *     <li>Hosting configuration - contains configuration of the adapter.</li>
 *     <li>Management targets - set of management information sources.</li>
 * </ul><br/>
 * Hosting configuration describes configuration of the adapter, that exposes
 * the management information to the outside world.
 * This configuration part contains adapter name (name of the Adapter Plug-in) and
 * additional elements, such as port number and host name.<br/>
 * Each management target contains information about management information source in the form
 * of the following elements:
 * <ul>
 *     <li>Connection string - source-specific string, that describes management information source.</li>
 *     <li>Connection type - name of the connector plug-in that is used to organize management information exchange with source.</li>
 *     <li>Management attributes - a set of atomic management entity that supplies management data.</li>
 * </ul><br/>
 * Each management attribute describes the single entry in the remote management information database. This
 * entry can have getter or setter for its value.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface AgentConfiguration extends BinarySerializable, Cloneable {
    /**
     * Represents a root interface for all agent configuration entities.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface ConfigurationEntity{
    }

    /**
     * Represents hosting configuration (front-end configuration).
     */
    public static interface HostingConfiguration extends ConfigurationEntity {
        /**
         * Gets the hosting adapter name.
         * @return The hosting adapter name.
         */
        public String getAdapterName();

        /**
         * Sets the hosting adapter name.
         * @param adapterName The adapter name.
         */
        public void setAdapterName(final String adapterName);

        /**
         * Returns a dictionary of hosting parameters, such as port and hosting address.
         * @return The map of additional configuration elements.
         */
        public Map<String, String> getHostingParams();
    }

    /**
     * Creates clone of this configuration.
     * @return The cloned instance of this configuration.
     */
    public AgentConfiguration clone();

    /**
     * Represents management target configuration (back-end management information providers).
     */
    public static interface ManagementTargetConfiguration extends ConfigurationEntity {

        /**
         * Represents event configuration.
         */
        public static interface EventConfiguration extends ConfigurationEntity{
            /**
             * Gets the event category.
             * @return The event category.
             */
            public String getCategory();

            /**
             * Sets the category of the event to listen.
             * @param eventCategory The category of the event to listen.
             */
            public void setCategory(final String eventCategory);

            /**
             * Gets a map of event options.
             * @return The map of event options.
             */
            public Map<String, String> getAdditionalElements();
        }

        /**
         * Represents attribute configuration.
         */
        public static interface AttributeConfiguration extends ConfigurationEntity {
            /**
             * Gets attribute value invoke/write operation timeout.
             * @return Gets attribute value invoke/write operation timeout.
             */
            public TimeSpan getReadWriteTimeout();

            /**
             * Sets attribute value invoke/write operation timeout.
             */
            public void setReadWriteTimeout(TimeSpan time);

            /**
             * Returns the attribute name.
             * @return The attribute name,
             */
            public String getAttributeName();

            /**
             * Sets the attribute name.
             * @param attributeName The attribute name.
             */
            public void setAttributeName(final String attributeName);

            /**
             * Returns the additional configuration elements.
             * @return The map of additional configuration elements.
             */
            public Map<String, String> getAdditionalElements();
        }

        /**
         * Gets the management target connection string.
         * @return The connection string that is used to connect to the management server.
         */
        public String getConnectionString();

        /**
         * Sets the management target connection string.
         * @param connectionString The connection string that is used to connect to the management server.
         */
        public void setConnectionString(final String connectionString);

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         * @return The management connector type.
         */
        public String getConnectionType();

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         * @param connectorType The management connector type.
         */
        public void setConnectionType(final String connectorType);

        /**
         * Returns the monitoring namespace that is visible outside from the agent and the front-end.
         * @return The namespace of the management target (such as SNMP OID prefix).
         */
        public String getNamespace();

        /**
         * Sets the monitoring namespace.
         * @param namespace The namespace of the management target (such as SNMP OID prefix).
         */
        public void setNamespace(final String namespace);

        /**
         * Returns the management attributes (key is a attribute identifier).
         * @return The dictionary of management attributes.
         */
        public Map<String, AttributeConfiguration> getAttributes();

        /**
         * Returns the event sources.
         * @return A set of event sources.
         */
        public Map<String, EventConfiguration> getEvents();

        /**
         * Returns the dictionary of additional configuration elements.
         * @return The dictionary of additional configuration elements.
         */
        public Map<String, String> getAdditionalElements();

        /**
         * Creates an empty attribute configuration.
         * <p>
         *     Usually, this method is used for adding new attributes in the map
         *     returned by {@link #getAttributes()} method.
         * </p>
         * @return An empty attribute configuration.
         */
        public AttributeConfiguration newAttributeConfiguration();

        /**
         * Creates an empty event configuration.
         * <p>
         *     Usually, this method is used for adding new events in the collection
         *     returned by {@link #getEvents()} method.
         * </p>
         * @return An empty event configuration.
         */
        public EventConfiguration newEventConfiguration();
    }

    /**
     * Returns the agent hosting configuration.
     * @return The agent hosting configuration.
     */
    public HostingConfiguration getAgentHostingConfig();

    /**
     * Represents management targets.
     * @return The dictionary of management targets (management back-ends).
     */
    public Map<String, ManagementTargetConfiguration> getTargets();

    /**
     * Empty implementation of ManagementTargetConfiguration interface
     * @return implementation of ManagementTargetConfiguration interface
     */
    public ManagementTargetConfiguration newManagementTargetConfiguration();

    /**
     * Serializes this object into the specified stream.
     * @param output
     * @throws UnsupportedOperationException Serialization is not supported.
     * @throws IOException Some I/O error occurs.
     */
    @Override
    public void save(final OutputStream output) throws UnsupportedOperationException, IOException;

    /**
     * Reads the file and fills the current instance.
     * @param input
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws IOException Cannot invoke from the specified stream.
     */
    @Override
    public void load(final InputStream input) throws UnsupportedOperationException, IOException;

    /**
     * Imports the state of specified object into this object.
     * @param input The import source.
     */
    public void load(final AgentConfiguration input);
}
