package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.ReferenceCountedObject;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.AbstractConcurrentResourceAccess.*;

/**
 * Represents SNMP-compliant managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceConnector extends AbstractManagedResourceConnector<SnmpConnectionOptions> implements AttributeSupport {
    private static final SnmpTypeSystem typeSystem = new SnmpTypeSystem();

    private static final class SnmpAttributeMetadata extends GenericAttributeMetadata<SnmpManagementEntityType>{
        private final SnmpManagementEntityType attributeType;
        private final Map<String, String> options;

        public SnmpAttributeMetadata(final OID attributeID, final SnmpManagementEntityType entityType, final Map<String, String> options){
            super(attributeID.toDottedString());
            this.attributeType = entityType;
            this.options = Collections.unmodifiableMap(options);
        }

        public final OID getAttributeID(){
            return new OID(getName());
        }

        /**
         * Detects the attribute type (this method will be called by infrastructure once).
         *
         * @return Detected attribute type.
         */
        @Override
        protected SnmpManagementEntityType detectAttributeType() {
            return attributeType;
        }

        @Override
        public int size() {
            return options.size();
        }

        @Override
        public boolean isEmpty() {
            return options.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return options.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return options.containsValue(value);
        }

        @Override
        public String get(final Object key) {
            return options.get(key);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set<String> keySet() {
            return options.keySet();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Collection<String> values() {
            return options.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Entry<String, String>> entrySet() {
            return options.entrySet();
        }
    }

    private static final class SnmpAttributeSupport extends AbstractAttributeSupport implements AutoCloseable{
        private final ReferenceCountedObject<SnmpClient> client;
        private final Logger logger;

        public SnmpAttributeSupport(final ReferenceCountedObject<SnmpClient> client, final Logger log){
            this.client = client;
            this.logger = log;
        }

        private SnmpAttributeMetadata connectAttribute(final OID attributeID, final Map<String, String> options) throws Exception{
            final Variable value = client.increfAndRead(new Action<SnmpClient, Variable, Exception>() {
                @Override
                public Variable invoke(final SnmpClient client) throws Exception {
                    return client.get(attributeID, TimeSpan.fromSeconds(5));
                }
            });
            if(value == null) throw new IOException(String.format("Attribute %s doesn't exist on SNMP agent", attributeID));
            final SnmpManagementEntityType attributeType = typeSystem.resolveSnmpScalarType(value, options);
            if(attributeType == null) throw new Exception(String.format("Type of the attribute %s cannot be determined. SMI syntax is %s. Wrapped is %s.", attributeID, value.getSyntax(), value.getClass()));
            return new SnmpAttributeMetadata(attributeID, attributeType, options);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The name of the attribute.
         * @param options       Attribute discovery options.
         * @return The description of the attribute.
         */
        @Override
        protected SnmpAttributeMetadata connectAttribute(final String attributeName, final Map<String, String> options) {
            try {
                return connectAttribute(new OID(attributeName), options);
            }
            catch (final Exception e) {
                logger.log(Level.SEVERE, String.format("Unable to connect attribute %s", attributeName), e);
            }
            return null;
        }

        /**
         * Removes the attribute from the connector.
         *
         * @param id            The unique identifier of the attribute.
         * @param attributeInfo An attribute metadata.
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @Override
        protected boolean disconnectAttribute(final String id, final GenericAttributeMetadata<?> attributeInfo) {
            if(super.disconnectAttribute(id, attributeInfo))
                try {
                    client.decref();
                    return true;
                }
                catch (final Exception e) {
                    logger.log(Level.WARNING, String.format("Some problems occurred during disconnection of attribute %s", id), e);
                    return true;
                }
            else return false;
        }

        private Variable getAttributeValue(final SnmpAttributeMetadata metadata, final TimeSpan readTimeout) throws Exception{
            return client.read(new Action<SnmpClient, Variable, Exception>() {
                @Override
                public Variable invoke(final SnmpClient client) throws Exception {
                    return client.get(metadata.getAttributeID(), readTimeout);
                }
            });
        }

        /**
         * Returns the value of the attribute.
         *
         * @param attribute    The metadata of the attribute to get.
         * @param readTimeout  The attribute value invoke operation timeout.
         * @param defaultValue The default value of the attribute if reading fails.
         * @return The value of the attribute.
         * @throws java.util.concurrent.TimeoutException
         */
        @Override
        protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
            try {
                return attribute instanceof SnmpAttributeMetadata ? getAttributeValue((SnmpAttributeMetadata)attribute, readTimeout) : defaultValue;
            }
            catch (final Exception e) {
                logger.log(Level.WARNING, String.format("Unable to get value of attribute %s", attribute.getName()), e);
                return null;
            }
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout The attribute value write operation timeout.
         * @param value        The value to write.
         * @return {@literal true} if attribute value is overridden successfully; otherwise, {@literal false}.
         */
        @Override
        protected boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
            return attribute instanceof SnmpAttributeMetadata && setAttributeValue((SnmpAttributeMetadata)attribute, writeTimeout, value);
        }

        private boolean setAttributeValue(final SnmpAttributeMetadata attribute, final TimeSpan writeTimeout, final Object value){
            return client.write(new ConsistentAction<SnmpClient, Boolean>() {
                @Override
                public Boolean invoke(final SnmpClient client) {
                    try {
                        client.set(Collections.singletonMap(attribute.getAttributeID(),
                                attribute.getType().convertToSnmp(value).get(new OID())), writeTimeout);
                        return true;
                    }
                    catch (final Exception e) {
                        logger.log(Level.WARNING, String.format("Unable to modify %s attribute", attribute.getAttributeID()), e);
                        return false;
                    }
                }
            });
        }

        @Override
        public void close() throws Exception {
            try {
                client.decref(attributesCount());
            }
            finally {
                clear();
            }
        }
    }

    private final SnmpAttributeSupport attributes;

    /**
     * Initializes a new management connector.
     * @param snmpConnectionOptions Management connector initialization options.
     * @throws java.io.IOException Unable to instantiate SNMP client.
     */
    public SnmpResourceConnector(final SnmpConnectionOptions snmpConnectionOptions) throws IOException {
        super(snmpConnectionOptions, SnmpConnectorHelpers.getLogger());
        final ReferenceCountedObject<SnmpClient> client = new ReferenceCountedObject<SnmpClient>() {
            @Override
            protected SnmpClient createResource() throws IOException {
                final SnmpClient client = snmpConnectionOptions.createSnmpClient();
                client.listen();
                return client;
            }

            @Override
            protected void cleanupResource(final SnmpClient resource) throws IOException {
                resource.close();
            }
        };
        attributes = new SnmpAttributeSupport(client, getLogger());
    }

    public SnmpResourceConnector(final String connectionString, final Map<String, String> parameters) throws IOException {
        this(new SnmpConnectionOptions(connectionString, parameters));
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id            A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options       The attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    public AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, options);
    }

    /**
     * Returns the attribute value.
     *
     * @param id           A key string that is used to invoke attribute from this connector.
     * @param readTimeout  The attribute value invoke operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        verifyInitialization();
        return attributes.getAttribute(id, readTimeout, defaultValue);
    }

    /**
     * Reads a set of managementAttributes.
     *
     * @param output      The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        verifyInitialization();
        return attributes.getAttributes(output, readTimeout);
    }

    /**
     * Writes the value of the specified attribute.
     *
     * @param id           An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value        The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be write in the specified duration.
     */
    @Override
    public boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException {
        verifyInitialization();
        return attributes.setAttribute(id, writeTimeout, value);
    }

    /**
     * Writes a set of managementAttributes inside of the transaction.
     *
     * @param values       The dictionary of managementAttributes keys and its values.
     * @param writeTimeout The attribute value write operation timeout.
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws java.util.concurrent.TimeoutException
     */
    @Override
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException {
        verifyInitialization();
        return attributes.setAttributes(values, writeTimeout);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public boolean disconnectAttribute(final String id) {
        verifyInitialization();
        return attributes.disconnectAttribute(id);
    }

    /**
     * Returns the information about the connected attribute.
     *
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    @Override
    public AttributeMetadata getAttributeInfo(final String id) {
        verifyInitialization();
        return attributes.getAttributeInfo(id);
    }

    /**
     * Returns a read-only collection of registered IDs of managementAttributes.
     *
     * @return A read-only collection of registered IDs of managementAttributes.
     */
    @Override
    public Collection<String> getConnectedAttributes() {
        return attributes.getConnectedAttributes();
    }
}
