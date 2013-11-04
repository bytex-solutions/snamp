package com.snamp.hosting;

import com.snamp.TimeSpan;

import java.io.*;
import java.util.*;

/**
 * Represents embedded agent configuration.
 * @author roman
 */
public class EmbeddedAgentConfiguration extends AbstractAgentConfiguration implements Serializable {

    public static final class EmbeddedHostingConfiguration implements HostingConfiguration{
        private String adapterName;
        private final Map<String, String> additionalElements;

        public EmbeddedHostingConfiguration(){
            adapterName = "";
            additionalElements = new HashMap<>(10);
        }

        /**
         * Gets the hosting adapter name.
         *
         * @return
         */
        @Override
        public final String getAdapterName() {
            return adapterName;
        }

        /**
         * Sets the hosting adapter name.
         *
         * @param adapterName The adapter name.
         */
        @Override
        public final void setAdapterName(final String adapterName) {
            this.adapterName = adapterName != null ? adapterName : "";
        }

        /**
         * Returns a dictionary of hosting parameters, such as port and hosting address.
         *
         * @return
         */
        @Override
        public Map<String, String> getHostingParams() {
            return getHostingParams();
        }
    }

    public static final class EmbeddedManagementTargetConfiguration implements ManagementTargetConfiguration{

        public static final class EmbeddedAttributeConfiguration implements AttributeConfiguration{
            private TimeSpan readWriteTimeout;
            private String attributeName;
            private final Map<String, String> additionalElements;

            public EmbeddedAttributeConfiguration(){
                readWriteTimeout = TimeSpan.INFINITE;
                attributeName = "";
                additionalElements = new HashMap<>();
            }

            public EmbeddedAttributeConfiguration(final String attributeName){
                this();
                this.attributeName = attributeName;
            }

            /**
             * Gets attribute value read/write operation timeout.
             *
             * @return
             */
            @Override
            public final TimeSpan getReadWriteTimeout() {
                return readWriteTimeout;
            }

            /**
             * Sets attribute value read/write operation timeout.
             */
            @Override
            public final void setReadWriteTimeout(final TimeSpan time) {
                this.readWriteTimeout = time;
            }

            /**
             * Returns the attribute name.
             *
             * @return The attribute name,
             */
            @Override
            public final String getAttributeName() {
                return attributeName;
            }

            /**
             * Sets the attribute name.
             *
             * @param attributeName The attribute name.
             */
            @Override
            public final void setAttributeName(final String attributeName) {
                this.attributeName = attributeName != null ? attributeName : "";
            }

            /**
             * Returns the additional configuration elements.
             *
             * @return
             */
            @Override
            public final Map<String, String> getAdditionalElements() {
                return additionalElements;
            }
        }

        private String connectionString;
        private final Map<String, AttributeConfiguration> attributes;
        private String connectionType;
        private String namespace;
        private final Map<String, String> additionalElements;

        public EmbeddedManagementTargetConfiguration(){
            connectionString = connectionType = namespace = "";
            attributes = new HashMap<>(10);
            additionalElements = new HashMap<>(10);
        }

        /**
         * Gets the management target connection string.
         *
         * @return The connection string that is used to connect to the management server.
         */
        @Override
        public final String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the management target connection string.
         *
         * @param connectionString The connection string that is used to connect to the management server.
         */
        @Override
        public final void setConnectionString(final String connectionString) {
            this.connectionString = connectionString != null ? connectionString : "";
        }

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @return The management connector type.
         */
        @Override
        public final String getConnectionType() {
            return connectionType;
        }

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @param connectorType The management connector type.
         */
        @Override
        public final void setConnectionType(final String connectorType) {
            this.connectionType = connectionType != null ? connectorType : "";
        }

        /**
         * Returns the monitoring namespace that is visible outside from the agent and the front-end.
         *
         * @return The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        public final String getNamespace() {
            return namespace;
        }

        /**
         * Sets the monitoring namespace.
         *
         * @param namespace The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        public final void setNamespace(final String namespace) {
            this.namespace = namespace != null ? namespace : "";
        }

        /**
         * Returns the management attributes (key is a attribute identifier).
         *
         * @return The dictionary of management attributes.
         */
        @Override
        public final Map<String, AttributeConfiguration> getAttributes() {
            return attributes;
        }

        /**
         * Returns the dictionary of additional configuration elements.
         *
         * @return The dictionary of additional configuration elements.
         */
        @Override
        public final Map<String, String> getAdditionalElements() {
            return additionalElements;
        }

        /**
         * Empty implementation of AttributeConfiguration interface
         *
         * @return implementation of AttributeConfiguration interface
         */
        @Override
        public final EmbeddedAttributeConfiguration newAttributeConfiguration() {
            return new EmbeddedAttributeConfiguration();
        }
    }

    private final EmbeddedHostingConfiguration hostingConfig;
    private final Map<String, ManagementTargetConfiguration> targets;

    public EmbeddedAgentConfiguration(){
        hostingConfig = new EmbeddedHostingConfiguration();
        targets = new HashMap<>(10);
    }

    /**
     * Clones this instance of agent configuration.
     *
     * @return
     */
    @Override
    public EmbeddedAgentConfiguration clone() {
        final EmbeddedAgentConfiguration clonedConfig = new EmbeddedAgentConfiguration();
        clonedConfig.load(this);
        return clonedConfig;
    }

    /**
     * Returns the agent hosting configuration.
     *
     * @return The agent hosting configuration.
     */
    @Override
    public final EmbeddedHostingConfiguration getAgentHostingConfig() {
        return hostingConfig;
    }

    /**
     * Represents management targets.
     *
     * @return The dictionary of management targets (management back-ends).
     */
    @Override
    public final Map<String, ManagementTargetConfiguration> getTargets() {
        return targets;
    }

    /**
     * Empty implementation of ManagementTargetConfiguration interface
     *
     * @return implementation of ManagementTargetConfiguration interface
     */
    @Override
    public final EmbeddedManagementTargetConfiguration newManagementTargetConfiguration() {
        return new EmbeddedManagementTargetConfiguration();
    }

    /**
     * Serializes this object into the specified stream.
     *
     * @param output
     * @throws java.io.IOException           Some I/O error occurs.
     */
    @Override
    public final void save(final OutputStream output) throws IOException {
        try(final ObjectOutputStream serializer = new ObjectOutputStream(output)){
            serializer.writeObject(this);
        }
    }

    /**
     * Reads the file and fills the current instance.
     *
     * @param input
     * @throws java.io.IOException           Cannot read from the specified stream.
     */
    @Override
    public final void load(final InputStream input) throws IOException {
        try(final ObjectInputStream deserializer = new ObjectInputStream(input)){
            load((AgentConfiguration)deserializer.readObject());
        }
        catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
