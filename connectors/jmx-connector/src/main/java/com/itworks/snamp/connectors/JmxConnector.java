package com.itworks.snamp.connectors;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.core.maintenance.*;
import com.itworks.snamp.internal.MethodStub;

import static com.itworks.snamp.internal.ReflectionUtils.weakReference;
import com.itworks.snamp.connectors.util.NotificationListenerInvokerFactory;
import com.itworks.snamp.licensing.JmxConnectorLimitations;
import org.apache.commons.beanutils.PropertyUtils;

import static com.itworks.snamp.connectors.JmxConnectionManager.MBeanServerConnectionHandler;
import static com.itworks.snamp.configuration.JmxConnectorConfigurationDescriptor.*;

import javax.management.*;
import javax.management.openmbean.*;
import javax.management.remote.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
@SuppressWarnings("unchecked")
final class JmxConnector extends AbstractManagementConnector implements NotificationSupport, Maintainable {
    /**
     * Represents JMX connector name.
     */
    public static final String NAME = "jmx";
    private static final Logger log = AbstractManagementConnectorFactory.getLogger(NAME);
    private static final JmxTypeSystem typeSystem = new JmxTypeSystem();

    private static enum JmxMaintenanceActions implements MaintenanceActionInfo {
        SIMULATE_CONNECTION_ABORT("simulateConnectionAbort");

        private final String name;

        private JmxMaintenanceActions(final String name){
            this.name = name;
        }

        /**
         * Gets system name of this action,
         *
         * @return The system name of this action.
         */
        @Override
        public final String getName() {
            return name;
        }

        /**
         * Gets description of this action.
         *
         * @param loc The locale of the description.
         * @return The description of this action.
         */
        @Override
        public final String getDescription(final Locale loc) {
            return "";
        }
    }

    private final static class JmxNotificationMetadata extends GenericNotificationMetadata{
        private final static String severityOption = "severity";
        private final Map<String, String> options;
        private final ExecutorService executor;

        /**
         * Represents owner of this notification metadata.
         */
        public final ObjectName eventOwner;
        private final MBeanNotificationInfo eventMetadata;

