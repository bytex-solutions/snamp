package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.OBJECT_NAME_PROPERTY;
import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.USE_REGEXP_PARAM;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector<JmxConnectionOptions> implements AttributeSupport, NotificationSupport {
    /**
     * Represents JMX connector name.
     */
    static final String NAME = JmxConnectorHelpers.CONNECTOR_NAME;

    private static interface JmxFeatureMetadata extends Serializable, DescriptorRead {
        @SpecialUse
        ObjectName getOwner();
        @SpecialUse
        String getName();
        @SpecialUse
        String getDescription();
    }

    /**
     * Represents JMX attribute metadata.
     */
    private static interface JmxAttributeMetadata extends OpenMBeanAttributeInfo, JmxFeatureMetadata {
        @SpecialUse
        String getType();
    }

    private static interface JmxNotificationMetadata extends JmxFeatureMetadata{
        @SpecialUse
        String[] getNotifTypes();
    }

    private final static class JmxNotificationInfo extends CustomNotificationInfo implements JmxNotificationMetadata{
        private static final long serialVersionUID = -2040203631422591069L;
        /**
         * Represents owner of this notification metadata.
         */
        private final ObjectName eventOwner;

        private JmxNotificationInfo(final String listID,
                                       final MBeanNotificationInfo nativeNotif,
                                       final ObjectName eventOwner,
                                       final NotificationDescriptor descriptor) {
            super(listID,
                    nativeNotif.getDescription(),
                    descriptor.setFields(nativeNotif.getDescriptor()));
            this.eventOwner = eventOwner;
        }

        @Override
        public ObjectName getOwner(){
            return eventOwner;
        }
    }

    private static final class JmxAttributeSupport extends AbstractAttributeSupport<JmxAttributeInfo>{
        private final JmxConnectionManager connectionManager;

        private JmxAttributeSupport(final JmxConnectionManager connectionManager){
            super(JmxAttributeInfo.class);
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
            JmxConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
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
            JmxConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
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
            JmxConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
                }
            });
        }

        private JmxAttributeInfo createPlainAttribute(final ObjectName namespace,
                                                          final String attributeName,
                                                          final AttributeDescriptor metadata) throws Exception{
            //extracts JMX attribute metadata
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
                @Override
                public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                        if (Objects.equals(attr.getName(), metadata.getAttributeName())) return attr;
                    return null;
                }
            });
            if(targetAttr == null) throw new AttributeNotFoundException(attributeName);
            else return new JmxAttributeInfo(attributeName, targetAttr, namespace, metadata);
        }

        private JmxAttributeInfo createCompositeAttribute(final ObjectName namespace, final String attributeName, final AttributeDescriptor metadata) throws Exception {
            final CompositeValueNavigator navigator = new CompositeValueNavigator(metadata.getAttributeName());
            //получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanAttributeInfo>() {
                @Override
                public MBeanAttributeInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                        if (Objects.equals(navigator.attributeName, attr.getName())) return attr;
                    return null;
                }
            });
            if (targetAttr == null) throw new AttributeNotFoundException(attributeName);
            else return new JmxCompositeAttributeInfo(attributeName,
                    targetAttr,
                    namespace,
                    metadata,
                    navigator);
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

        private static boolean useRegexpOption(final AttributeDescriptor options) {
            return options.hasField(USE_REGEXP_PARAM) &&
                    Objects.equals(Boolean.TRUE.toString(), options.getField(USE_REGEXP_PARAM, String.class));
        }

        private JmxAttributeInfo connectAttribute(final ObjectName namespace,
                                                  final String attributeName,
                                                  final AttributeDescriptor metadata,
                                                  final boolean useRegexp) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            if(namespace == null) return null;
            if(CompositeValueNavigator.isCompositeAttribute(metadata.getAttributeName()))
                return createCompositeAttribute(namespace, attributeName, metadata);
            else if(useRegexp) return connectAttribute(findObjectName(namespace), attributeName, metadata, false);
            else return createPlainAttribute(namespace, attributeName, metadata);
        }

        private JmxAttributeInfo connectAttribute(final ObjectName namespace,
                                                  final String attributeName,
                                                  final AttributeDescriptor metadata) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            return connectAttribute(namespace, attributeName, metadata, useRegexpOption(metadata));
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeID The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute.
         * @throws Exception Internal connector error.
         */
        @Override
        protected JmxAttributeInfo connectAttribute(final String attributeID,
                                                    final AttributeDescriptor descriptor) throws Exception {
            final String namespace = descriptor.getField(OBJECT_NAME_PROPERTY, String.class);
            JmxConnectorLimitations.current().verifyMaxAttributeCount(attributesCount());
            return connectAttribute(new ObjectName(namespace), attributeID, descriptor);
        }

        private Object getAttribute(final JmxCompositeAttributeInfo metadata) throws Exception {
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<Object>() {
                @Override
                public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                    final Object value = connection.getAttribute(metadata.getOwner(),
                            AttributeDescriptor.getAttributeName(metadata));
                    return value != null ? metadata.navigator.getValue(value) : null;
                }
            });
        }

        /**
         * Obtains the value of a specific attribute of the managed resource.
         *
         * @param metadata The metadata of the attribute.
         * @return The value of the attribute retrieved.
         * @throws Exception Internal connector error.
         */
        @Override
        protected Object getAttribute(final JmxAttributeInfo metadata) throws Exception {
            return metadata instanceof JmxCompositeAttributeInfo ?
                    getAttribute((JmxCompositeAttributeInfo)metadata):
                    connectionManager.handleConnection(new MBeanServerConnectionHandler<Object>() {
                        @Override
                        public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                            return connection.getAttribute(metadata.getOwner(),
                                    AttributeDescriptor.getAttributeName(metadata));
                        }
                });
        }

        /**
         * Set the value of a specific attribute of the managed resource.
         *
         * @param attribute The attribute of to set.
         * @param value     The value of the attribute.
         * @throws Exception                                       Internal connector error.
         * @throws javax.management.InvalidAttributeValueException Incompatible attribute type.
         */
        @Override
        protected void setAttribute(final JmxAttributeInfo attribute, final Object value) throws Exception {
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.setAttribute(attribute.getOwner(),
                            new Attribute(AttributeDescriptor.getAttributeName(attribute), value));
                    return null;
                }
            });
        }
    }

    private static final class JmxNotificationSupport extends AbstractNotificationSupport<JmxNotificationInfo> implements NotificationListener, ConnectionEstablishedEventHandler {
        private final JmxConnectionManager connectionManager;
        private final NotificationListenerInvoker listenerInvoker;

        private JmxNotificationSupport(final JmxConnectionManager connectionManager) {
            super(JmxNotificationInfo.class);
            this.connectionManager = connectionManager;
            this.connectionManager.addReconnectionHandler(this);
            listenerInvoker = createListenerInvoker(Executors.newSingleThreadExecutor());
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor){
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, new NotificationListenerInvokerFactory.ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    JmxConnectorHelpers.log(Level.SEVERE, "Unable to process JMX notification.", e);
                }
            });
        }

        /**
         * Gets the invoker used to executed notification listeners.
         *
         * @return The notification listener invoker.
         */
        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
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
            JmxConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
                }
            });
        }

        private Set<ObjectName> getNotificationTargets() {
            final JmxNotificationInfo[] notifications = getNotificationInfo();
            final Set<ObjectName> targets = Sets.newHashSetWithExpectedSize(notifications.length);
            for(final JmxNotificationInfo info: notifications)
                targets.add(info.getOwner());
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

        @Override
        protected boolean disableNotifications(final JmxNotificationInfo metadata) {
            final Set<ObjectName> targets = getNotificationTargets();
            if (!targets.contains(metadata.getOwner()))
                try {
                    disableListening(metadata.getOwner());
                    return true;
                } catch (final Exception e) {
                    JmxConnectorHelpers.log(Level.WARNING, String.format("Unable to unsubscribe from %s", metadata.getOwner()), e);
                    return false;
                }
            else return false;
        }


        @Override
        protected JmxNotificationInfo enableNotifications(final String listID,
                                                          final NotificationDescriptor metadata) throws Exception {
            final JmxNotificationInfo eventData = connectionManager.handleConnection(new MBeanServerConnectionHandler<JmxNotificationInfo>() {
                @Override
                public JmxNotificationInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    if (metadata.hasField(OBJECT_NAME_PROPERTY)) {
                        final ObjectName on = new ObjectName(metadata.getField(OBJECT_NAME_PROPERTY, String.class));
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(on).getNotifications())
                            for (final String notifType : notificationInfo.getNotifTypes())
                                if (Objects.equals(notifType, metadata.getNotificationCategory()))
                                    return new JmxNotificationInfo(listID,
                                            notificationInfo,
                                            on,
                                            metadata);
                        return null;
                    } else return null;
                }
            });
            if (eventData != null) {
                //checks whether the enabled MBean object already listening
                final Set<ObjectName> listeningContext = getNotificationTargets();
                if (!listeningContext.contains(eventData.eventOwner))
                    enableListening(eventData.eventOwner);
                return eventData;
            }
            else throw new IllegalArgumentException(String.format("%s notification is not supported", listID));
        }

        @Override
        public final Void handle(final MBeanServerConnection connection) throws IOException, JMException {
            //for each MBean object assigns notification listener
            for (final ObjectName target : getNotificationTargets())
                connection.addNotificationListener(target, this, null, null);
            return null;
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            Object userData = null;
            try {
                userData = UserDataExtractor.getUserData(notification);
            } catch (final OpenDataException | IllegalArgumentException e) {
                JmxConnectorHelpers.log(Level.WARNING, "Unable to process user data %s in notification %s",
                        notification.getUserData(),
                        notification.getType(), e);
            }
            fire(notification.getType(),
                    notification.getMessage(),
                    userData);
        }

        private void unsubscribeAll() throws Exception {
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
        private static final char delimiter = '@';
        /**
         * Represents the name of the attribute that has a composite type.
         */
        private final String attributeName;
        private final String[] path;

        /**
         * Initializes a new field navigator.
         * @param attributeName The name
         */
        private CompositeValueNavigator(final String attributeName)
        {
            if(!isCompositeAttribute(attributeName)) throw new IllegalArgumentException("Неверный формат имени составного атрибута");
            final String[] parts = attributeName.split(new String(new char[]{delimiter}));
            this.attributeName = parts[0];
            this.path = Arrays.copyOfRange(parts, 1, parts.length);
        }

        @SuppressWarnings("UnusedDeclaration")
        private int depth(){
            return path.length;
        }

        @SuppressWarnings("UnusedDeclaration")
        private String item(int index)
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

        private Object getValue(final Object root)
        {
            return getValue(root, 0);
        }

        private OpenType<?> getType(final OpenType<?> root, final int index){
            if(root instanceof CompositeType && index < path.length){
                final CompositeType cdata = (CompositeType)root;
                final String subattr = path[index];
                return cdata.containsKey(subattr) ? getType(cdata.getType(subattr), index + 1) : root;
            }
            else return root;
        }

        private OpenType<?> getType(final OpenType<?> root){
            return getType(root, 0);
        }

        /**
         * Получить полный путь композитного атрибута.
         */
        @Override
        public String toString(){
            return this.attributeName + Arrays.toString(path).replace(", ", new String(new char[]{delimiter}));
        }

        /**
         * Determines whether the attribute name contains subfield path.
         * @param attributeName The name of the attribute.
         * @return {@literal true}, if the specified attribute is a decomposition; otherwise, {@literal false}.
         */
        private static boolean isCompositeAttribute(final String attributeName){
            return attributeName.indexOf(delimiter) >= 0;
        }
    }

    /**
     * Represents an abstract class for building JMX attribute providers.
     */
    private static class JmxAttributeInfo extends OpenMBeanAttributeInfoSupport implements JmxAttributeMetadata {
        private static final long serialVersionUID = 3262046901190396737L;
        final ObjectName namespace;

        private JmxAttributeInfo(final String attributeName,
                                         final MBeanAttributeInfo nativeAttr,
                                         final boolean isWritable,
                                         final ObjectName namespace,
                                         final AttributeDescriptor metadata,
                                         final Function<MBeanAttributeInfo, OpenType<?>> typeResolver) throws OpenDataException{
            super(attributeName,
                    nativeAttr.getDescription(),
                    detectAttributeType(nativeAttr, typeResolver),
                    nativeAttr.isReadable(),
                    isWritable,
                    nativeAttr.isIs(),
                    metadata.setFields(nativeAttr.getDescriptor()));
            this.namespace = namespace;
        }

        private JmxAttributeInfo(final String attributeName,
                                   final MBeanAttributeInfo nativeAttr,
                                   final ObjectName namespace,
                                   final AttributeDescriptor metadata) throws OpenDataException{
            this(attributeName,
                    nativeAttr,
                    nativeAttr.isWritable(),
                    namespace,
                    metadata,
                    new Function<MBeanAttributeInfo, OpenType<?>>() {
                        @Override
                        public OpenType<?> apply(final MBeanAttributeInfo nativeAttr) {
                            return AttributeDescriptor.getOpenType(nativeAttr);
                        }
                    });
        }

        private static OpenType<?> detectAttributeType(final MBeanAttributeInfo nativeAttr,
                                                       final Function<MBeanAttributeInfo, OpenType<?>> resolver) throws OpenDataException {
            final OpenType<?> result = resolver.apply(nativeAttr);
            if (result == null)
                throw new OpenDataException(String.format("Attribute %s with type %s cannot be mapped to Open Type", nativeAttr.getName(), nativeAttr.getType()));
            else return result;
        }

        /**
         * Returns the attribute owner.
         * @return An owner of this attribute.
         */
        @Override
        public final ObjectName getOwner(){
            return namespace;
        }

    }

    private static final class JmxCompositeAttributeInfo extends JmxAttributeInfo{
        private static final long serialVersionUID = -8511917301245817602L;
        private final CompositeValueNavigator navigator;

        private JmxCompositeAttributeInfo(final String attributeName,
                                          final MBeanAttributeInfo nativeAttr,
                                          final ObjectName namespace,
                                          final AttributeDescriptor metadata,
                                          final CompositeValueNavigator navigator) throws OpenDataException {
            super(attributeName,
                    nativeAttr,
                    false,     //composite attribute is always read-only
                    namespace,
                    metadata,
                    new Function<MBeanAttributeInfo, OpenType<?>>() {
                        @Override
                        public OpenType<?> apply(final MBeanAttributeInfo nativeAttr) {
                            return navigator.getType(AttributeDescriptor.getOpenType(nativeAttr));
                        }
                    });
            this.navigator = Objects.requireNonNull(navigator);
        }
    }

    private final JmxNotificationSupport notifications;
    private final JmxAttributeSupport attributes;
    private final JmxConnectionManager connectionManager;

    JmxConnector(final JmxConnectionOptions connectionOptions) {
        super(connectionOptions);
        this.connectionManager = connectionOptions.createConnectionManager();
        //attempts to establish connection immediately
        connectionManager.connect();
        this.notifications = new JmxNotificationSupport(connectionManager);
        this.attributes = new JmxAttributeSupport(connectionManager);
    }

    JmxConnector(final String connectionString, final Map<String, String> connectionOptions) throws MalformedURLException {
        this(new JmxConnectionOptions(connectionString, connectionOptions));
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id               A key string that is used to invoke attribute from this connector.
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     * @throws javax.management.AttributeNotFoundException The managed resource doesn't provide the attribute with the specified name.
     * @throws javax.management.JMException                Internal connector error.
     */
    @Override
    public MBeanAttributeInfo connectAttribute(final String id, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) throws JMException {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, readWriteTimeout, options);
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public MBeanAttributeInfo[] getAttributeInfo() {
        return attributes.getAttributeInfo();
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
     * Enables event listening for the specified category of events.
     * <p/>
     * category can be used for enabling notifications for the same category
     * but with different options.
     * <p/>
     * listId parameter
     * is used as a value of {@link javax.management.Notification#getType()}.
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the event category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     * @throws javax.management.JMException Internal connector error.
     */
    @Override
    public MBeanNotificationInfo enableNotifications(final String listId, final String category, final CompositeData options) throws JMException {
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
     * Adds a listener to this MBean.
     *
     * @param listener The listener object which will handle the
     *                 notifications emitted by the broadcaster.
     * @param filter   The filter object. If filter is null, no
     *                 filtering will be performed before handling notifications.
     * @param handback An opaque object to be sent back to the
     *                 listener when a notification is emitted. This object cannot be
     *                 used by the Notification broadcaster object. It should be
     *                 resent unchanged with the notification to the listener.
     * @throws IllegalArgumentException Listener parameter is null.
     * @see #removeNotificationListener
     */
    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        verifyInitialization();
        notifications.addNotificationListener(listener, filter, handback);
    }

    /**
     * Removes a listener from this MBean.  If the listener
     * has been registered with different handback objects or
     * notification filters, all entries corresponding to the listener
     * will be removed.
     *
     * @param listener A listener that was previously added to this
     *                 MBean.
     * @throws javax.management.ListenerNotFoundException The listener is not
     *                                                    registered with the MBean.
     * @see #addNotificationListener
     * @see javax.management.NotificationEmitter#removeNotificationListener
     */
    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        verifyInitialization();
        notifications.removeNotificationListener(listener);
    }

    /**
     * <p>Returns an array indicating, for each notification this
     * MBean may send, the name of the Java class of the notification
     * and the notification type.</p>
     * <p/>
     * <p>It is not illegal for the MBean to send notifications not
     * described in this array.  However, some clients of the MBean
     * server may depend on the array being complete for their correct
     * functioning.</p>
     *
     * @return the array of possible notifications.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifications.getNotificationInfo();
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attributeID The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attributeID) throws AttributeNotFoundException, MBeanException, ReflectionException {
        verifyInitialization();
        return attributes.getAttribute(attributeID);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        verifyInitialization();
        attributes.setAttribute(attribute);
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        verifyInitialization();
        return this.attributes.getAttributes(attributes);
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        verifyInitialization();
        return this.attributes.setAttributes(attributes);
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