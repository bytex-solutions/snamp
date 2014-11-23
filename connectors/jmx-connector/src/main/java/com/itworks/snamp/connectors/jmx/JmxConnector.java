package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.ConversionException;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.NotificationListener;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicensingException;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.remote.JMXConnector;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector<JmxConnectionOptions> implements AttributeSupport, NotificationSupport {
    /**
     * Represents JMX connector name.
     */
    public static final String NAME = JmxConnectorHelpers.CONNECTOR_NAME;
    private static final Logger logger = JmxConnectorHelpers.getLogger();
    private static final JmxTypeSystem typeSystem = new JmxTypeSystem();
    private static final String JMX_ENTITY_OPTION = "jmx-compliant";

    private final static class JmxNotificationMetadata extends GenericNotificationMetadata{
        private final Map<String, String> options;
        private final ExecutorService executor;

        /**
         * Represents owner of this notification metadata.
         */
        public final ObjectName eventOwner;
        private final Descriptor advancedMetadata;

        public JmxNotificationMetadata(final String notifType,
                                       final Descriptor notificationDescriptor,
                                       final ObjectName eventOwner,
                                       final Map<String, String> options){
            super(notifType);
            options.put(JMX_ENTITY_OPTION, Boolean.toString(true));
            this.options = Collections.unmodifiableMap(options);
            this.eventOwner = eventOwner;
            this.executor = Executors.newSingleThreadExecutor();
            this.advancedMetadata = notificationDescriptor;
        }

        private static Severity parseSeverity(final String value){
            switch (value){
                case "1": //jmx severity level
                case "panic": return Severity.PANIC;
                case "2": //jmx severity level
                case "alert": return Severity.ALERT;
                case "3": //jmx severity level
                case "critical": return Severity.CRITICAL;
                case "4": //jmx severity level
                case "error": return Severity.ERROR;
                case "5": //jmx severity level
                case "warning": return Severity.WARNING;
                case "6": //jmx severity level
                case "notice": return Severity.NOTICE;
                case "7": //jmx severity level
                case "info": return Severity.INFO;
                case "8": //jmx severity level
                case "debug": return Severity.DEBUG;
                case "0": //jmx severity level
                default: return Severity.UNKNOWN;
            }
        }

        public final Severity getSeverity(){
            final String JMX_SEVERITY = "severity";
            if(options.containsKey(SEVERITY_PARAM))
                return parseSeverity(options.get(SEVERITY_PARAM));
            else if(advancedMetadata != null && ArrayUtils.contains(advancedMetadata.getFieldNames(), JMX_SEVERITY))
                return parseSeverity(Objects.toString(advancedMetadata.getFieldValue(JMX_SEVERITY)));
            else return Severity.UNKNOWN;
        }

        /**
         * Raises the notification associated with this descriptor.
         * @param n A notification to emit.
         */
        public final void fire(final javax.management.Notification n){
            fire(new JmxNotificationWrapper(getSeverity(), n), NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, new NotificationListenerInvokerFactory.ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    logger.log(Level.SEVERE, "Unable to process JMX notification.", e);
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
         * Detects the attachment type.
         * <p/>
         * This method will be called automatically by SNAMP infrastructure
         * and once for this instance of notification metadata.
         *
         * @return The attachment type.
         */
        @Override
        protected ManagedEntityType detectAttachmentType() {
            final String JMX_USER_DATA_OPEN_TYPE = JmxTypeSystem.OPEN_TYPE_DESCR_FIELD;
            MBeanServer m; m.getCla
            if (advancedMetadata != null && ArrayUtils.contains(advancedMetadata.getFieldNames(), JMX_USER_DATA_OPEN_TYPE)) {
                final OpenType<?> attachmentType = (OpenType<?>) advancedMetadata.getFieldValue(JMX_USER_DATA_OPEN_TYPE);
                return typeSystem.createEntityType(attachmentType);
            } else return null;
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

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<String> keySet() {
            return options.keySet();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Collection<String> values() {
            return options.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<Entry<String, String>> entrySet() {
            return options.entrySet();
        }
    }

    private static final class JmxAttributeSupport extends AbstractAttributeSupport{
        private final JmxConnectionManager connectionManager;

        public JmxAttributeSupport(final JmxConnectionManager connectionManager){
            this.connectionManager = connectionManager;
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

        private static Supplier<OpenType<?>> createTypeDetectionFallback(final JmxAttributeProvider provider){
            return new Supplier<OpenType<?>>() {
                @Override
                public OpenType<?> get() {
                    try {
                        return provider.getTypeFromAttributeValue();
                    } catch (final Exception e) {
                        logger.log(Level.SEVERE, String.format("Failed to detect type of attribute %s", provider.getName()), e);
                        return SimpleType.STRING;
                    }
                }
            };
        }

        private JmxAttributeProvider createPlainAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options) throws Exception{
            //extracts JMX attribute metadata
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
                @Override
                public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                        if (attributeName.equals(attr.getName())) return attr;
                    return null;
                }
            });
            if(targetAttr == null) throw new AttributeNotFoundException(attributeName);
            else return new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace, options){
                @Override
                protected final JmxManagedEntityType detectAttributeType() {
                    return typeSystem.createAttributeType(targetAttr, createTypeDetectionFallback(this));
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

                @Override
                public boolean canRead() {
                    return targetAttr.isReadable();
                }

                @Override
                protected final MBeanServerConnectionHandler<Object> createAttributeValueReader() {
                    return new MBeanServerConnectionHandler<Object>(){
                        @Override
                        public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                            return connection.getAttribute(namespace, getName());
                        }
                    };
                }

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
            };
        }

        private JmxAttributeProvider createCompositeAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options) throws Exception{
            final CompositeValueNavigator navigator = new CompositeValueNavigator(attributeName);
            //получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
                @Override
                public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for(final MBeanAttributeInfo attr: connection.getMBeanInfo(namespace).getAttributes())
                        if(navigator.attributeName.equals(attr.getName())) return attr;
                    return null;
                }
            });
            if(targetAttr == null) throw new AttributeNotFoundException(attributeName);
            else return new JmxAttributeProvider(connectionManager, targetAttr.getName(), namespace, options){

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

                @Override
                protected final MBeanServerConnectionHandler<Object> createAttributeValueReader() {
                    return new MBeanServerConnectionHandler<Object>(){
                        @Override
                        public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                            return navigator.getValue(connection.getAttribute(namespace, navigator.attributeName));
                        }
                    };
                }

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
                protected final JmxManagedEntityType detectAttributeType() {
                    final OpenType<?> compositeType = JmxTypeSystem.getOpenType(targetAttr, createTypeDetectionFallback(this));
                    return typeSystem.createEntityType(navigator.getType(compositeType));
                }
            };
        }

        private ObjectName findObjectName(final ObjectName namespace) throws Exception {
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<ObjectName>() {
                @Override
                public ObjectName handle(final MBeanServerConnection connection) throws IOException, JMException {

                    final Set<ObjectInstance> beans = connection.queryMBeans(namespace, null);
                    return beans.size() > 0 ? beans.iterator().next().getObjectName() : null;
                }
            });
        }

        private static boolean useRegexpOption(final Map<String, String> options){
            return options.containsKey(USE_REGEXP_PARAM) && Boolean.TRUE.toString().equals(options.get(USE_REGEXP_PARAM));
        }

        private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options, final boolean useRegexp) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            if(namespace == null) return null;
            if(CompositeValueNavigator.isCompositeAttribute(attributeName))
                return createCompositeAttribute(namespace, attributeName, options);
            else if(useRegexp) return connectAttribute(findObjectName(namespace), attributeName, options, false);
            else return createPlainAttribute(namespace, attributeName, options);
        }

        private JmxAttributeProvider connectAttribute(final ObjectName namespace, final String attributeName, final Map<String, String> options) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            return connectAttribute(namespace, attributeName, options, useRegexpOption(options));
        }

        /**
         * Connects to the specified attribute.
         * @param attributeName The name of the attribute.
         * @param options The attribute discovery options.
         * @return The description of the attribute.
         * @throws java.lang.Exception Internal connector error.
         */
        @Override
        protected JmxAttributeProvider connectAttribute(final String attributeName, final Map<String, String> options) throws Exception{

            final String namespace = Objects.toString(options.get(OBJECT_NAME_PROPERTY), "");
            try {
                JmxConnectorLimitations.current().verifyMaxAttributeCount(attributesCount());
                return connectAttribute(new ObjectName(namespace), attributeName, options);
            }
            catch (final MalformedObjectNameException e) {
                logger.log(Level.SEVERE, String.format("Unsupported JMX object name: %s", namespace), e);
            }
            catch (final LicensingException e){
                logger.log(Level.INFO, String.format("Maximum count of attributes is reached: %s. Unable to connect %s attribute", attributesCount(), attributeName), e);
            }
            return null;
        }

        /**
         * Returns the value of the attribute.
         *
         * @param attribute    The metadata of the attribute to get.
         * @param readTimeout Read operation timeout.
         * @return The value of the attribute.
         *
         */
        @Override
        protected Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout) throws Exception {
            if (attribute instanceof JmxAttributeProvider)
                return ((JmxAttributeProvider) attribute).getValue();
            else throw new ConversionException(attribute, JmxAttributeProvider.class);
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout Write operation timeout.
         * @param value The value to write.
         */
        @Override
        protected final void setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws Exception{
            if(attribute instanceof JmxAttributeProvider)
                ((JmxAttributeProvider)attribute).setValue(value);
            else throw new ConversionException(attribute, JmxAttributeProvider.class);
        }
    }

    private static final class JmxNotificationSupport extends AbstractNotificationSupport implements javax.management.NotificationListener, ConnectionEstablishedEventHandler {
        private final JmxConnectionManager connectionManager;

        public JmxNotificationSupport(final JmxConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            this.connectionManager.addReconnectionHandler(this);
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
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
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
            failedToDisableNotifications(logger, Level.WARNING, listID, e);
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
            failedToSubscribe(logger, Level.WARNING, listenerID, e);
        }

        private Set<ObjectName> getNotificationTargets() {
            final Set<ObjectName> targets = new HashSet<>(10);
            for (final String category : getCategories())
                for (final JmxNotificationMetadata eventData : getEnabledNotifications(category, JmxNotificationMetadata.class).values())
                    targets.add(eventData.eventOwner);
            return targets;
        }

        private void enableListening(final ObjectName target) throws Exception {
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {

                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.addNotificationListener(target, JmxNotificationSupport.this, null, null);
                    return null;
                }
            });
        }

        private void disableListening(final ObjectName target) throws Exception {
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.removeNotificationListener(target, JmxNotificationSupport.this);
                    return null;
                }
            });
        }

        private void disableNotifications(final JmxNotificationMetadata notificationType) throws Exception {
            final Set<ObjectName> targets = getNotificationTargets();
            if (!targets.contains(notificationType.eventOwner))
                disableListening(notificationType.eventOwner);
        }

        /**
         * Adds a new listener for the specified notification.
         *
         * @param listener The event listener.
         * @return Any custom data associated with the subscription.
         */
        @Override
        @MethodStub
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
        @MethodStub
        protected void unsubscribe(final NotificationListener listener, final Object data) {

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
        protected void disableNotifications(final GenericNotificationMetadata notificationType) throws Exception {
            //remove JMX listener if there is no one active MBean listener
            try {
                if (notificationType instanceof JmxNotificationMetadata)
                    disableNotifications((JmxNotificationMetadata) notificationType);
            } finally {
                notificationType.removeListeners();
            }
        }

        /**
         * Enables event listening for the specified category of events.
         *
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         * @throws java.lang.Exception Internal connector error.
         */
        @Override
        protected final GenericNotificationMetadata enableNotifications(final String category, final Map<String, String> options) throws Exception {
            final JmxNotificationMetadata eventData = connectionManager.handleConnection(new MBeanServerConnectionHandler<JmxNotificationMetadata>() {
                @Override
                public JmxNotificationMetadata handle(final MBeanServerConnection connection) throws IOException, JMException {
                    if (options.containsKey(OBJECT_NAME_PROPERTY)) {
                        final ObjectName on = new ObjectName(options.get(OBJECT_NAME_PROPERTY));
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(on).getNotifications())
                            for (final String notifType : notificationInfo.getNotifTypes())
                                if (Objects.equals(notifType, category))
                                    return new JmxNotificationMetadata(notifType, notificationInfo.getDescriptor(), on, options);
                        return null;
                    } else return null;
                }
            });
            if (eventData != null) {
                //checks whether the enabled MBean object already listening
                final Set<ObjectName> listeningContext = getNotificationTargets();
                if (!listeningContext.contains(eventData.eventOwner))
                    enableListening(eventData.eventOwner);
            }
            return eventData;
        }

        private void handleNotification(final ObjectName source, final javax.management.Notification notification) {
            final Map<String, JmxNotificationMetadata> enabledNotifs = getEnabledNotifications(notification.getType(), JmxNotificationMetadata.class);
            for (final JmxNotificationMetadata eventMetadata : enabledNotifs.values())
                if (source.equals(eventMetadata.eventOwner)) eventMetadata.fire(notification);
        }

        @Override
        public final void handleNotification(final javax.management.Notification notification, final Object handback) {
            //iterates through all listeners and executes it
            if (notification.getSource() instanceof ObjectName)
                handleNotification((ObjectName) notification.getSource(), notification);
            else
                logger.warning(String.format("Unable to handle notification %s because source is unknown", notification));
        }

        public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
            //for each MBean object assigns notification listener
            for (final ObjectName target : getNotificationTargets())
                connection.addNotificationListener(target, this, null, null);
            return null;
        }

        public final void unsubscribeAll() throws Exception {
            for (final ObjectName target : getNotificationTargets())
                disableListening(target);
        }
    }


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

        @SuppressWarnings("UnusedDeclaration")
        public int depth(){
            return path.length;
        }

        @SuppressWarnings("UnusedDeclaration")
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

        public Object getValue(final Object root)
        {
            return getValue(root, 0);
        }

        private OpenType<?> getType(final OpenType<?> root, final int index)
        {
            if(root instanceof CompositeType && index < path.length){
                final CompositeType cdata = (CompositeType)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getType(cdata.getType(subattr), index + 1) : root;
            }
            else return root;
        }

        public OpenType<?> getType(final OpenType<?> root)
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
         * @param attributeName The name of the attribute.
         * @return {@literal true}, if the specified attribute is a decomposition; otherwise, {@literal false}.
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
         * @return The owner of this attribute.
         */
        @SuppressWarnings("UnusedDeclaration")
        public ObjectName getOwner();

        /**
         * Returns the type of the attribute value.
         * @return The type of the attribute value.
         */
        @Override
        public JmxManagedEntityType getType();
    }

    /**
     * Represents an abstract class for building JMX attribute providers.
     */
    private abstract static class JmxAttributeProvider extends GenericAttributeMetadata<JmxManagedEntityType> implements JmxAttributeMetadata {
        private final ObjectName namespace;
        private MBeanServerConnectionHandler<Object> attributeValueReader;
        private final JmxConnectionManager connectionManager;
        private final Map<String, String> options;

        protected JmxAttributeProvider(final JmxConnectionManager manager,
                                       final String attributeName,
                                       final ObjectName namespace,
                                       final Map<String, String> options){
            super(attributeName);
            this.connectionManager = manager;
            this.namespace = namespace;
            options.put(JMX_ENTITY_OPTION, Boolean.toString(true));
            this.options = Collections.unmodifiableMap(options);
        }

        protected final OpenType<?> getTypeFromAttributeValue() throws Exception {
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<OpenType<?>>() {
                @Override
                public OpenType<?> handle(final MBeanServerConnection connection) throws IOException, JMException {
                    return JmxTypeSystem.getOpenTypeFromValue(connection.getAttribute(namespace, getName()));
                }
            });
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

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<String> keySet() {
            return options.keySet();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Collection<String> values() {
            return options.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public final Set<Entry<String, String>> entrySet() {
            return options.entrySet();
        }

        /**
         * Creates a new instance of the attribute value reader.
         * @return A new instance of the attribute value reader.
         */
        protected abstract MBeanServerConnectionHandler<Object> createAttributeValueReader();

        /**
         * Creates a new instance of the attribute value writer.
         * @return A new instance of the attribute value writer.
         */
        protected abstract MBeanServerConnectionHandler<Boolean> createAttributeValueWriter(final Object value);

        /**
         * Returns the attribute owner.
         * @return An owner of this attribute.
         */
        public final ObjectName getOwner(){
            return namespace;
        }

        /**
         * Returns the value of the attribute.
         * @return The value of the attribute.
         * @throws java.lang.Exception Error when reading attribute.
         */
        public final Object getValue() throws Exception{
            if(canRead()){
                if(attributeValueReader == null) attributeValueReader = createAttributeValueReader();
                return connectionManager.handleConnection(attributeValueReader);
            }
            else throw new IllegalStateException("Attribute is write-only");
        }

        /**
         * Writes the value to the attribute.
         * @param value The value to write.
         * @throws java.lang.Exception Error when updating attribute.
         */
        public final void setValue(Object value) throws Exception {
            final JmxManagedEntityType typeInfo = getType();
            if (canWrite()) {
                value = typeInfo.convertToJmx(value);
                connectionManager.handleConnection(createAttributeValueWriter(value));
            } else throw new IllegalStateException("Attribute is read-only");
        }
    }

    private final static class JmxNotificationWrapper extends HashMap<String, Object> implements com.itworks.snamp.connectors.notifications.Notification {
        private final javax.management.Notification jmxNotification;
        private final Severity notificationSeverity;
        private transient volatile Object userData;

        public JmxNotificationWrapper(final Severity severity, final javax.management.Notification jmxNotification){
            userData = null;
            this.jmxNotification = jmxNotification;
            notificationSeverity = severity != null ? severity : Severity.UNKNOWN;
            //loads notification as Java Bean and explore each JB property as attachment
            try {
                final BeanInfo descriptor = Introspector.getBeanInfo(jmxNotification.getClass(), javax.management.Notification.class);
                for(final PropertyDescriptor property: descriptor.getPropertyDescriptors())
                    put(property.getName(), Utils.getProperty(jmxNotification, descriptor, property.getName()));
            }
            catch (final java.beans.IntrospectionException | ReflectiveOperationException e) {
                logger.log(Level.WARNING, "Unable to wrap MBean notification into map.", e);
            }
        }

        /**
         * Gets user data associated with this object.
         *
         * @return The user data associated with this object.
         */
        @Override
        public Object getUserData() {
            return userData;
        }

        /**
         * Sets the user data associated with this object.
         *
         * @param value The user data to be associated with this object.
         */
        @Override
        public void setUserData(final Object value) {
            userData = value;
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


    private final JmxNotificationSupport notifications;
    private final JmxAttributeSupport attributes;
    private final JmxConnectionManager connectionManager;

    public JmxConnector(final JmxConnectionOptions connectionOptions) {
        super(connectionOptions, logger);
        this.connectionManager = connectionOptions.createConnectionManager();
        //attempts to establish connection immediately
        connectionManager.connect();
        this.notifications = new JmxNotificationSupport(connectionManager);
        this.attributes = new JmxAttributeSupport(connectionManager);
    }

    public JmxConnector(final String connectionString, final Map<String, String> connectionOptions) throws MalformedURLException {
        this(new JmxConnectionOptions(connectionString, connectionOptions));
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
    public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) throws NotificationSupportException{
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
    public final boolean disableNotifications(final String listId) throws NotificationSupportException{
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
     * @param delayed Specifies delayed subscription.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    @Override
    public void subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) throws NotificationSupportException {
        verifyInitialization();
        notifications.subscribe(listenerId, listener, delayed);
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously passed to {@link #subscribe(String, com.itworks.snamp.connectors.notifications.NotificationListener, boolean)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final String listenerId) {
        verifyInitialization();
        return notifications.unsubscribe(listenerId);
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
    public AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) throws AttributeSupportException {
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
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException Unregistered attribute detected.
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
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException Unregistered attribute requested.
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
     * @param writeTimeout Batch write operation timeout.
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
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        if(Objects.equals(JmxConnectionManager.class, objectType))
            return objectType.cast(connectionManager);
        else return super.queryObject(objectType);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws java.lang.Exception Some I/O error occurs.
     */
    @Override
    public final void close() throws Exception{
        super.close();
        attributes.clear();
        notifications.unsubscribeAll();
        notifications.clear();
        connectionManager.close();
    }
}