        public JmxNotificationMetadata(final String notifType,
                                       final MBeanNotificationInfo notificationInfo,
                                       final ObjectName eventOwner,
                                       final Map<String, String> options){
            super(notifType);
            this.options = Collections.unmodifiableMap(options);
            this.eventOwner = eventOwner;
            this.eventMetadata = notificationInfo;
            this.executor = Executors.newSingleThreadExecutor();
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
         * Raises the notification associated with this descriptor.
         * @param n A notification to emit.
         */
        public final void fire(final javax.management.Notification n){
            fire(new JmxNotificationWrapper(getSeverity(), n), NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, new NotificationListenerInvokerFactory.ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    log.log(Level.SEVERE, "Unable to process JMX notification.", e);
                }
            }));
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
    }

    private static final class JmxNotificationSupport extends AbstractNotificationSupport implements javax.management.NotificationListener, MBeanServerConnectionHandler<Void>{
        private final JmxConnectionManager connectionManager;

        public JmxNotificationSupport(final JmxConnectionManager connectionManager){
            this.connectionManager = connectionManager;
            this.connectionManager.addReconnectionHandler(this);
        }

        private Set<ObjectName> getNotificationTargets(){
            final Set<ObjectName> targets = new HashSet<>(10);
            for(final String category: getCategories())
                for(final JmxNotificationMetadata eventData: getEnabledNotifications(category, JmxNotificationMetadata.class).values())
                    targets.add(eventData.eventOwner);
            return targets;
        }

        private void enableListening(final ObjectName target){
            final javax.management.NotificationListener listener = this;
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.addNotificationListener(target, listener, null, null);
                    return null;
                }
            }, null);
        }

        private void disableListening(final ObjectName target){
            final javax.management.NotificationListener listener = this;

            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.removeNotificationListener(target, listener);
                    return null;
                }
            }, null);
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param notificationType The event type.
         * @param listener         The event listener.
         */
        @Override
        @MethodStub
        protected final Object subscribeCore(final NotificationMetadata notificationType, final NotificationListener listener) {
            return null;
        }

        /**
         * Cancels the notification listening.
         *
         * @param metadata The event type.
         * @param listener The notification listener to remove.
         * @param data     The custom data associated with subscription that returned from {@link #subscribeCore(com.itworks.snamp.connectors.NotificationMetadata, com.itworks.snamp.connectors.NotificationSupport.NotificationListener)}
         *                 method.
         */
        @Override
        @MethodStub
        protected final void unsubscribeCore(final NotificationMetadata metadata, final NotificationListener listener, final Object data) {

        }

        private void disableNotificationsCore(final JmxNotificationMetadata notificationType){
            final Set<ObjectName> targets = getNotificationTargets();
            if(!targets.contains(notificationType.eventOwner))
                disableListening(notificationType.eventOwner);
        }

        /**
         * Disable all notifications associated with the specified event.
         *
         * @param notificationType The event descriptor.
         */
        @Override
        protected final void disableNotificationsCore(final NotificationMetadata notificationType) {
            //remove JMX listener if there is no one active MBean listener
            if(notificationType instanceof JmxNotificationMetadata)
                disableNotificationsCore((JmxNotificationMetadata)notificationType);
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
            final JmxNotificationMetadata eventData = connectionManager.handleConnection(new MBeanServerConnectionHandler<JmxNotificationMetadata>() {
                @Override
                public JmxNotificationMetadata handle(final MBeanServerConnection connection) throws IOException, JMException {
                    if (options.containsKey(OBJECT_NAME_PROPERTY)) {
                        final ObjectName on = new ObjectName(options.get(OBJECT_NAME_PROPERTY));
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(on).getNotifications())
                            for (final String notifType : notificationInfo.getNotifTypes())
                                if (Objects.equals(notifType, category))
                                    return new JmxNotificationMetadata(notifType, notificationInfo, on, options);
                        return null;
                    } else return null;
                }
            }, null);
            if(eventData != null){
                //checks whether the enabled MBean object already listening
                final Set<ObjectName> listeningContext = getNotificationTargets();
                if(!listeningContext.contains(eventData.eventOwner))
                    enableListening(eventData.eventOwner);
            }
            return eventData;
        }

        private final void handleNotification(final ObjectName source, final javax.management.Notification notification){
            final Map<String, JmxNotificationMetadata> enabledNotifs = getEnabledNotifications(notification.getType(), JmxNotificationMetadata.class);
            for(final JmxNotificationMetadata eventMetadata: enabledNotifs.values())
                if(source.equals(eventMetadata.eventOwner)) eventMetadata.fire(notification);
        }

        @Override
        public final void handleNotification(final javax.management.Notification notification, final Object handback) {
            //iterates through all listeners and executes it
            if(notification.getSource() instanceof ObjectName)
                handleNotification((ObjectName)notification.getSource(), notification);
            else log.warning(String.format("Unable to handle notification %s because source is unknown", notification));
        }

        public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
            //for each MBean object assigns notification listener
            for(final ObjectName target: getNotificationTargets())
                connection.addNotificationListener(target, this, null, null);
            return null;
        }

        public final void unsubscribeAll(){
            for(final ObjectName target: getNotificationTargets())
                disableListening(target);
        }
    }

    /**
     * Represents count of instantiated connectors.
     */
    private static final AtomicLong instanceCounter = new AtomicLong(0);

    private final JmxConnectionManager connectionManager;
    private final JmxNotificationSupport notifications;
    private final Maintainable maintenance;

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
        private final Map<String, String> options;

        protected JmxAttributeProvider(final JmxConnectionManager manager,
                                       final String attributeName,
                                       final ObjectName namespace,
                                       final Map<String, String> options){
            super(attributeName, namespace.toString());
            this.connectionManager = manager;
            this.namespace = namespace;
            this.options = options != null ? Collections.unmodifiableMap(options) : Collections.<String, String>emptyMap();
        }

        protected JmxAttributeProvider(final JmxConnectionManager manager,
                                       final String attributeName,
                                       final ObjectName namespace){
            this(manager, attributeName, namespace, null);
        }

        @Override
        public String getDisplayName(final Locale locale) {
            return getName();
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
        public final boolean containsKey(final Object option) {
            return options.containsKey(option);
        }

        @Override
        public final boolean containsValue(final Object o) {
            return options.containsValue(o);
        }

        @Override
        public final String get(final Object option) {
            return options.get(option);
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
                for(final PropertyDescriptor property: Introspector.getBeanInfo(jmxNotification.getClass(), javax.management.Notification.class).getPropertyDescriptors())
                    put(property.getName(), PropertyUtils.getProperty(jmxNotification, property.getName()));
            }
            catch (final java.beans.IntrospectionException | ReflectiveOperationException e) {
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

    private JmxAttributeProvider createPlainAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options){
        //extracts JMX attribute metadata
        final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
            @Override
            public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                    if (attributeName.equals(attr.getName())) return attr;
                return null;
            }
        }, null);
        return targetAttr != null ? new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace, options){
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

    private JmxAttributeProvider createCompositeAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options){
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
        return targetAttr != null ? new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace, options){
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
        JmxConnectorLimitations.current().verifyMaxInstanceCount(instanceCounter.incrementAndGet());
        this.connectionManager = new JmxConnectionManager(log, serviceURL, connectionProperties);
        this.notifications = new JmxNotificationSupport(this.connectionManager);
        //create maintainer
        this.maintenance = weakReference(new AbstractMaintainable<JmxMaintenanceActions>(JmxMaintenanceActions.class) {
            @Override
            protected final Object[] parseArguments(final JmxMaintenanceActions action, final String arguments, final Locale loc) {
                return new Object[0];
            }

            @Action
            public final String simulateConnectionAbort(){
                connectionManager.simulateConnectionAbort();
                return "OK";
            }
        }, Maintainable.class);
    }

    private static boolean useRegexpOption(final Map<String, String> options){
        return options.containsKey("useRegexp") && Boolean.TRUE.toString().equals(options.get("useRegexp"));
    }

    private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options, final boolean useRegexp){
        //creates JMX attribute provider based on its metadata and connection options.
        if(namespace == null) return null;
        if(CompositeValueNavigator.isCompositeAttribute(attributeName))
            return createCompositeAttribute(namespace, attributeName, options);
        else if(useRegexpOption(options)) return connectAttribute(findObjectName(namespace), attributeName, options, false);
        else return createPlainAttribute(namespace, attributeName, options);
    }

    private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options){
        //creates JMX attribute provider based on its metadata and connection options.
        return connectAttribute(namespace, attributeName, options, useRegexpOption(options));
    }

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    protected JmxAttributeProvider connectAttributeCore(final String attributeName, final Map<String, String> options){
        final String namespace = Objects.toString(options.get(OBJECT_NAME_PROPERTY), "");
        try {
            return connectAttribute(new ObjectName(namespace), attributeName, options);
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
     * @param listenerId An identifier of the notification listener.
     * @param listId   The identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final boolean subscribe(final String listenerId, final String listId, final NotificationListener listener) {
        verifyInitialization();
        return notifications.subscribe(listenerId, listId, listener);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously passed to {@link #subscribe(String, String, com.itworks.snamp.connectors.NotificationSupport.NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final String listenerId) {
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
        notifications.unsubscribeAll();
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
     * Returns read-only map of maintenance actions.
     *
     * @return Read-only map of maintenance action,
     */
    @Override
    public final Set<String> getActions() {
        return maintenance.getActions();
    }

    /**
     * Returns human-readable description of the specified maintenance action that
     * includes description of the arguments string.
     *
     * @param actionName The name of the maintenance action.
     * @param loc        Target locale of the action description.
     * @return Localized description of the action.
     */
    @Override
    public final String getActionDescription(final String actionName, final Locale loc) {
        return maintenance.getActionDescription(actionName, loc);
    }

    /**
     * Invokes maintenance action.
     *
     * @param actionName The name of the action to invoke.
     * @param arguments  The action invocation command line. May be {@literal null} or empty for parameterless
     *                   action.
     * @param loc        Localization of the action arguments string and invocation result.
     * @return The localized result of the action invocation.
     */
    @Override
    public final Future<String> doAction(final String actionName, final String arguments, final Locale loc) {
        return maintenance.doAction(actionName, arguments, loc);
    }
}