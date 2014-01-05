package com.snamp.connectors;

import com.snamp.*;
import com.snamp.licensing.JmxConnectorLimitations;
import static com.snamp.connectors.JmxConnectionManager.MBeanServerConnectionHandler;

import javax.management.*;
import javax.management.openmbean.*;
import javax.management.remote.*;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
@SuppressWarnings("unchecked")
final class JmxConnector extends AbstractManagementConnector implements NotificationSupport, JmxMaintenanceSupport {
    /**
     * Represents JMX connector name.
     */
    public static final String connectorName = "jmx";
    private static final Logger log = AbstractManagementConnectorFactory.getLogger(connectorName);
    private static final JmxTypeSystem typeSystem = new JmxTypeSystem();
    private static final String objectNameOption = "objectName";


    private static final javax.management.NotificationListener createJmxListener(final NotificationListener listener, final String category, final Notification.Severity severity){
            return new javax.management.NotificationListener() {
                @Override
                public void handleNotification(final javax.management.Notification notification, final Object handback) {
                    if(Objects.equals(category, notification.getType()))
                        listener.handle(new JmxNotificationWrapper(severity, notification));
                }
            };
    }

    private final static class JmxNotificationMetadata extends GenericNotificationMetadata implements MBeanServerConnectionHandler<Void>{
        private final static String severityOption = "severity";
        private final Map<String, String> options;
        private final JmxConnectionManager connectionManager;

        /**
         * Represents owner of this notification metadata.
         */
        public final ObjectName eventOwner;
        private final MBeanNotificationInfo eventMetadata;

        public JmxNotificationMetadata(final JmxConnectionManager manager,
                                       final String notifType,
                                       final MBeanNotificationInfo notificationInfo,
                                       final ObjectName eventOwner,
                                       final Map<String, String> options){
            super(notifType);
            this.options = Collections.unmodifiableMap(options);
            this.eventOwner = eventOwner;
            this.eventMetadata = notificationInfo;
            this.connectionManager = manager;
            manager.addReconnectionHandler(this);
        }

        public final Notification.Severity getSeverity(){
            if(options.containsKey(severityOption))
                switch (options.get(severityOption)){
                    case "panic": return Notification.Severity.PANIC;
                    case "alert": return Notification.Severity.ALERT;
                    case "critical": return Notification.Severity.CRITICAL;
                    case "error": return Notification.Severity.ERROR;
                    case "warning": return Notification.Severity.WARNING;
                    case "notice": return Notification.Severity.NOTICE;
                    case "info": return Notification.Severity.INFO;
                    case "debug": return Notification.Severity.DEBUG;
                    default: return Notification.Severity.UNKNOWN;

                }
            else return Notification.Severity.UNKNOWN;
        }

        /**
         * Gets listeners invocation model for this notification type.
         *
         * @return Listeners invocation model for this notification type.
         */
        @Override
        public final NotificationModel getNotificationModel() {
            return NotificationModel.MULTICAST;
        }

        /**
         * Returns the attachment type descriptor.
         * @param attachment The notification attachment.
         * @return The attachment type descriptor for the specified attachment; or {@literal null} if
         * attachment is not supported.
         */
        @Override
        public final ManagementEntityType getAttachmentType(final Object attachment) {
            return attachment != null ? typeSystem.createEntityType(attachment.getClass()) : null;
        }

        @Override
        public final int size() {
            return options.size();
        }

        @Override
        public final boolean isEmpty() {
            return options.isEmpty();
        }

        @Override
        public final boolean containsKey(final Object key) {
            return options.containsKey(key);
        }

        @Override
        public final boolean containsValue(final Object value) {
            return options.containsValue(value);
        }

        @Override
        public final String get(final Object key) {
            return options.get(key);
        }

        @Override
        public final Set<String> keySet() {
            return options.keySet();
        }

        @Override
        public final Collection<String> values() {
            return options.values();
        }

