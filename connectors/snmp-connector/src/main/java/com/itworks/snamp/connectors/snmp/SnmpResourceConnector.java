package com.itworks.snamp.connectors.snmp;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.itworks.snamp.ConversionException;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.itworks.snamp.concurrent.ConcurrentResourceAccessor;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.ManagedEntityValue;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.licensing.LicensingException;
import com.itworks.snamp.mapping.KeyedRecordSet;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.smi.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.concurrent.AbstractConcurrentResourceAccessor.Action;
import static com.itworks.snamp.concurrent.AbstractConcurrentResourceAccessor.ConsistentAction;
import static com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory.ExceptionHandler;
import static com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker;
import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.*;

/**
 * Represents SNMP-compliant managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceConnector extends AbstractManagedResourceConnector<SnmpConnectionOptions> implements AttributeSupport, NotificationSupport {
    static final String NAME = SnmpConnectorHelpers.CONNECTOR_NAME;

    private static final class SnmpNotification extends NotificationImpl{
        private SnmpNotification(final Severity severity,
                                 final long sequenceNumber,
                                 final String message,
                                 final Integer32 requestID,
                                 final ManagedEntityValue<?> attachment){
            super(severity,
                    sequenceNumber,
                    new Date(),
                    message,
                    requestID != null ? requestID.toString() : null,
                    attachment);
        }
    }

    private static final class SnmpNotificationMetadata extends GenericNotificationMetadata implements CommandResponder{
        private final Map<String, String> options;
        private final AtomicLong sequenceNumber;
        private final NotificationListenerInvoker listenerInvoker;
        private final SnmpTypeSystem typeSystem;

        private SnmpNotificationMetadata(final OID category,
                                        final Map<String, String> options,
                                        final SnmpTypeSystem typeSystem){
            super(category.toDottedString());
            this.options = Collections.unmodifiableMap(options);
            this.sequenceNumber = new AtomicLong(0L);
            this.listenerInvoker = createInvoker();
            this.typeSystem = typeSystem;
        }

        private static NotificationListenerInvoker createInvoker(){
            return createParallelExceptionResistantInvoker(Executors.newSingleThreadExecutor(), new ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    SnmpConnectorHelpers.log(Level.SEVERE, "Unable to process SNMP notification. Context: null",
                            LogicalOperation.current(), e);
                }
            });
        }

        private OID getNotificationID(){
            return new OID(getCategory());
        }

        public final Severity getSeverity(){
            if(options.containsKey(SEVERITY_PARAM))
                switch (options.get(SEVERITY_PARAM)){
                    case "panic": return Severity.PANIC;
                    case "alert": return Severity.ALERT;
                    case "critical": return Severity.CRITICAL;
                    case "error": return Severity.ERROR;
                    case "warning": return Severity.WARNING;
                    case "notice": return Severity.NOTICE;
                    case "info": return Severity.INFO;
                    case "debug": return Severity.DEBUG;
                    default: return Severity.UNKNOWN;

                }
            else return Severity.UNKNOWN;
        }

        private static ManagedEntityValue<?> getAttachments(final List<VariableBinding> bindings,
                                                     final Map<String, String> options,
                                                     final SnmpTypeSystem typeSystem){
            switch (bindings.size()){
                case 0: return null;
                case 1:
                    final Variable binding = bindings.get(0).getVariable();
                    return new ManagedEntityValue<>(binding,
                            typeSystem.resolveSnmpScalarType(binding, options));
                default:
                    final Map<String, Object> attachment = Maps.newHashMapWithExpectedSize(bindings.size());
                    final Map<String, ManagedEntityType> attachmentType = Maps.newHashMapWithExpectedSize(bindings.size());
                    for(final VariableBinding b: bindings){
                        final String key = b.getOid().toDottedString();
                        attachment.put(key, b.getVariable());
                        attachmentType.put(key, typeSystem.resolveSnmpScalarType(b.getVariable(), options));
                    }
                    return new ManagedEntityValue<>(new KeyedRecordSet<String, Object>() {
                        @Override
                        protected Set<String> getKeys() {
                            return attachment.keySet();
                        }

                        @Override
                        protected Object getRecord(final String key) {
                            return attachment.get(key);
                        }
                    },
                   typeSystem.createEntityDictionaryType(attachmentType));
            }
        }

        private void processPdu(final PDU event){
            List<VariableBinding> bindings = event.getBindingList(getNotificationID());
            if(bindings.size() == 0) return;

            String message;
            if(containsKey(MESSAGE_TEMPLATE_PARAM)){  //format message, no attachments
                message = get(MESSAGE_TEMPLATE_PARAM);
                for(final VariableBinding binding: bindings) {
                    final OID postfix = SnmpConnectorHelpers.getPostfix(getNotificationID(), binding.getOid());
                    if (postfix.size() == 0) continue;
                    message = message.replaceAll(String.format("\\{%s\\}", postfix), binding.getVariable().toString());
                }
                bindings = Collections.emptyList();
            }
            else if(containsKey(MESSAGE_OID_PARAM)){      //extract message, add attachments
                final OID messageOID = new OID(message = get(MESSAGE_OID_PARAM));
                bindings = new ArrayList<>(bindings);
                final Iterator<VariableBinding> iterator = bindings.iterator();
                while (iterator.hasNext()){
                    final VariableBinding b = iterator.next();
                    if(Objects.equals(messageOID, SnmpConnectorHelpers.getPostfix(getNotificationID(), b.getOid()))){
                        message = b.getVariable().toString();
                        iterator.remove();
                        break;
                    }
                }
            }
            else {              //concatenate bindings, no attachments
                message = Joiner.on(System.lineSeparator()).join(bindings);
                bindings = Collections.emptyList();
            }
            fire(message, event.getRequestID(), bindings);
        }

        private void fire(final String message,
                          final Integer32 requestID,
                          final List<VariableBinding> attachment){
            fire(new SnmpNotification(getSeverity(),
                    sequenceNumber.getAndIncrement(),
                    message,
                    requestID,
                    getAttachments(attachment, options, typeSystem)), listenerInvoker);
        }

        @Override
        public void processPdu(final CommandResponderEvent event) {
            processPdu(event.getPDU());
        }

        /**
         * Gets listeners invocation model for this notification type.
         *
         * @return Listeners invocation model for this notification type.
         */
        @Override
        public NotificationModel getNotificationModel() {
            return NotificationModel.MULTICAST_SEQUENTIAL;
        }

        /**
         * Detects the attachment type.
         * <p/>
         * This method will be called automatically by SNAMP infrastructure
         * and once for this instance of notification metadata.
         *
         * @return The attachment type.
         */
        @Override
        protected ManagedEntityType detectAttachmentType() {
            return null;
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

    private static final class SnmpNotificationSupport extends AbstractNotificationSupport{
        private final AbstractConcurrentResourceAccessor<SnmpClient> client;
        private final SnmpTypeSystem typeSystem;

        private SnmpNotificationSupport(final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                        final SnmpTypeSystem typeSystem){
            this.client = client;
            this.typeSystem = typeSystem;
        }

        /**
         * Reports an error when enabling notifications.
         *
         * @param listID   Subscription list identifier.
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToEnableNotifications(logger, Level.SEVERE, listID, category, e);
                }
            });
        }

        /**
         * Reports an error when disabling notifications.
         *
         * @param listID Subscription list identifier.
         * @param e      Internal connector error.
         * @see #failedToDisableNotifications(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToDisableNotifications(final String listID, final Exception e) {
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToDisableNotifications(logger, Level.WARNING, listID, e);
                }
            });
        }

        /**
         * Reports an error when subscribing the listener.
         *
         * @param listenerID Subscription list identifier.
         * @param e          Internal connector error.
         * @see #failedToSubscribe(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToSubscribe(final String listenerID, final Exception e) {
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToSubscribe(logger, Level.WARNING, listenerID, e);
                }
            });
        }

        /**
         * Enables event listening for the specified category of events.
         * <p>
         * In the default implementation this method does nothing.
         * </p>
         *
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        @Override
        protected SnmpNotificationMetadata enableNotifications(final String category, final Map<String, String> options) {
            try {
                return client.write(new Action<SnmpClient, SnmpNotificationMetadata, ParseException>() {
                    @Override
                    public SnmpNotificationMetadata invoke(final SnmpClient client) throws ParseException {
                        final SnmpNotificationMetadata listener = new SnmpNotificationMetadata(new OID(SNMP4JSettings.getOIDTextFormat().parse(category)), options, typeSystem);
                        client.addCommandResponder(listener);
                        return listener;
                    }
                });
            }
            catch (final Exception e) {
                SnmpConnectorHelpers.log(Level.WARNING, "Subscription to SNMP event %s failed. Context: %s",
                        category, LogicalOperation.current(), e);
                return null;
            }
        }

        private void disableNotifications(final SnmpNotificationMetadata notifMeta) {
            try {
                client.write(new ConsistentAction<SnmpClient, Void>() {
                    @Override
                    public Void invoke(final SnmpClient client) {
                        client.removeCommandResponder(notifMeta);
                        return null;
                    }
                });
            } catch (final Exception e) {
                SnmpConnectorHelpers.log(Level.WARNING, "Problem occurs when unsubscribing event %s. Context: %s",
                        notifMeta.getCategory(), LogicalOperation.current(), e);
            }
        }

        /**
         * Disable all notifications associated with the specified event.
         * <p>
         * In the default implementation this method does nothing.
         * </p>
         *
         * @param notificationType The event descriptor.
         */
        @Override
        protected void disableNotifications(final GenericNotificationMetadata notificationType) {
            if(notificationType instanceof SnmpNotificationMetadata)
                disableNotifications((SnmpNotificationMetadata)notificationType);
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param listener The event listener.
         * @return Any custom data associated with the subscription.
         */
        @Override
        protected Object subscribe(final NotificationListener listener) {
            return null;
        }

        /**
         * Cancels the notification listening.
         *
         * @param listener The notification listener to remove.
         * @param data     The custom data associated with subscription that returned from {@link #subscribe(com.itworks.snamp.connectors.notifications.NotificationListener)}
         */
        @Override
        protected void unsubscribe(final NotificationListener listener, final Object data) {

        }
    }

    private static final class SnmpAttributeMetadata extends GenericAttributeMetadata<SnmpManagedEntityType> {
        private final SnmpManagedEntityType attributeType;
        private final Map<String, String> options;

        public SnmpAttributeMetadata(final OID attributeID, final SnmpManagedEntityType entityType, final Map<String, String> options){
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
        protected SnmpManagedEntityType detectAttributeType() {
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

    private static final class SnmpAttributeSupport extends AbstractAttributeSupport{
        private final AbstractConcurrentResourceAccessor<SnmpClient> client;
        private final SnmpTypeSystem typeSystem;

        private SnmpAttributeSupport(final AbstractConcurrentResourceAccessor<SnmpClient> client,
                                     final SnmpTypeSystem typeSystem){
            this.client = client;
            this.typeSystem = typeSystem;
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
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
                }
            });
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
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToGetAttribute(logger, Level.WARNING, attributeID, e);
                }
            });
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
            SnmpConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
                }
            });
        }

        private Address[] getClientAddresses(){
            return client.read(new ConsistentAction<SnmpClient, Address[]>() {
                @Override
                public Address[] invoke(final SnmpClient client) {
                    return client.getClientAddresses();
                }
            });
        }

        private SnmpAttributeMetadata connectAttribute(final OID attributeID, final Map<String, String> options) throws Exception{
            final Variable value = client.read(new Action<SnmpClient, Variable, Exception>() {
                private final TimeSpan responseTimeout = SnmpConnectorConfigurationProvider.getResponseTimeout(options);

                @Override
                public Variable invoke(final SnmpClient client) throws IOException, TimeoutException, InterruptedException {
                    return client.get(attributeID, responseTimeout);
                }
            });
            if(value == null) throw new IOException(String.format("Attribute %s doesn't exist on SNMP agent", attributeID));
            final SnmpManagedEntityType attributeType = typeSystem.resolveSnmpScalarType(value, options);
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
                SnmpConnectorLicenseLimitations.current().verifyMaxAttributeCount(attributesCount());
                return connectAttribute(new OID(attributeName), options);
            }
            catch (final LicensingException e){
                SnmpConnectorHelpers.log(Level.INFO, "Maximum count of attributes is reached: %s. Unable to connect %s attribute. Context: %s",
                        attributesCount(), attributeName, LogicalOperation.current(), e);
            }
            catch (final Exception e) {
                SnmpConnectorHelpers.log(Level.SEVERE, "Unable to connect attribute %s. Context: %s",
                        attributeName, LogicalOperation.current(), e);
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
            return true;
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
         * @return The value of the attribute.
         * @throws java.util.concurrent.TimeoutException
         */
        @Override
        protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout) throws Exception {
            if (attribute instanceof SnmpAttributeMetadata)
                return getAttributeValue((SnmpAttributeMetadata) attribute, readTimeout);
            else throw new ConversionException(attribute, SnmpAttributeMetadata.class);
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout The attribute value write operation timeout.
         * @param value        The value to write.
         * @throws java.lang.Exception Internal connector error.
         */
        @Override
        protected void setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws Exception{
            if(attribute instanceof SnmpAttributeMetadata)
                setAttributeValue((SnmpAttributeMetadata)attribute, writeTimeout, value);

        }

        private void setAttributeValue(final SnmpAttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws Exception {
            client.read(new Action<SnmpClient, Void, Exception>() {
                @Override
                public Void invoke(final SnmpClient client) throws IOException, InvalidSnmpValueException, TimeoutException, InterruptedException {
                    client.set(Collections.singletonMap(attribute.getAttributeID(),
                            attribute.getType().convertToSnmp(value).get(new OID())), writeTimeout);
                    return null;
                }
            });
        }
    }

    private final SnmpAttributeSupport attributes;
    private final SnmpNotificationSupport notifications;
    private final AbstractConcurrentResourceAccessor<SnmpClient> client;

    /**
     * Initializes a new management connector.
     * @param snmpConnectionOptions Management connector initialization options.
     * @throws java.io.IOException Unable to instantiate SNMP client.
     */
    SnmpResourceConnector(final SnmpConnectionOptions snmpConnectionOptions) throws IOException {
        super(snmpConnectionOptions);
        client = new ConcurrentResourceAccessor<>(snmpConnectionOptions.createSnmpClient());
        final SnmpTypeSystem typeSystem = new SnmpTypeSystem();
        attributes = new SnmpAttributeSupport(client, typeSystem);
        notifications = new SnmpNotificationSupport(client, typeSystem);
    }

    SnmpResourceConnector(final String connectionString,
                                 final Map<String, String> parameters) throws IOException {
        this(new SnmpConnectionOptions(connectionString, parameters));
    }

    void listen() throws IOException{
        client.write(new Action<SnmpClient, Void, IOException>() {
            @Override
            public Void invoke(final SnmpClient client) throws IOException{
                client.listen();
                return null;
            }
        });
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
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException Unregistered attribute requested.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
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
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException
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

    /**
     * Enables event listening for the specified category of events.
     * <p>
     * categoryId can be used for enabling notifications for the same category
     * but with different options.
     * </p>
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) throws NotificationSupportException{
        verifyInitialization();
        return notifications.enableNotifications(listId, category, options);
    }

    /**
     * Disables event listening for the specified category of events.
     * <p>
     * This method removes all listeners associated with the specified subscription list.
     * </p>
     *
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public boolean disableNotifications(final String listId) throws NotificationSupportException{
        verifyInitialization();
        return notifications.disableNotifications(listId);
    }

    /**
     * Gets the notification metadata by its category.
     *
     * @param listId The identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     * for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    @Override
    public NotificationMetadata getNotificationInfo(final String listId) {
        return notifications.getNotificationInfo(listId);
    }

    /**
     * Returns a read-only collection of enabled notifications (subscription list identifiers).
     *
     * @return A read-only collection of enabled notifications (subscription list identifiers).
     */
    @Override
    public Collection<String> getEnabledNotifications() {
        return notifications.getEnabledNotifications();
    }

    /**
     * Attaches the notification listener.
     *
     * @param listenerId Unique identifier of the listener.
     * @param listener   The notification listener.
     * @param delayed    {@literal true} to force delayed subscription. This flag indicates
     *                   that you can attach a listener even if this object
     *                   has no enabled notifications.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public void subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) throws NotificationSupportException{
        verifyInitialization();
        notifications.subscribe(listenerId, listener, delayed);
    }

    /**
     * Gets a logger associated with this platform service.
     *
     * @return A logger associated with this platform service.
     */
    @Override
    public Logger getLogger() {
        return getLoggerImpl();
    }

    static Logger getLoggerImpl(){
        return getLogger(NAME);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously returned by {@link #subscribe(String, com.itworks.snamp.connectors.notifications.NotificationListener, boolean)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean unsubscribe(final String listenerId) {
        verifyInitialization();
        return notifications.unsubscribe(listenerId);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws IOException Unable to close SNMP client.
     */
    @Override
    public void close() throws IOException {
        try {
            client.write(new Action<SnmpClient, Void, IOException>() {
                @Override
                public Void invoke(final SnmpClient client) throws IOException {
                    client.close();
                    return null;
                }
            });
        } finally {
            attributes.clear();
            notifications.clear();
        }
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        if(Objects.equals(objectType, Address[].class))
            return objectType.cast(attributes.getClientAddresses());
        else return super.queryObject(objectType);
    }
}
