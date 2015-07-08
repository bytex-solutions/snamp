package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.configuration.AbsentConfigurationParameterException;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AbstractAttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptorRead;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.connectors.operations.AbstractOperationSupport;
import com.itworks.snamp.connectors.operations.OperationDescriptor;
import com.itworks.snamp.connectors.operations.OperationDescriptorRead;
import com.itworks.snamp.connectors.operations.OperationSupport;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector implements AttributeSupport, NotificationSupport, OperationSupport {
    /**
     * Represents JMX connector name.
     */
    static final String NAME = JmxConnectorHelpers.CONNECTOR_NAME;

    private interface JmxFeatureMetadata extends Serializable, DescriptorRead {
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
    private interface JmxAttributeMetadata extends OpenMBeanAttributeInfo, JmxFeatureMetadata, AttributeDescriptorRead {
        @SpecialUse
        String getType();
    }

    private interface JmxNotificationMetadata extends JmxFeatureMetadata, NotificationDescriptorRead{
        @SpecialUse
        String[] getNotifTypes();
    }

    private interface JmxOperationMetadata extends JmxFeatureMetadata, OperationDescriptorRead{
        @SpecialUse
        String getReturnType();
        @SpecialUse
        MBeanParameterInfo[] getSignature();
        @SpecialUse
        int getImpact();
    }

    private final static class JmxOperationInfo extends MBeanOperationInfo implements JmxOperationMetadata{
        private static final long serialVersionUID = -2143203631423581065L;
        private final ObjectName operationOwner;
        private final OperationDescriptor descriptor;

        private JmxOperationInfo(final String operationID,
                                 final MBeanOperationInfo nativeOp,
                                 final ObjectName owner,
                                 OperationDescriptor descriptor){
            super(operationID,
                    nativeOp.getDescription(),
                    nativeOp.getSignature(),
                    nativeOp.getReturnType(),
                    nativeOp.getImpact(),
                    descriptor = descriptor.setFields(nativeOp.getDescriptor()));
            this.operationOwner = owner;
            this.descriptor = descriptor;
        }

        @Override
        public OperationDescriptor getDescriptor() {
            return MoreObjects.firstNonNull(descriptor, OperationDescriptor.EMPTY_DESCRIPTOR);
        }

        @Override
        public ObjectName getOwner() {
            return operationOwner;
        }

        private static Object invoke(final JmxConnectionManager connectionManager,
                                     final Object[] arguments,
                                     final String[] signature,
                                     final ObjectName owner,
                                     final OperationDescriptor descriptor) throws Exception{
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<Object>() {
                @Override
                public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                    return connection.invoke(owner,
                            OperationDescriptor.getOperationName(descriptor),
                            arguments,
                            signature);
                }
            });
        }

        private static String[] constructSignature(final MBeanParameterInfo[] signature){
            final String[] result = new String[signature.length];
            for(int i = 0; i < signature.length; i++)
                result[i] = signature[i].getType();
            return result;
        }

        private Object invoke(final JmxConnectionManager connectionManager,
                              final Object[] arguments) throws Exception{
            return invoke(connectionManager,
                    arguments,
                    constructSignature(getSignature()),
                    operationOwner,
                    getDescriptor());
        }
    }

    private final static class JmxOperationSupport extends AbstractOperationSupport<JmxOperationInfo>{
        private final JmxConnectionManager manager;

        private JmxOperationSupport(final String resourceName,
                                    final JmxConnectionManager connectionManager){
            super(resourceName, JmxOperationInfo.class);
            this.manager = connectionManager;
        }

        private JmxOperationInfo enableOperation(final String userDefinedName,
                                                 final OperationDescriptor descriptor,
                                                 final ObjectName owner,
                                                 final boolean useRegexp) throws Exception{
            if(useRegexp)
                return enableOperation(userDefinedName, descriptor, manager.resolveName(owner), false);
            final MBeanOperationInfo metadata = manager.handleConnection(new MBeanServerConnectionHandler<MBeanOperationInfo>() {
                @Override
                public MBeanOperationInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for(final MBeanOperationInfo candidate: connection.getMBeanInfo(owner).getOperations())
                        if(Objects.equals(descriptor.getOperationName(), candidate.getName()) && checkSignature(descriptor, candidate.getSignature()))
                            return candidate;
                    return null;
                }
            });
            if(metadata != null)
                return new JmxOperationInfo(userDefinedName, metadata, owner, descriptor);
            else throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist in '%s' object", descriptor.getOperationName(), owner)));
        }

        private JmxOperationInfo enableOperation(final String userDefinedName,
                                                 final OperationDescriptor descriptor,
                                                 final ObjectName owner) throws Exception{
            return enableOperation(userDefinedName, descriptor, owner, useRegexpOption(descriptor));
        }

        @Override
        protected JmxOperationInfo enableOperation(final String userDefinedName,
                                                   final OperationDescriptor descriptor) throws Exception {
            final String namespace = descriptor.getField(OBJECT_NAME_PROPERTY, String.class);
            if(Strings.isNullOrEmpty(namespace))
                throw new AbsentConfigurationParameterException(OBJECT_NAME_PROPERTY);
            return enableOperation(userDefinedName, descriptor, new ObjectName(namespace));
        }

        /**
         * Invokes an operation.
         *
         * @param callInfo Operation call information. Cannot be {@literal null}.
         * @return Invocation result.
         * @throws Exception Unable to invoke operation.
         */
        @Override
        protected Object invoke(final OperationCallInfo<JmxOperationInfo> callInfo) throws Exception {
            return callInfo.getMetadata().invoke(manager, callInfo.toArray());
        }

        @Override
        protected void failedToEnableOperation(final String userDefinedName,
                                               final String operationName,
                                               final Exception e) {
            JmxConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToEnableOperation(logger, Level.SEVERE, userDefinedName, operationName, e);
                }
            });
        }
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

    private static final class JmxAttributeSupport extends AbstractAttributeSupport<JmxAttributeInfo> {
        private final JmxConnectionManager connectionManager;

        private JmxAttributeSupport(final String resourceName,
                                    final JmxConnectionManager connectionManager){
            super(resourceName, JmxAttributeInfo.class);
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



        private JmxAttributeInfo connectAttribute(final ObjectName namespace,
                                                  final String attributeName,
                                                  final AttributeDescriptor metadata,
                                                  final boolean useRegexp) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            if(namespace == null) return null;
            else if(useRegexp) return connectAttribute(connectionManager.resolveName(namespace),
                    attributeName,
                    metadata,
                    false);
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
            if(Strings.isNullOrEmpty(namespace))
                throw new AbsentConfigurationParameterException(OBJECT_NAME_PROPERTY);
            return connectAttribute(new ObjectName(namespace), attributeID, descriptor);
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
            return metadata.getValue(connectionManager);
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
            attribute.setValue(connectionManager, value);
        }
    }

    private static final class JmxNotificationSupport extends AbstractNotificationSupport<JmxNotificationInfo> implements NotificationListener, ConnectionEstablishedEventHandler {
        private final JmxConnectionManager connectionManager;
        private final NotificationListenerInvoker listenerInvoker;

        private JmxNotificationSupport(final String resourceName,
                                       final JmxConnectionManager connectionManager) {
            super(resourceName, JmxNotificationInfo.class);
            this.connectionManager = connectionManager;
            this.connectionManager.addReconnectionHandler(this);
            listenerInvoker = createListenerInvoker(Executors.newSingleThreadExecutor(new GroupedThreadFactory("notifs-" + resourceName)));
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
                public final Void handle(final MBeanServerConnection connection) throws IOException, InstanceNotFoundException {
                    try {
                        connection.removeNotificationListener(target, JmxNotificationSupport.this);
                    } catch (final ListenerNotFoundException ignored) {
                    }
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

        private JmxNotificationInfo enableNotifications(final String listID,
                                                        final NotificationDescriptor metadata,
                                                        final ObjectName owner,
                                                        final boolean useRegexp) throws Exception {
            if (useRegexp)
                return enableNotifications(listID, metadata, connectionManager.resolveName(owner), false);
            final JmxNotificationInfo eventData = connectionManager.handleConnection(new MBeanServerConnectionHandler<JmxNotificationInfo>() {
                @Override
                public JmxNotificationInfo handle(final MBeanServerConnection connection) throws IOException, JMException {
                    for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(owner).getNotifications())
                        for (final String notifType : notificationInfo.getNotifTypes())
                            if (Objects.equals(notifType, metadata.getNotificationCategory()))
                                return new JmxNotificationInfo(listID,
                                        notificationInfo,
                                        owner,
                                        metadata);
                    return null;
                }
            });
            if (eventData != null) {
                //checks whether the enabled MBean object already listening
                final Set<ObjectName> listeningContext = getNotificationTargets();
                if (!listeningContext.contains(eventData.eventOwner))
                    enableListening(eventData.eventOwner);
                return eventData;
            } else throw new IllegalArgumentException(String.format("%s notification is not supported", listID));
        }

        private JmxNotificationInfo enableNotifications(final String listID,
                                                        final NotificationDescriptor metadata,
                                                        final ObjectName namespace) throws Exception {
            return enableNotifications(listID, metadata, namespace, useRegexpOption(metadata));
        }

        @Override
        protected JmxNotificationInfo enableNotifications(final String listID,
                                                          final NotificationDescriptor metadata) throws Exception {
            final String namespace = metadata.getField(OBJECT_NAME_PROPERTY, String.class);
            if(Strings.isNullOrEmpty(namespace))
                throw new AbsentConfigurationParameterException(OBJECT_NAME_PROPERTY);
            return enableNotifications(listID, metadata, new ObjectName(namespace));
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
     * Represents an abstract class for building JMX attribute providers.
     */
    private static class JmxAttributeInfo extends OpenMBeanAttributeInfoSupport implements JmxAttributeMetadata {
        private static final long serialVersionUID = 3262046901190396737L;
        private final ObjectName namespace;
        private final AttributeDescriptor descriptor;

        private JmxAttributeInfo(final String attributeName,
                                         final MBeanAttributeInfo nativeAttr,
                                         final boolean isWritable,
                                         final ObjectName namespace,
                                         AttributeDescriptor metadata,
                                         final Function<MBeanAttributeInfo, OpenType<?>> typeResolver) throws OpenDataException{
            super(attributeName,
                    nativeAttr.getDescription(),
                    detectAttributeType(nativeAttr, typeResolver),
                    nativeAttr.isReadable(),
                    isWritable,
                    nativeAttr.isIs(),
                    metadata = metadata.setFields(nativeAttr.getDescriptor()));
            this.namespace = namespace;
            this.descriptor = metadata;
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
         * Returns the descriptor for the feature.  Changing the returned value
         * will have no affect on the original descriptor.
         *
         * @return a descriptor that is either immutable or a copy of the original.
         * @since 1.6
         */
        @Override
        public AttributeDescriptor getDescriptor() {
            return MoreObjects.firstNonNull(descriptor, AttributeDescriptor.EMPTY_DESCRIPTOR);
        }

        /**
         * Returns the attribute owner.
         * @return An owner of this attribute.
         */
        @Override
        public final ObjectName getOwner(){
            return namespace;
        }

        private static Object getValue(final JmxConnectionManager connectionManager,
                                       final ObjectName owner,
                                       final AttributeDescriptor descriptor) throws Exception{
            return connectionManager.handleConnection(new MBeanServerConnectionHandler<Object>() {
                @Override
                public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
                    return connection.getAttribute(owner,
                            AttributeDescriptor.getAttributeName(descriptor));
                }
            });
        }

        private Object getValue(final JmxConnectionManager connectionManager) throws Exception {
            return getValue(connectionManager, namespace, getDescriptor());
        }

        private static void setValue(final JmxConnectionManager connectionManager,
                                     final ObjectName owner,
                                     final AttributeDescriptor descriptor,
                                     final Object value) throws Exception{
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public Void handle(final MBeanServerConnection connection) throws IOException, JMException {
                    connection.setAttribute(owner,
                            new Attribute(AttributeDescriptor.getAttributeName(descriptor), value));
                    return null;
                }
            });
        }

        private void setValue(final JmxConnectionManager connectionManager, final Object value) throws Exception {
            setValue(connectionManager, namespace, getDescriptor(), value);
        }
    }
    private final JmxNotificationSupport notifications;
    private final JmxAttributeSupport attributes;
    private final JmxConnectionManager connectionManager;
    private final JmxOperationSupport operations;

    JmxConnector(final String resourceName,
                 final JmxConnectionOptions connectionOptions) {
        this.connectionManager = connectionOptions.createConnectionManager();
        //attempts to establish connection immediately
        connectionManager.connect();
        this.notifications = new JmxNotificationSupport(resourceName, connectionManager);
        this.attributes = new JmxAttributeSupport(resourceName, connectionManager);
        this.operations = new JmxOperationSupport(resourceName, connectionManager);
    }

    JmxConnector(final String resourceName,
                 final String connectionString,
                 final Map<String, String> connectionOptions) throws MalformedURLException {
        this(resourceName, new JmxConnectionOptions(connectionString, connectionOptions));
    }

    MBeanAttributeInfo addAttribute(final String id,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
        verifyInitialization();
        return attributes.addAttribute(id, attributeName, readWriteTimeout, options);
    }

    MBeanNotificationInfo enableNotifications(final String listId,
                                              final String category,
                                              final CompositeData options) {
        verifyInitialization();
        return notifications.enableNotifications(listId, category, options);
    }

    MBeanOperationInfo enableOperation(final String operationID,
                                       final String operationName,
                                       final CompositeData options){
        verifyInitialization();
        return operations.enableOperation(operationID, operationName, options);
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
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications, operations);
    }

    /**
     * Gets subscription model.
     *
     * @return The subscription model.
     */
    @Override
    public NotificationSubscriptionModel getSubscriptionModel() {
        return notifications.getSubscriptionModel();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType,
                new Function<Class<T>, T>() {
                    @Override
                    public T apply(final Class<T> objectType) {
                        return JmxConnector.super.queryObject(objectType);
                    }
                }, connectionManager, attributes, notifications, operations);
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
        attributes.clear(true);
        notifications.unsubscribeAll();
        notifications.clear(true, true);
        operations.clear(true);
        super.close();
        connectionManager.close();
    }
}