        @Override
        public final Set<Entry<String, String>> entrySet() {
            return options.entrySet();
        }

        public final javax.management.NotificationListener subscribe(final NotificationListener listener){
            final javax.management.NotificationListener wrapper = createJmxListener(listener, getCategory(), getSeverity());
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<javax.management.NotificationListener>() {
                @Override
                public javax.management.NotificationListener handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.addNotificationListener(eventOwner, wrapper, null, null);
                    return wrapper;
                }
            }, wrapper);
        }

        /**
         * Re-registers notification listeners.
         * @param connection
         * @return
         * @throws IOException
         * @throws JMException
         */
        @Override
        public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
            for(final Pair<NotificationListener, Object> listener: getListeners())
                if(listener.second instanceof javax.management.NotificationListener)
                    connection.addNotificationListener(eventOwner, (javax.management.NotificationListener)listener.second, null, null);
            return null;
        }

        private final void removeJmxListener(final javax.management.NotificationListener listener){
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.removeNotificationListener(eventOwner, listener);
                    return null;
                }
            }, null);
        }

        public final void disableNotifications(){
            //remove all listeners
            for(final Pair<NotificationListener, Object> listener: removeListeners())
                if(listener.second instanceof javax.management.NotificationListener)
                    removeJmxListener((javax.management.NotificationListener)listener.second);
        }
    }

    private static final class JmxNotificationSupport extends AbstractNotificationSupport{
        private final JmxConnectionManager connectionManager;

        public JmxNotificationSupport(final JmxConnectionManager connectionManager){
            this.connectionManager = connectionManager;
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param notificationType The event type.
         * @param listener         The event listener.
         */
        @Override
        protected final Object subscribeCore(final NotificationMetadata notificationType, final NotificationListener listener) {
            return notificationType instanceof JmxNotificationMetadata ?
                    ((JmxNotificationMetadata)notificationType).subscribe(listener):
                    null;
        }

        private final void unsubscribeCore(final JmxNotificationMetadata md, final javax.management.NotificationListener listener){
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Object>() {
                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.removeNotificationListener(md.eventOwner, listener);
                    return null;
                }
            }, null);
        }

        /**
         * Cancels the notification listening.
         *
         * @param metadata The event type.
         * @param listener The notification listener to remove.
         * @param data     The custom data associated with subscription that returned from {@link #subscribeCore(com.snamp.connectors.NotificationMetadata, com.snamp.connectors.NotificationSupport.NotificationListener)}
         *                 method.
         */
        @Override
        protected final void unsubscribeCore(final NotificationMetadata metadata, final NotificationListener listener, final Object data) {
            if(metadata instanceof JmxNotificationMetadata && data instanceof javax.management.NotificationListener)
                unsubscribeCore((JmxNotificationMetadata)metadata, (javax.management.NotificationListener)data);
            else log.warning(String.format("JMX-incompliant listener detected %s", data));
        }

        /**
         * Disable all notifications associated with the specified event.
         *
         * @param notificationType The event descriptor.
         */
        @Override
        protected final void disableNotificationsCore(final NotificationMetadata notificationType) {
            if(notificationType instanceof JmxNotificationMetadata)
                ((JmxNotificationMetadata)notificationType).disableNotifications();
        }

        /**
         * Enables event listening for the specified category of events.
         *
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        @Override
        protected final GenericNotificationMetadata enableNotificationsCore(final String category, final Map<String, String> options) {
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<JmxNotificationMetadata>() {
                @Override
                public JmxNotificationMetadata handle(final MBeanServerConnection connection) throws IOException, JMException {
                    if (options.containsKey(objectNameOption)) {
                        final ObjectName on = new ObjectName(options.get(objectNameOption));
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(on).getNotifications())
                            for (final String notifType : notificationInfo.getNotifTypes())
                                if (Objects.equals(notifType, category))
                                    return new JmxNotificationMetadata(connectionManager, notifType, notificationInfo, on, options);
                        return null;
                    } else return null;
                }
            }, null);
        }
    }

    /**
     * Represents count of instantiated connectors.
     */
    private static final AtomicLong instanceCounter = new AtomicLong(0);

    private final JmxConnectionManager connectionManager;
    private final JmxNotificationSupport notifications;

    /**
     * Represents field navigator in the composite JMX data.
     */
    private static final class CompositeValueNavigator
    {
        /**
         * Represents path delimiter.
         */
        public static final char delimiter = '@';
        /**
         * Represents the name of the attribute that has a composite type.
         */
        public final String attributeName;
        private final String[] path;

        /**
         * Initializes a new field navigator.
         * @param attributeName The name
         */
        public CompositeValueNavigator(final String attributeName)
        {
            if(!isCompositeAttribute(attributeName)) throw new IllegalArgumentException("Неверный формат имени составного атрибута");
            final String[] parts = attributeName.split(new String(new char[]{delimiter}));
            this.attributeName = parts[0];
            this.path = Arrays.copyOfRange(parts, 1, parts.length);
        }

        /**
         * Returns the path depth.
         * @return
         */
        public int depth(){
            return path.length;
        }

        /**
         * Returns the subfield name by depth index.
         * @param index
         * @return
         */
        public String item(int index)
        {
            return path[index];
        }

        private Object getValue(final Object root, final int index)
        {
            if(root instanceof CompositeData && index < path.length){
                final CompositeData cdata = (CompositeData)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getValue(cdata.get(subattr), index + 1) : root;
            }
            else return root;
        }

        /**
         * Получить значение вложенного атрибута.
         * @param root
         * @return
         */
        public Object getValue(final Object root)
        {
            return getValue(root, 0);
        }

        private Object getType(final Object root, final int index)
        {
            if(root instanceof CompositeType && index < path.length){
                final CompositeType cdata = (CompositeType)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getType(cdata.getType(subattr), index + 1) : root;
            }
            else return root;
        }

        /**
         * Returns a type of the inner field.
         * @param root
         * @return String or OpenType.
         */
        public Object getType(final OpenType<?> root)
        {
            return getType(root, 0);
        }

        /**
         * Получить полный путь композитного атрибута.
         */

        public String toString()
        {
            return this.attributeName + Arrays.toString(path).replace(", ", new String(new char[]{delimiter}));
        }

        /**
         * Determines whether the attribute name contains subfield path.
         * @param attributeName
         * @return
         */
        public static boolean isCompositeAttribute(final String attributeName)
        {
            return attributeName.indexOf(delimiter) >= 0;
        }
    }

    /**
     * Represents JMX attribute metadata.
     */
    public static interface JmxAttributeMetadata extends AttributeMetadata {
        static final String OWNER_OPTION = "owner";
        /**
         * Returns the object name in which the current attribute is located.
         * @return
         */
        public ObjectName getOwner();

        /**
         * Returns the type of the attribute value.
         * @return The type of the attribute value.
         */
        @Override
        public JmxManagementEntityType getType();
    }

    /**
     * Represents an abstract class for building JMX attribute providers.
     */
    private abstract static class JmxAttributeProvider extends GenericAttributeMetadata<JmxManagementEntityType> implements JmxAttributeMetadata {
        private final ObjectName namespace;
        private MBeanServerConnectionHandler<Object> attributeValueReader;
        private final JmxConnectionManager connectionManager;

        protected JmxAttributeProvider(final JmxConnectionManager manager,
                                       final String attributeName,
                                       final ObjectName namespace){
            super(attributeName, namespace.toString());
            this.connectionManager = manager;
            this.namespace = namespace;
        }

        @Override
        public String getDisplayName(final Locale locale) {
            return getName();
        }

        @Override
        public final int size() {
            return 1;
        }

        @Override
        public final boolean isEmpty() {
            return false;
        }

        @Override
        public final boolean containsKey(final Object option) {
            return OWNER_OPTION.equals(option);
        }

        @Override
        public final boolean containsValue(final Object o) {
            return namespace.equals(o);
        }

        @Override
        public final String get(final Object o) {
            return Objects.equals(o, OWNER_OPTION) ? namespace.toString() : null;
        }

        @Override
        public final Set<String> keySet() {
            return new HashSet<String>(1){{
                add(OWNER_OPTION);
            }};
        }

        @Override
        public final Collection<String> values() {
            return new ArrayList(1){{
                add(namespace.toString());
            }};
        }

        @Override
        public final Set<Entry<String, String>> entrySet() {
            return new HashSet<Entry<String, String>>(1){{
                add(new Entry<String, String>(){

                    @Override
                    public final String getKey() {
                        return OWNER_OPTION;
                    }

                    @Override
                    public final String getValue() {
                        return namespace.toString();
                    }

                    @Override
                    public final String setValue(String s) {
                        throw new UnsupportedOperationException();
                    }
                });
            }};
        }

        /**
         * Creates a new instance of the attribute value reader.
         * @return
         */
        protected abstract MBeanServerConnectionHandler<Object> createAttributeValueReader();

        /**
         * Creates a new instance of the attribute value writer.
         * @return
         */
        protected abstract MBeanServerConnectionHandler<Boolean> createAttributeValueWriter(final Object value);

        /**
         * Returns the attribute owner.
         * @return
         */
        public final ObjectName getOwner(){
            return namespace;
        }

        /**
         * Returns the value of the attribute.
         * @param defval
         * @return
         */
        public final Object getValue(final Object defval){
            if(canRead()){
                if(attributeValueReader == null) attributeValueReader = createAttributeValueReader();
                return connectionManager.handleConnection(attributeValueReader, defval);
            }
            else return defval;
        }

        /**
         * Writes the value to the attribute.
         * @param value
         * @return
         */
        public final boolean setValue(Object value){
            final JmxManagementEntityType typeInfo = getType();
            if(canWrite() && value != null)
                try{
                    value = typeInfo.convertToJmxType(value);
                    return connectionManager.handleConnection(createAttributeValueWriter(value), false);
                }
                catch (final IllegalArgumentException e){
                    log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    return false;
                }
            else return false;
        }
    }

    private final static class JmxNotificationWrapper extends HashMap<String, Object> implements Notification{
        private final javax.management.Notification jmxNotification;
        private final Severity notificationSeverity;

        public JmxNotificationWrapper(final Severity severity, final javax.management.Notification jmxNotification){
            this.jmxNotification = jmxNotification;
            notificationSeverity = severity != null ? severity : Severity.UNKNOWN;
            //loads notification as Java Bean and explore each JB property as attachment
            try {
                putAll(new BeanPropertyAccessor<>(jmxNotification, javax.management.Notification.class));
            }
            catch (final IntrospectionException e) {
                log.log(Level.WARNING, "Unable to wrap MBean notification into map.", e);
            }
        }

        /**
         * Gets the date and time at which the notification is generated.
         *
         * @return The date and time at which the notification is generated.
         */
        @Override
        public final Date getTimeStamp() {
            return new Date(jmxNotification.getTimeStamp());
        }

        /**
         * Gets the order number of the notification message.
         *
         * @return The order number of the notification message.
         */
        @Override
        public final long getSequenceNumber() {
            return jmxNotification.getSequenceNumber();
        }

        /**
         * Gets a severity of this event.
         *
         * @return The severity of this event.
         */
        @Override
        public final Severity getSeverity() {
            return notificationSeverity;
        }

        /**
         * Gets a message description of this notification.
         *
         * @return The message description of this notification.
         */
        @Override
        public final String getMessage() {
            return jmxNotification.getMessage();
        }
    }

    private JmxAttributeProvider createPlainAttribute(final ObjectName namespace, final String attributeName){
        //extracts JMX attribute metadata
        final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
            @Override
            public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                    if (attributeName.equals(attr.getName())) return attr;
                return null;
            }
        }, null);
        return targetAttr != null ? new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace){
            @Override
            protected final JmxManagementEntityType detectAttributeType() {
                if(targetAttr instanceof OpenMBeanAttributeInfoSupport)
                    return typeSystem.createEntityType(((OpenMBeanAttributeInfoSupport) targetAttr).getOpenType());
                else return typeSystem.createEntityType(targetAttr.getType());
            }

            /**
             * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
             *
             * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
             */
            @Override
            public boolean canWrite() {
                return targetAttr.isWritable();
            }

            /**
             * By default, returns {@literal true}.
             *
             * @return
             */
            @Override
            public boolean canRead() {
                return targetAttr.isReadable();
            }

            /**
             * Creates a new instance of the attribute value reader.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionHandler<Object> createAttributeValueReader() {
                return new MBeanServerConnectionHandler<Object>(){
                    @Override
                    public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                        return connection.getAttribute(namespace, getName());
                    }
                };
            }
            /**
             * Creates a new instance of the attribute value writer.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionHandler<Boolean> createAttributeValueWriter(final Object value) {
                return new MBeanServerConnectionHandler<Boolean>(){
                    @Override
                    public Boolean handle(final MBeanServerConnection connection) throws IOException, JMException {
                        connection.setAttribute(namespace, new Attribute(getName(), value));
                        return true;
                    }
                };
            }

            @Override
            public final String getDescription(final Locale locale) {
                return targetAttr.getDescription();
            }
        } : null;
    }

    private JmxAttributeProvider createCompositeAttribute(final ObjectName namespace, final String attributeName){
        final CompositeValueNavigator navigator = new CompositeValueNavigator(attributeName);
        //получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
        final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
            @Override
            public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                for(final MBeanAttributeInfo attr: connection.getMBeanInfo(namespace).getAttributes())
                    if(navigator.attributeName.equals(attr.getName())) return attr;
                return null;
            }
        }, null);
        return targetAttr != null ? new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace){
            private final OpenType<?> compositeType = targetAttr instanceof OpenMBeanAttributeInfoSupport ? ((OpenMBeanAttributeInfoSupport)targetAttr).getOpenType() : SimpleType.STRING;

            @Override
            public final boolean canRead() {
                return targetAttr.isReadable();
            }

            @Override
            public final boolean canWrite(){
                return false;
            }

            @Override
            public final String getDescription(final Locale locale) {
                return targetAttr.getDescription();
            }

            /**
             * Creates a new instance of the attribute value reader.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionHandler<Object> createAttributeValueReader() {
                return new MBeanServerConnectionHandler<Object>(){
                    @Override
                    public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                        return navigator.getValue(connection.getAttribute(namespace, navigator.attributeName));
                    }
                };
            }

            /**
             * The writer for the composite data structure is not supported.
             *
             * @return
             */
            @Override
            protected final MBeanServerConnectionHandler<Boolean> createAttributeValueWriter(Object value) {
                return new MBeanServerConnectionHandler<Boolean>() {
                    @Override
                    public Boolean handle(MBeanServerConnection connection) {
                        return false;
                    }
                };
            }

            @Override
            protected final JmxManagementEntityType detectAttributeType() {
                final Object attributeType = navigator.getType(compositeType);
                if(attributeType instanceof OpenType<?>)
                    return typeSystem.createEntityType((OpenType<?>)attributeType);
                else if(attributeType instanceof Class<?>)
                    return typeSystem.createEntityType((Class<?>)attributeType);
                else return typeSystem.createEntityType(Objects.toString(attributeType, ""));
            }
        } : null;
    }

    private ObjectName findObjectName(final ObjectName namespace){
        return connectionManager.handleConnection(new MBeanServerConnectionHandler<ObjectName>() {
            @Override
            public ObjectName handle(final MBeanServerConnection connection) throws IOException, JMException {

                final Set<ObjectInstance> beans = connection.queryMBeans(namespace, null);

                for (final ObjectInstance instance : beans) return instance.getObjectName();
                return null;
            }
        }, null);
    }

    /**
     * Initializes a new connector and established connection to the JMX provider.
     * @param serviceURL JMX connection string.
     * @param connectionProperties JMX connection properties(such as credentials).
     * @exception IllegalArgumentException Could not establish connection to JMX provider.
     */
    public JmxConnector(final JMXServiceURL serviceURL, final Map<String, Object> connectionProperties){
        if(serviceURL == null) throw new IllegalArgumentException("serviceURL is null.");
        this.connectionManager = new JmxConnectionManager(log, serviceURL, connectionProperties);
        this.notifications = new JmxNotificationSupport(this.connectionManager);
        JmxConnectorLimitations.current().verifyMaxInstanceCount(instanceCounter.incrementAndGet());
    }

    private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final boolean useRegexp){
        //creates JMX attribute provider based on its metadata and connection options.
        if(namespace == null) return null;
        if(CompositeValueNavigator.isCompositeAttribute(attributeName))
            return createCompositeAttribute(namespace, attributeName);
        else if(useRegexp) return connectAttribute(findObjectName(namespace), attributeName, false);
        else return createPlainAttribute(namespace, attributeName);
    }

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected JmxAttributeProvider connectAttributeCore(final String attributeName, final Map<String, String> options){
        final String namespace = Objects.toString(options.get(objectNameOption), "");
        try {
            return connectAttribute(new ObjectName(namespace), attributeName, options.containsKey("useRegexp") && Boolean.TRUE.equals(options.get("useRegexp")));
        } catch (final MalformedObjectNameException e) {
            log.log(Level.SEVERE, String.format("Unsupported JMX object name: %s", namespace), e);
            return null;
        }
        finally {
            JmxConnectorLimitations.current().verifyMaxAttributeCount(attributesCount());
        }
    }

    /**
     * Returns the value of the attribute.
     *
     * @param attribute    The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws java.util.concurrent.TimeoutException
     *
     */
    @Override
    protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException {
        return attribute instanceof JmxAttributeProvider ? ((JmxAttributeProvider)attribute).getValue(defaultValue) : defaultValue;
    }

    /**
     * Sends the attribute value to the remote agent.
     *
     * @param attribute    The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return
     */
    @Override
    protected final boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) {
        return attribute instanceof JmxAttributeProvider && ((JmxAttributeProvider)attribute).setValue(value);
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
    public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) {
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
    public final boolean disableNotifications(final String listId) {
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
    public final NotificationMetadata getNotificationInfo(final String listId) {
        verifyInitialization();
        return notifications.getNotificationInfo(listId);
    }

    /**
     * Attaches the notification listener.
     *
     * @param listId   The identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final Object subscribe(final String listId, final NotificationListener listener) {
        verifyInitialization();
        return notifications.subscribe(listId, listener);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously returned by {@link #subscribe(String, com.snamp.connectors.NotificationSupport.NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final Object listenerId) {
        verifyInitialization();
        return notifications.unsubscribe(listenerId);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws java.lang.Exception Some I/O error occurs.
     */
    @Override
    public final void close() throws Exception{
        super.close();
        notifications.clear();
        try{
            connectionManager.close();
        }
        finally {
            instanceCounter.decrementAndGet();
        }
    }

    /**
     * Releases all resources associated with this connector.
     */
    @Override
    protected final void finalize() {
        instanceCounter.decrementAndGet();
    }

    /**
     * Simulates connection abort.
     * <p>
     *     Only for testing purposes only.
     * </p>
     */
    @Internal
    @Override
    public final void simulateConnectionAbort(){
        connectionManager.simulateConnectionAbort();
    }
}
