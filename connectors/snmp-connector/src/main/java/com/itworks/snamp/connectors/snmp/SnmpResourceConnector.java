package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.ReferenceCountedObject;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import org.apache.commons.lang3.StringUtils;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.smi.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.AbstractConcurrentResourceAccess.Action;
import static com.itworks.snamp.AbstractConcurrentResourceAccess.ConsistentAction;
import static com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory.ExceptionHandler;
import static com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker;
import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.MESSAGE_TEMPLATE;
import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.SEVERITY_PARAM;

/**
 * Represents SNMP-compliant managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceConnector extends AbstractManagedResourceConnector<SnmpConnectionOptions> implements AttributeSupport, NotificationSupport {
    private static final SnmpTypeSystem typeSystem = new SnmpTypeSystem();

    private static final class SnmpNotificationMetadata extends GenericNotificationMetadata implements CommandResponder{
        private final Map<String, String> options;
        private final AtomicLong sequenceNumber;
        private final Logger logger;
        private final ExecutorService executor;

        public SnmpNotificationMetadata(final OID category,
                                        final Map<String, String> options,
                                        final Logger logger){
            super(category.toDottedString());
            this.options = Collections.unmodifiableMap(options);
            this.sequenceNumber = new AtomicLong(0L);
            this.logger = logger;
            this.executor = Executors.newSingleThreadExecutor();
        }

        public OID getNotificationID(){
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

        private static Map<String, Object> getAttachments(final Collection<VariableBinding> bindings){
            final Map<String, Object> attachments = new HashMap<>(bindings.size());
            for(final VariableBinding b: bindings)
                attachments.put(b.getOid().toDottedString(), b.getVariable());
            return attachments;
        }

        private void processPdu(final PDU event){
            final Collection<VariableBinding> bindings = event.getBindingList(getNotificationID());
            if(bindings.size() == 0) return;
            String template = get(MESSAGE_TEMPLATE);
            if(template == null) template = StringUtils.join(bindings, System.lineSeparator());
            else for(final VariableBinding binding: bindings){
                final OID postfix = SnmpConnectorHelpers.getPostfix(getNotificationID(), binding.getOid());
                if(postfix.size() == 0) continue;
                template = template.replaceAll(String.format("\\{%s\\}", postfix), binding.getVariable().toString());
            }
            fire(new NotificationImpl(getSeverity(), sequenceNumber.getAndIncrement(), new Date(), template, getAttachments(bindings)), createParallelExceptionResistantInvoker(executor, new ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    logger.log(Level.SEVERE, "Unable to process JMX notification.", e);
                }
            }));
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
            return NotificationModel.MULTICAST;
        }

        /**
         * Returns the type descriptor for the specified attachment.
         *
         * @param attachment The notification attachment.
         * @return The type descriptor for the specified attachment; or {@literal null} if the specified
         * attachment is not supported.
         */
        @Override
        public ManagementEntityType getAttachmentType(final Object attachment) {
            return attachment instanceof Variable ?
                    typeSystem.resolveSnmpScalarType((Variable)attachment, options) :
                    null;
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

    private static final class SnmpNotificationSupport extends AbstractNotificationSupport implements AutoCloseable{
        private final ReferenceCountedObject<SnmpClient> client;
        private final Logger logger;

        public SnmpNotificationSupport(final ReferenceCountedObject<SnmpClient> client,
                                       final Logger logger){
            this.client = client;
            this.logger = logger;
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
                return client.increfAndWrite(new Action<SnmpClient, SnmpNotificationMetadata, ParseException>() {
                    @Override
                    public SnmpNotificationMetadata invoke(final SnmpClient client) throws ParseException {
                        final SnmpNotificationMetadata listener = new SnmpNotificationMetadata(new OID(SNMP4JSettings.getOIDTextFormat().parse(category)), options, logger);
                        client.addCommandResponder(listener);
                        return listener;
                    }
                });
            }
            catch (final Exception e) {
                logger.log(Level.WARNING, String.format("Subscription to SNMP event %s failed.", category), e);
                return null;
            }
        }

        private void disableNotifications(final SnmpNotificationMetadata notifMeta){
            try {
                client.write(new ConsistentAction<SnmpClient, Void>() {
                    @Override
                    public Void invoke(final SnmpClient client) {
                        client.removeCommandResponder(notifMeta);
                        return null;
                    }
                });
                client.decref();
            }
            catch (final Exception e){
                logger.log(Level.WARNING, String.format("Problem occurs when unsubscribing event %s.", notifMeta.getCategory()), e);
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

        @Override
        public void close() throws Exception {
            try {
                client.decref();
            }
            finally {
                clear();
            }
        }
    }

    private static final class SnmpAttributeMetadata extends GenericAttributeMetadata<SnmpManagementEntityType> {
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

        public Address[] getClientAddresses(){
            return client.read(new ConsistentAction<SnmpClient, Address[]>() {
                @Override
                public Address[] invoke(final SnmpClient client) {
                    return client.getClientAddresses();
                }
            });
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
    private final SnmpNotificationSupport notifications;

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
        notifications = new SnmpNotificationSupport(client, getLogger());
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
     */
    @Override
    public NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) {
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
     */
    @Override
    public boolean disableNotifications(final String listId) {
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
     * @return {@literal true}, if listener is added successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) {
        verifyInitialization();
        return notifications.subscribe(listenerId, listener, delayed);
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
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        attributes.close();
        notifications.close();
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
        return super.queryObject(objectType);
    }
}
