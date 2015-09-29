package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeDescriptorRead;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.*;
import com.bytex.snamp.connectors.operations.AbstractOperationRepository;
import com.bytex.snamp.connectors.operations.OperationDescriptor;
import com.bytex.snamp.connectors.operations.OperationDescriptorRead;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.SpecialUse;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector implements AttributeSupport, NotificationSupport, OperationSupport {
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

    private final static class JmxOperationRepository extends AbstractOperationRepository<JmxOperationInfo> {
        private final JmxConnectionManager connectionManager;
        private final ObjectName globalObjectName;
        private static final Class<JmxOperationInfo> FEATURE_TYPE = JmxOperationInfo.class;

        private JmxOperationRepository(final String resourceName,
                                       final ObjectName globalName,
                                       final JmxConnectionManager connectionManager){
            super(resourceName, FEATURE_TYPE);
            this.globalObjectName = globalName;
            this.connectionManager = connectionManager;
        }

        private static JmxOperationInfo enableOperation(final JmxConnectionManager connectionManager,
                                                        final String userDefinedName,
                                                        final OperationDescriptor descriptor,
                                                        final ObjectName owner,
                                                        final boolean useRegexp) throws Exception{
            if(useRegexp)
                return enableOperation(connectionManager, userDefinedName, descriptor, connectionManager.resolveName(owner), false);
            final MBeanOperationInfo metadata = connectionManager.handleConnection(new MBeanServerConnectionHandler<MBeanOperationInfo>() {
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
                                                 final ObjectName owner,
                                                 final boolean useRegexp) throws Exception {
            return enableOperation(connectionManager, userDefinedName, descriptor, owner, useRegexp);
        }

        private JmxOperationInfo enableOperation(final String userDefinedName,
                                                 final OperationDescriptor descriptor,
                                                 final ObjectName owner) throws Exception{
            return enableOperation(userDefinedName, descriptor, owner, useRegexpOption(descriptor));
        }

        @Override
        protected JmxOperationInfo enableOperation(final String userDefinedName,
                                                   final OperationDescriptor descriptor) throws Exception {
            return enableOperation(userDefinedName, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
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
            return callInfo.getMetadata().invoke(connectionManager, callInfo.toArray());
        }

        @Override
        protected void failedToEnableOperation(final String userDefinedName,
                                               final String operationName,
                                               final Exception e) {
            failedToEnableOperation(getLoggerImpl(), Level.WARNING, userDefinedName, operationName, e);
        }

        @Override
        public Collection<JmxOperationInfo> expand() {
            if (globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxOperationInfo>>() {
                    private void fillOperations(final MBeanServerConnection connection,
                                                final ObjectName objectName,
                                                final boolean generateName,
                                                final Collection<JmxOperationInfo> output) throws JMException, IOException {
                        int counter = 1;
                        for (final MBeanOperationInfo operationInfo : connection.getMBeanInfo(objectName).getOperations()) {
                            final String operationID = generateName ?
                                    operationInfo.getName() + counter++ :
                                    operationInfo.getName();
                            final JmxOperationInfo op = enableOperation(operationID,
                                    operationInfo.getName(),
                                    TIMEOUT_FOR_SMART_MODE,
                                    toConfigurationParameters(globalObjectName));
                            if (op != null) output.add(op);
                        }
                    }

                    @Override
                    public LinkedList<JmxOperationInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxOperationInfo> result = new LinkedList<>();
                        if (globalObjectName == null)
                            for (final ObjectName objectName : connection.queryNames(null, null))
                                fillOperations(connection, objectName, true, result);
                        else
                            fillOperations(connection, globalObjectName, false, result);
                        return result;
                    }
                });
            } catch (final Exception e) {
                failedToExpand(getLoggerImpl(), Level.WARNING, e);
                return Collections.emptyList();
            }
        }

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
            return featureType.isAssignableFrom(FEATURE_TYPE);
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

    private static final class JmxAttributeRepository extends AbstractAttributeRepository<JmxAttributeInfo> {
        private final JmxConnectionManager connectionManager;
        private final ObjectName globalObjectName;
        private static final Class<JmxAttributeInfo> FEATURE_TYPE = JmxAttributeInfo.class;

        private JmxAttributeRepository(final String resourceName,
                                       final ObjectName globalName,
                                       final JmxConnectionManager connectionManager){
            super(resourceName, FEATURE_TYPE);
            this.globalObjectName = globalName;
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
            failedToConnectAttribute(getLoggerImpl(), Level.WARNING, attributeID, attributeName, e);
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
            failedToGetAttribute(getLoggerImpl(), Level.WARNING, attributeID, e);
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
            failedToSetAttribute(getLoggerImpl(), Level.WARNING, attributeID, value, e);
        }

        private static JmxAttributeInfo createPlainAttribute(final JmxConnectionManager connectionManager,
                                                             final ObjectName namespace,
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

        private JmxAttributeInfo createPlainAttribute(final ObjectName namespace,
                                                          final String attributeName,
                                                          final AttributeDescriptor metadata) throws Exception {
            return createPlainAttribute(connectionManager, namespace, attributeName, metadata);
        }

        private JmxAttributeInfo connectAttribute(final String attributeName,
                                                  final AttributeDescriptor metadata,
                                                  final boolean useRegexp,
                                                  final ObjectName namespace) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            if(namespace == null) return null;
            else if(useRegexp) return connectAttribute(attributeName, metadata, false, connectionManager.resolveName(namespace)
            );
            else return createPlainAttribute(namespace, attributeName, metadata);
        }

        private JmxAttributeInfo connectAttribute(final String attributeName,
                                                  final AttributeDescriptor metadata,
                                                  final ObjectName namespace) throws Exception{
            //creates JMX attribute provider based on its metadata and connection options.
            return connectAttribute(attributeName, metadata, useRegexpOption(metadata), namespace);
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
            return connectAttribute(attributeID, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
        }

        @Override
        public List<JmxAttributeInfo> expand() {
            if(globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxAttributeInfo>>() {
                    private void fillAttributes(final MBeanServerConnection connection,
                                                final ObjectName objectName,
                                                final boolean generateName,
                                                final Collection<JmxAttributeInfo> output) throws JMException, IOException{
                        int counter = 1;
                        for(final MBeanAttributeInfo attributeInfo: connection.getMBeanInfo(objectName).getAttributes()){
                            final String attributeID = generateName ?
                                    attributeInfo.getName() + counter++ :
                                    attributeInfo.getName();
                            final JmxAttributeInfo attr = addAttribute(attributeID,
                                    attributeInfo.getName(),
                                    TIMEOUT_FOR_SMART_MODE,
                                    toConfigurationParameters(globalObjectName));
                            if(attr != null) output.add(attr);
                        }
                    }

                    @Override
                    public LinkedList<JmxAttributeInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxAttributeInfo> result = new LinkedList<>();
                        if(globalObjectName == null)
                            for(final ObjectName objectName : connection.queryNames(null, null))
                                fillAttributes(connection, objectName, true, result);
                        else
                            fillAttributes(connection, globalObjectName, false, result);
                        return result;
                    }
                });
            } catch (final Exception e) {
                failedToExpand(getLoggerImpl(), Level.WARNING, e);
                return Collections.emptyList();
            }
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

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
            return featureType.isAssignableFrom(FEATURE_TYPE);
        }
    }

    private static final class JmxNotificationRepository extends AbstractNotificationRepository<JmxNotificationInfo> implements NotificationListener, ConnectionEstablishedEventHandler {
        private final JmxConnectionManager connectionManager;
        private final NotificationListenerInvoker listenerInvoker;
        private final ObjectName globalObjectName;
        private static final Class<JmxNotificationInfo> FEATURE_TYPE = JmxNotificationInfo.class;

        private JmxNotificationRepository(final String resourceName,
                                          final ObjectName globalName,
                                          final JmxConnectionManager connectionManager) {
            super(resourceName, FEATURE_TYPE);
            this.connectionManager = connectionManager;
            this.globalObjectName = globalName;
            this.connectionManager.addReconnectionHandler(this);
            listenerInvoker = createListenerInvoker(Executors.newSingleThreadExecutor(new GroupedThreadFactory("notifs-".concat(resourceName))));
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor){
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, new NotificationListenerInvokerFactory.ExceptionHandler() {
                @Override
                public final void handle(final Throwable e, final NotificationListener source) {
                    getLoggerImpl().log(Level.SEVERE, "Unable to process JMX notification", e);
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
            failedToEnableNotifications(getLoggerImpl(), Level.WARNING, listID, category, e);
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
                    connection.addNotificationListener(target, JmxNotificationRepository.this, null, null);
                    return null;
                }
            });
        }

        private void disableListening(final ObjectName target) throws Exception {
            connectionManager.handleConnection(new MBeanServerConnectionHandler<Void>() {
                @Override
                public final Void handle(final MBeanServerConnection connection) throws IOException, InstanceNotFoundException {
                    try {
                        connection.removeNotificationListener(target, JmxNotificationRepository.this);
                    } catch (final ListenerNotFoundException ignored) {
                    }
                    return null;
                }
            });
        }

        @Override
        protected void disableNotifications(final JmxNotificationInfo metadata) {
            final Set<ObjectName> targets = getNotificationTargets();
            if (!targets.contains(metadata.getOwner()))
                try {
                    disableListening(metadata.getOwner());
                } catch (final Exception e) {
                    getLoggerImpl().log(Level.WARNING, String.format("Unable to unsubscribe from %s", metadata.getOwner()), e);
                }
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
            return enableNotifications(listID, metadata, globalObjectName == null ? getObjectName(metadata) : globalObjectName);
        }

        @Override
        public List<JmxNotificationInfo> expand() {
            if (globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxNotificationInfo>>() {
                    private void fillNotifications(final MBeanServerConnection connection,
                                                   final ObjectName objectName,
                                                   final boolean generateName,
                                                   final Collection<JmxNotificationInfo> output) throws JMException, IOException {
                        int counter = 1;
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(objectName).getNotifications()) {
                            if (notificationInfo.getNotifTypes().length < 1) continue;
                            final String notificationId = generateName ?
                                    ArrayUtils.getFirst(notificationInfo.getNotifTypes()) + counter++ :
                                    ArrayUtils.getFirst(notificationInfo.getNotifTypes());
                            final JmxNotificationInfo event = enableNotifications(notificationId,
                                    ArrayUtils.getFirst(notificationInfo.getNotifTypes()),
                                    toConfigurationParameters(globalObjectName));
                            if (event != null) output.add(event);
                        }
                    }

                    @Override
                    public LinkedList<JmxNotificationInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxNotificationInfo> result = new LinkedList<>();
                        if (globalObjectName == null)
                            for (final ObjectName objectName : connection.queryNames(null, null))
                                fillNotifications(connection, objectName, true, result);
                        else
                            fillNotifications(connection, globalObjectName, false, result);
                        return result;
                    }
                });
            } catch (final Exception e) {
                failedToExpand(getLoggerImpl(), Level.WARNING, e);
                return Collections.emptyList();
            }
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
                getLoggerImpl().log(Level.WARNING, String.format("Unable to process user data %s in notification %s",
                        notification.getUserData(),
                        notification.getType()), e);
            }
            fire(notification.getType(),
                    notification.getMessage(),
                    notification.getSequenceNumber(),
                    notification.getTimeStamp(),
                    userData);
        }

        private void unsubscribeAll() throws Exception {
            for (final ObjectName target : getNotificationTargets())
                disableListening(target);
        }

        private static boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
            return featureType.isAssignableFrom(FEATURE_TYPE);
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
    private final JmxNotificationRepository notifications;
    private final JmxAttributeRepository attributes;
    private final JmxConnectionManager connectionManager;
    private final JmxOperationRepository operations;
    private final boolean smartMode;

    JmxConnector(final String resourceName,
                 final JmxConnectionOptions connectionOptions) {
        this.connectionManager = connectionOptions.createConnectionManager(getLogger());
        //attempts to establish connection immediately
        connectionManager.connect();
        //SmartMode can be enabled if
        if(connectionOptions.isSmartModeEnabled()){
            smartMode = connectionOptions.getGlobalObjectName() != null;
            if(!smartMode)
                getLogger().log(Level.SEVERE, String.format("SmartMode cannot be enabled for %s resource. Configuration property '%s' is not specified",
                        resourceName,
                        JmxConnectorConfigurationDescriptor.OBJECT_NAME_PROPERTY));
        }
        else smartMode = false;
        this.notifications = new JmxNotificationRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
        this.attributes = new JmxAttributeRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
        this.operations = new JmxOperationRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
    }

    JmxConnector(final String resourceName,
                 final String connectionString,
                 final Map<String, String> connectionOptions) throws MalformedURLException, MalformedObjectNameException {
        this(resourceName, new JmxConnectionOptions(connectionString, connectionOptions));
    }

    boolean addAttribute(final String id,
                                    final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
        verifyInitialization();
        return attributes.addAttribute(id, attributeName, readWriteTimeout, options) != null;
    }

    boolean enableNotifications(final String listId,
                                              final String category,
                                              final CompositeData options) {
        verifyInitialization();
        return notifications.enableNotifications(listId, category, options) != null;
    }

    boolean enableOperation(final String operationID,
                                       final String operationName,
                                       final TimeSpan invocationTimeout,
                                       final CompositeData options){
        verifyInitialization();
        return operations.enableOperation(operationID, operationName, invocationTimeout, options) != null;
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

    @Override
    public boolean canExpandWith(final Class<? extends MBeanFeatureInfo> featureType) {
        return this.smartMode && (JmxAttributeRepository.canExpandWith(featureType) ||
                JmxNotificationRepository.canExpandWith(featureType) ||
                JmxOperationRepository.canExpandWith(featureType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends MBeanFeatureInfo> Collection<? extends F> expand(final Class<F> featureType) {
        if(this.smartMode)
            if(attributes.canExpandWith(featureType))
                return (Collection<F>)attributes.expand();
            else if(notifications.canExpandWith(featureType))
                return (Collection<F>)notifications.expand();
            else if(operations.canExpandWith(featureType))
                return (Collection<F>)operations.expand();
        return Collections.emptyList();
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    void disableNotificationsExcept(final Set<String> events) {
        this.notifications.removeAllExcept(events);
    }

    void disableOperationsExcept(final Set<String> operations) {
        this.operations.removeAllExcept(operations);
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
        return getLogger(getType());
    }

    static String getType(){
        return getConnectorType(JmxConnector.class);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws java.lang.Exception Some I/O error occurs.
     */
    @Override
    public final void close() throws Exception{
        attributes.removeAll(true);
        notifications.unsubscribeAll();
        notifications.removeAll(true, true);
        operations.removeAll(true);
        super.close();
        connectionManager.close();
    }
}