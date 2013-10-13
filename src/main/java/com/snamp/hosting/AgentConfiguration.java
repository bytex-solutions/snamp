package com.snamp.hosting;

import com.snamp.*;

import java.io.*;
import java.util.Map;

/**
 * Represents agent configuration.
 * @author roman
 */
public interface AgentConfiguration extends BinarySerializable {
    /**
     * Represents hosting configuration (front-end configuration).
     */
    public static interface HostingConfiguration {
        /**
         * Gets the hosting adapter name.
         * @return
         */
        public String getAdapterName();

        /**
         * Sets the hosting adapter name.
         * @param adapterName The adapter name.
         */
        public void setAdapterName(final String adapterName);

        /**
         * Returns a dictionary of hosting parameters, such as port and hosting address.
         * @return
         */
        public Map<String, String> getHostingParams();
    }

    /**
     * Represents management target configuration (back-end management information providers).
     */
    public static interface ManagementTargetConfiguration {

        /**
         * Represents attribute configuration.
         */
        public static interface AttributeConfiguration {
            /**
             * Gets attribute value read/write operation timeout.
             * @return
             */
            public TimeSpan getReadWriteTimeout();

            /**
             * Sets attribute value read/write operation timeout.
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
             * @return
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
         * Returns the dictionary of additional configuration elements.
         * @return The dictionary of additional configuration elements.
         */
        public Map<String, String> getAdditionalElements();

        /**
         * Empty implementation of AttributeConfiguration interface
         * @return implementation of AttributeConfiguration interface
         */
        public AttributeConfiguration newAttributeConfiguration();
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

    @Override
    public void save(final OutputStream output) throws UnsupportedOperationException, IOException;

    /**
     * Reads the file and fills the current instance.
     * @param input
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws IOException Cannot read from the specified stream.
     */
    @Override
    public void load(final InputStream input) throws UnsupportedOperationException, IOException;
}
