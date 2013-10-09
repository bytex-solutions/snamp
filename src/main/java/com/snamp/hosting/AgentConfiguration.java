package com.snamp.hosting;

import com.snamp.TimeSpan;

import java.util.Map;

/**
 * Represents agent configuration.
 * @author roman
 */
public interface AgentConfiguration {
    /**
     * Represents hosting configuration (front-end configuration).
     */
    public static interface HostingConfiguration {
        /**
         * Represents the port number for SNMP agent,
         */
        static final int snmpDefaultPort = 161;

        /**
         * Represents the default hosting IP-address.
         */
        static final String defaultAddress = "0.0.0.0";

        /**
         * Gets the agent hosting port.
         * @return The agent hosting port.
         */
        public int getPort();

        /**
         * Sets the agent hosting port.
         * @param port The port number.
         */
        public void setPort(final int port);

        /**
         * Gets the agent hosting address (such as IP binding).
         * @return The hosting address.
         */
        public String getAddress();

        /**
         * Sets the agent hosting address.
         * @param address The agent hosting address.
         */
        public void setAddress(final String address);
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
            public Map<String, Object> getAdditionalElements();
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
        public Map<String, Object> getAdditionalElements();

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
}
