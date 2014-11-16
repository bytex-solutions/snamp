package com.itworks.snamp.connectors.rshell;

import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.snamp.ConversionException;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.rshell.RShellConnectorConfigurationDescriptor.COMMAND_PROFILE_PATH_PARAM;

/**
 * Represents RShell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellResourceConnector extends AbstractManagedResourceConnector<RShellConnectionOptions> implements AttributeSupport {
    static String NAME = RShellConnectorHelpers.CONNECTOR_NAME;
    private static final RShellConnectorTypeSystem typeSystem = new RShellConnectorTypeSystem();

    private static final class RShellAttributeMetadata extends GenericAttributeMetadata<ManagedEntityType>{
        private final XmlCommandLineToolProfile commandProfile;
        private final Map<String, String> attributeOptions;

        private RShellAttributeMetadata(final String attributeName,
                                        final XmlCommandLineToolProfile profile,
                                        final Map<String, String> options){
            super(attributeName);
            commandProfile = profile;
            attributeOptions = Collections.unmodifiableMap(options);
        }

        @SuppressWarnings("unchecked")
        Object getValue(final CommandExecutionChannel channel) throws IOException, ScriptException {
            final Object value = commandProfile.readFromChannel(channel, this);
            switch (commandProfile.getReaderTemplate().getCommandOutputParser().getParsingResultType()) {
                case TABLE:
                    return RShellConnectorTypeSystem.toTable((Collection<Map<String, Object>>) value, commandProfile.getReaderTemplate().getCommandOutputParser());
                default:
                    return value;
            }
        }

        boolean setValue(final CommandExecutionChannel channel, final Object value) throws ScriptException, IOException {
            if (TypeLiterals.isInstance(value, TypeLiterals.STRING_COLUMN_TABLE))
                return commandProfile.writeToChannel(channel, RShellConnectorTypeSystem.fromTable(TypeLiterals.cast(value, TypeLiterals.STRING_COLUMN_TABLE)));
            else return commandProfile.writeToChannel(channel, value);
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return commandProfile.getModifierTemplate() != null;
        }

        /**
         * Detects the attribute type (this method will be called by infrastructure once).
         *
         * @return Detected attribute type.
         */
        @Override
        protected ManagedEntityType detectAttributeType() {
            return typeSystem.createEntityType(commandProfile.getReaderTemplate().getCommandOutputParser());
        }

        /**
         * Returns the number of key-value mappings in this map.  If the
         * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
         * <tt>Integer.MAX_VALUE</tt>.
         *
         * @return the number of key-value mappings in this map
         */
        @Override
        public int size() {
            return attributeOptions.size();
        }

        /**
         * Returns <tt>true</tt> if this map contains no key-value mappings.
         *
         * @return <tt>true</tt> if this map contains no key-value mappings
         */
        @Override
        public boolean isEmpty() {
            return attributeOptions.isEmpty();
        }

        /**
         * Returns <tt>true</tt> if this map contains a mapping for the specified
         * key.  More formally, returns <tt>true</tt> if and only if
         * this map contains a mapping for a key <tt>k</tt> such that
         * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
         * at most one such mapping.)
         *
         * @param key key whose presence in this map is to be tested
         * @return <tt>true</tt> if this map contains a mapping for the specified
         * key
         * @throws ClassCastException   if the key is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified key is null and this map
         *                              does not permit null keys
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         */
        @Override
        public boolean containsKey(final Object key) {
            return attributeOptions.containsKey(key);
        }

        /**
         * Returns <tt>true</tt> if this map maps one or more keys to the
         * specified value.  More formally, returns <tt>true</tt> if and only if
         * this map contains at least one mapping to a value <tt>v</tt> such that
         * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
         * will probably require time linear in the map size for most
         * implementations of the <tt>Map</tt> interface.
         *
         * @param value value whose presence in this map is to be tested
         * @return <tt>true</tt> if this map maps one or more keys to the
         * specified value
         * @throws ClassCastException   if the value is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified value is null and this
         *                              map does not permit null values
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         */
        @Override
        public boolean containsValue(final Object value) {
            return attributeOptions.containsValue(value);
        }

        /**
         * Returns the value to which the specified key is mapped,
         * or {@code null} if this map contains no mapping for the key.
         * <p/>
         * <p>More formally, if this map contains a mapping from a key
         * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
         * key.equals(k))}, then this method returns {@code v}; otherwise
         * it returns {@code null}.  (There can be at most one such mapping.)
         * <p/>
         * <p>If this map permits null values, then a return value of
         * {@code null} does not <i>necessarily</i> indicate that the map
         * contains no mapping for the key; it's also possible that the map
         * explicitly maps the key to {@code null}.  The {@link #containsKey
         * containsKey} operation may be used to distinguish these two cases.
         *
         * @param key the key whose associated value is to be returned
         * @return the value to which the specified key is mapped, or
         * {@code null} if this map contains no mapping for the key
         * @throws ClassCastException   if the key is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified key is null and this map
         *                              does not permit null keys
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         */
        @Override
        public String get(final Object key) {
            return attributeOptions.get(key);
        }

        /**
         * Returns a {@link java.util.Set} view of the keys contained in this map.
         * The set is backed by the map, so changes to the map are
         * reflected in the set, and vice-versa.  If the map is modified
         * while an iteration over the set is in progress (except through
         * the iterator's own <tt>remove</tt> operation), the results of
         * the iteration are undefined.  The set supports element removal,
         * which removes the corresponding mapping from the map, via the
         * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
         * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
         * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
         * operations.
         *
         * @return a set view of the keys contained in this map
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Set<String> keySet() {
            return attributeOptions.keySet();
        }

        /**
         * Returns a {@link java.util.Collection} view of the values contained in this map.
         * The collection is backed by the map, so changes to the map are
         * reflected in the collection, and vice-versa.  If the map is
         * modified while an iteration over the collection is in progress
         * (except through the iterator's own <tt>remove</tt> operation),
         * the results of the iteration are undefined.  The collection
         * supports element removal, which removes the corresponding
         * mapping from the map, via the <tt>Iterator.remove</tt>,
         * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
         * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
         * support the <tt>add</tt> or <tt>addAll</tt> operations.
         *
         * @return a collection view of the values contained in this map
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Collection<String> values() {
            return attributeOptions.values();
        }

        /**
         * Returns a {@link java.util.Set} view of the mappings contained in this map.
         * The set is backed by the map, so changes to the map are
         * reflected in the set, and vice-versa.  If the map is modified
         * while an iteration over the set is in progress (except through
         * the iterator's own <tt>remove</tt> operation, or through the
         * <tt>setValue</tt> operation on a map entry returned by the
         * iterator) the results of the iteration are undefined.  The set
         * supports element removal, which removes the corresponding
         * mapping from the map, via the <tt>Iterator.remove</tt>,
         * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
         * <tt>clear</tt> operations.  It does not support the
         * <tt>add</tt> or <tt>addAll</tt> operations.
         *
         * @return a set view of the mappings contained in this map
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Entry<String, String>> entrySet() {
            return attributeOptions.entrySet();
        }
    }

    private static final class RShellAttributes extends AbstractAttributeSupport {

        private final CommandExecutionChannel executionChannel;
        private final Logger logger;

        private RShellAttributes(final CommandExecutionChannel channel, final Logger logger) {
            this.executionChannel = channel;
            this.logger = logger;
        }

        /**
         * Reports an error when connecting attribute.
         *
         * @param attributeID   The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
        }

        /**
         * Reports an error when getting attribute.
         *
         * @param attributeID The attribute identifier.
         * @param e           Internal connector error.
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            failedToGetAttribute(logger, Level.WARNING, attributeID, e);
        }

        /**
         * Reports an error when updating attribute.
         *
         * @param attributeID The attribute identifier.
         * @param value       The value of the attribute.
         * @param e           Internal connector error.
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
         */
        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The name of the attribute.
         * @param options       Attribute discovery options.
         * @return The description of the attribute.
         * @throws com.itworks.snamp.connectors.rshell.CommandProfileNotFoundException Path to the command profile is specified but file not found.
         */
        @Override
        protected GenericAttributeMetadata<?> connectAttribute(final String attributeName, final Map<String, String> options) throws CommandProfileNotFoundException, UndefinedCommandProfileException {
            if (options.containsKey(COMMAND_PROFILE_PATH_PARAM)) {
                final XmlCommandLineToolProfile profile = XmlCommandLineToolProfile.loadFrom(new File(options.get(COMMAND_PROFILE_PATH_PARAM)));
                if (profile != null)
                    return new RShellAttributeMetadata(attributeName, profile, options);
                else
                    throw new CommandProfileNotFoundException(options.get(COMMAND_PROFILE_PATH_PARAM));
            }
            throw new UndefinedCommandProfileException();
        }

        /**
         * Returns the value of the attribute.
         *
         * @param attribute   The metadata of the attribute to get.
         * @param readTimeout The attribute value invoke operation timeout.
         * @return The value of the attribute.
         * @throws java.io.IOException          Some I/O error occurred.
         * @throws javax.script.ScriptException Attempt to interpret incorrect portion of script code.
         */
        @Override
        protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout) throws IOException, ScriptException {
            if (attribute instanceof RShellAttributeMetadata)
                return ((RShellAttributeMetadata) attribute).getValue(executionChannel);
            else throw new ConversionException(attribute, RShellAttributeMetadata.class);
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout The attribute value write operation timeout.
         * @param value        The value to write.
         * @throws java.io.IOException          Some I/O error occurred.
         * @throws javax.script.ScriptException Attempt to interpret incorrect portion of script code.
         */
        @Override
        protected void setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws ScriptException, IOException {
            if (attribute instanceof RShellAttributeMetadata)
                ((RShellAttributeMetadata) attribute).setValue(executionChannel, value);
            else throw new ConversionException(attribute, RShellAttributeMetadata.class);
        }
    }

    private final CommandExecutionChannel executionChannel;
    private final RShellAttributes attributes;

    /**
     * Initializes a new management connector.
     *
     * @param connectionOptions Management connector initialization options.
     * @param logger                  A logger for this management connector.
     */
    RShellResourceConnector(final RShellConnectionOptions connectionOptions, final Logger logger) throws Exception {
        super(connectionOptions, logger);
        executionChannel = connectionOptions.createExecutionChannel();
        attributes = new RShellAttributes(executionChannel, logger);
    }

    RShellResourceConnector(final String connectionString,
                                   final Map<String, String> connectionOptions,
                                   final Logger logger) throws Exception{
        this(new RShellConnectionOptions(connectionString, connectionOptions), logger);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id            A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options       The attribute discovery options.
     * @return The description of the attribute.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) throws AttributeSupportException{
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, options);
    }

    /**
     * Returns the attribute value.
     *
     * @param id           A key string that is used to invoke attribute from this connector.
     * @param readTimeout  The attribute value invoke operation timeout.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException Unregistered attribute requested.
     */
    @Override
    public Object getAttribute(final String id, final TimeSpan readTimeout) throws TimeoutException, AttributeSupportException, UnknownAttributeException {
        verifyInitialization();
        return attributes.getAttribute(id, readTimeout);
    }

    /**
     * Reads a set of managementAttributes.
     *
     * @param output      The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException, AttributeSupportException {
        verifyInitialization();
        return attributes.getAttributes(output, readTimeout);
    }

    /**
     * Writes the value of the specified attribute.
     *
     * @param id           An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value        The value to write.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be write in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException Unregistered attibute requested.
     */
    @Override
    public void setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException, AttributeSupportException, UnknownAttributeException {
        verifyInitialization();
        attributes.setAttribute(id, writeTimeout, value);
    }

    /**
     * Writes a set of managementAttributes inside of the transaction.
     *
     * @param values       The dictionary of managementAttributes keys and its values.
     * @param writeTimeout The attribute value write operation timeout.
     * @throws java.util.concurrent.TimeoutException
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    @Override
    public void setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException, AttributeSupportException {
        verifyInitialization();
        attributes.setAttributes(values, writeTimeout);
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

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        try {
            executionChannel.close();
        } finally {
            super.close();
        }
    }
}
