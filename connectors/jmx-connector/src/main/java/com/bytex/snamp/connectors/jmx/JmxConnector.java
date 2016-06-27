package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeDescriptorRead;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.metrics.MetricsReader;
import com.bytex.snamp.connectors.notifications.*;
import com.bytex.snamp.connectors.operations.AbstractOperationRepository;
import com.bytex.snamp.connectors.operations.OperationDescriptor;
import com.bytex.snamp.connectors.operations.OperationDescriptorRead;
import com.bytex.snamp.connectors.operations.OperationSupport;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import org.osgi.framework.BundleContext;

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

import static com.bytex.snamp.connectors.jmx.JmxConnectorDescriptionProvider.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector implements AttributeSupport, NotificationSupport, OperationSupport {
    private interface JmxFeatureMetadata extends Serializable, DescriptorRead {
        ObjectName getOwner();
        String getName();
        String getDescription();
        String getAlias();
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
                                     final String operationName,
                                     final Object[] arguments,
                                     final String[] signature,
                                     final ObjectName owner) throws Exception{
            return connectionManager.handleConnection(connection -> connection.invoke(owner,
                    operationName,
                    arguments,
                    signature));
        }

        private static String[] constructSignature(final MBeanParameterInfo[] signature){
            final String[] result = new String[signature.length];
            for(int i = 0; i < signature.length; i++)
                result[i] = signature[i].getType();
            return result;
        }

        @Override
        public String getAlias(){
            return getDescriptor().getName(getName());
        }

        private Object invoke(final JmxConnectionManager connectionManager,
                              final Object[] arguments) throws Exception{
            return invoke(connectionManager,
                    getAlias(),
                    arguments,
                    constructSignature(getSignature()),
                    operationOwner
            );
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
                                                        final String operationName,
                                                        final OperationDescriptor descriptor,
                                                        final ObjectName owner,
                                                        final boolean useRegexp) throws Exception{
            if(useRegexp)
                return enableOperation(connectionManager, operationName, descriptor, connectionManager.resolveName(owner), false);
            final MBeanOperationInfo metadata = connectionManager.handleConnection(connection -> {
                for(final MBeanOperationInfo candidate: connection.getMBeanInfo(owner).getOperations())
                    if(Objects.equals(descriptor.getName(operationName), candidate.getName()) && checkSignature(descriptor, candidate.getSignature()))
                        return candidate;
                return null;
            });
            if(metadata != null)
                return new JmxOperationInfo(operationName, metadata, owner, descriptor);
            else throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist in '%s' object", descriptor.getName(operationName), owner)));

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
            return callInfo.getMetadata().invoke(connectionManager,
                    callInfo.toArray());
        }

        @Override
        protected void failedToEnableOperation(final String operationName,
                                               final Exception e) {
            failedToEnableOperation(getLoggerImpl(), Level.WARNING, operationName, e);
        }

        @Override
        public Collection<JmxOperationInfo> expand() {
            if (globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxOperationInfo>>() {
                    private void fillOperations(final MBeanServerConnection connection,
                                                final ObjectName objectName,
                                                final Collection<JmxOperationInfo> output) throws JMException, IOException {
                        for (final MBeanOperationInfo operationInfo : connection.getMBeanInfo(objectName).getOperations()) {
                            final JmxOperationInfo op = enableOperation(operationInfo.getName(),
                                    AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration.TIMEOUT_FOR_SMART_MODE,
                                    toConfigurationParameters(globalObjectName));
                            if (op != null) output.add(op);
                        }
                    }

                    @Override
                    public LinkedList<JmxOperationInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxOperationInfo> result = new LinkedList<>();
                        fillOperations(connection, globalObjectName, result);
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
        public String getAlias() {
            return getDescriptor().getName(getName());
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
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeName, final Exception e) {
            failedToConnectAttribute(getLoggerImpl(), Level.WARNING, attributeName, e);
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
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(connection -> {
                for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                    if (Objects.equals(attr.getName(), metadata.getName(attributeName))) return attr;
                return null;
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
         * @param attributeName The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute.
         * @throws Exception Internal connector error.
         */
        @Override
        protected JmxAttributeInfo connectAttribute(final String attributeName,
                                                    final AttributeDescriptor descriptor) throws Exception {
            return connectAttribute(attributeName, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
        }

        @Override
        public List<JmxAttributeInfo> expand() {
            if(globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxAttributeInfo>>() {
                    private void fillAttributes(final MBeanServerConnection connection,
                                                final ObjectName objectName,
                                                final Collection<JmxAttributeInfo> output) throws JMException, IOException{
                        for(final MBeanAttributeInfo attributeInfo: connection.getMBeanInfo(objectName).getAttributes()){
                            final JmxAttributeInfo attr = addAttribute(attributeInfo.getName(),
                                    AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.TIMEOUT_FOR_SMART_MODE,
                                    toConfigurationParameters(globalObjectName));
                            if(attr != null) output.add(attr);
                        }
                    }

                    @Override
                    public LinkedList<JmxAttributeInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxAttributeInfo> result = new LinkedList<>();
                        fillAttributes(connection, globalObjectName, result);
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
                                          final BundleContext context,
                                          final JmxConnectionManager connectionManager) {
            super(resourceName, FEATURE_TYPE, DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName)));
            this.connectionManager = connectionManager;
            this.globalObjectName = globalName;
            this.connectionManager.addReconnectionHandler(this);
            listenerInvoker = createListenerInvoker(Executors.newSingleThreadExecutor(new GroupedThreadFactory("notifs-".concat(resourceName))));
        }

        private static NotificationListenerInvoker createListenerInvoker(final Executor executor){
            return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, (e, source) -> getLoggerImpl().log(Level.SEVERE, "Unable to process JMX notification", e));
        }

        /**
         * Determines whether raising of registered events is suspended.
         *
         * @return {@literal true}, if events are suspended; otherwise {@literal false}.
         */
        @Override
        public boolean isSuspended() {
            return super.isSuspended() && !DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
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
         * @param category An event category.
         * @param e        Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToEnableNotifications(final String category, final Exception e) {
            failedToEnableNotifications(getLoggerImpl(), Level.WARNING, category, e);
        }

        private Set<ObjectName> getNotificationTargets() {
            final JmxNotificationInfo[] notifications = getNotificationInfo();
            final Set<ObjectName> targets = Sets.newHashSetWithExpectedSize(notifications.length);
            for(final JmxNotificationInfo info: notifications)
                targets.add(info.getOwner());
            return targets;
        }

        private void enableListening(final ObjectName target) throws Exception {
            connectionManager.handleConnection(connection -> {
                connection.addNotificationListener(target, JmxNotificationRepository.this, null, null);
                return null;
            });
        }

        private void disableListening(final ObjectName target) throws Exception {
            connectionManager.handleConnection(connection -> {
                try {
                    connection.removeNotificationListener(target, JmxNotificationRepository.this);
                } catch (final ListenerNotFoundException ignored) {
                }
                return null;
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

        private JmxNotificationInfo enableNotifications(final String category,
                                                        final NotificationDescriptor metadata,
                                                        final ObjectName owner,
                                                        final boolean useRegexp) throws Exception {
            if (useRegexp)
                return enableNotifications(category, metadata, connectionManager.resolveName(owner), false);
            final JmxNotificationInfo eventData = connectionManager.handleConnection(connection -> {
                for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(owner).getNotifications())
                    for (final String notifType : notificationInfo.getNotifTypes())
                        if (Objects.equals(notifType, metadata.getName(category)))
                            return new JmxNotificationInfo(category,
                                    notificationInfo,
                                    owner,
                                    metadata);
                return null;
            });
            if (eventData != null) {
                //checks whether the enabled MBean object already listening
                final Set<ObjectName> listeningContext = getNotificationTargets();
                if (!listeningContext.contains(eventData.eventOwner))
                    enableListening(eventData.eventOwner);
                return eventData;
            } else throw new IllegalArgumentException(String.format("%s notification is not supported", category));
        }

        private JmxNotificationInfo enableNotifications(final String category,
                                                        final NotificationDescriptor metadata,
                                                        final ObjectName namespace) throws Exception {
            return enableNotifications(category, metadata, namespace, useRegexpOption(metadata));
        }

        @Override
        protected JmxNotificationInfo enableNotifications(final String category,
                                                          final NotificationDescriptor metadata) throws Exception {
            return enableNotifications(category, metadata, globalObjectName == null ? getObjectName(metadata) : globalObjectName);
        }

        @Override
        public List<JmxNotificationInfo> expand() {
            if (globalObjectName == null)
                return Collections.emptyList();
            else try {
                return connectionManager.handleConnection(new MBeanServerConnectionHandler<List<JmxNotificationInfo>>() {
                    private void fillNotifications(final MBeanServerConnection connection,
                                                   final ObjectName objectName,
                                                   final Collection<JmxNotificationInfo> output) throws JMException, IOException {
                        for (final MBeanNotificationInfo notificationInfo : connection.getMBeanInfo(objectName).getNotifications()) {
                            if (notificationInfo.getNotifTypes().length < 1) continue;
                            final JmxNotificationInfo event = enableNotifications(ArrayUtils.getFirst(notificationInfo.getNotifTypes()),
                                    toConfigurationParameters(globalObjectName));
                            if (event != null) output.add(event);
                        }
                    }

                    @Override
                    public LinkedList<JmxNotificationInfo> handle(final MBeanServerConnection connection) throws IOException, JMException {
                        final LinkedList<JmxNotificationInfo> result = new LinkedList<>();
                        fillNotifications(connection, globalObjectName, result);
                        return result;
                    }
                });
            } catch (final Exception e) {
                failedToExpand(getLoggerImpl(), Level.WARNING, e);
                return Collections.emptyList();
            }
        }

        @Override
        public Void handle(final MBeanServerConnection connection) throws IOException, JMException {
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

        @Override
        public void close() {
            super.close();
            connectionManager.removeReconnectionHandler(this);
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
                    AttributeDescriptor::getOpenType);
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

        @Override
        public final String getAlias(){
            return getDescriptor().getName(getName());
        }

        private static Object getValue(final JmxConnectionManager connectionManager,
                                       final String attributeName,
                                       final ObjectName owner) throws Exception{
            return connectionManager.handleConnection(connection -> connection.getAttribute(owner, attributeName));
        }

        private Object getValue(final JmxConnectionManager connectionManager) throws Exception {
            return getValue(connectionManager, getAlias(), namespace);
        }

        private static void setValue(final JmxConnectionManager connectionManager,
                                     final String attributeName,
                                     final ObjectName owner,
                                     final Object value) throws Exception{
            connectionManager.handleConnection(connection -> {
                connection.setAttribute(owner, new Attribute(attributeName, value));
                return null;
            });
        }

        private void setValue(final JmxConnectionManager connectionManager, final Object value) throws Exception {
            setValue(connectionManager, getAlias(), namespace, value);
        }
    }
    @Aggregation
    private final JmxNotificationRepository notifications;
    @Aggregation
    private final JmxAttributeRepository attributes;
    @Aggregation
    private final JmxConnectionManager connectionManager;
    @Aggregation
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
                        JmxConnectorDescriptionProvider.OBJECT_NAME_PROPERTY));
        }
        else smartMode = false;
        this.notifications = new JmxNotificationRepository(resourceName,
                connectionOptions.getGlobalObjectName(),
                Utils.getBundleContextOfObject(this),
                connectionManager);
        this.attributes = new JmxAttributeRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
        this.operations = new JmxOperationRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
    }

    JmxConnector(final String resourceName,
                 final String connectionString,
                 final Map<String, String> connectionOptions) throws MalformedURLException, MalformedObjectNameException {
        this(resourceName, new JmxConnectionOptions(connectionString, connectionOptions));
    }

    @Override
    protected MetricsReader createMetricsReader(){
        return assembleMetricsReader(attributes, notifications, operations);
    }

    boolean addAttribute(final String attributeName,
                                    final TimeSpan readWriteTimeout,
                                    final CompositeData options) {
        verifyClosedState();
        return attributes.addAttribute(attributeName, readWriteTimeout, options) != null;
    }

    boolean enableNotifications(final String category,
                                              final CompositeData options) {
        verifyClosedState();
        return notifications.enableNotifications(category, options) != null;
    }

    boolean enableOperation(final String operationName,
                                       final TimeSpan invocationTimeout,
                                       final CompositeData options){
        verifyClosedState();
        return operations.enableOperation(operationName, invocationTimeout, options) != null;
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
        verifyClosedState();
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
        verifyClosedState();
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
        removeResourceEventListener(listener, attributes, notifications, operations);
    }

    /**
     * Determines whether raising of registered events is suspended.
     *
     * @return {@literal true}, if events are suspended; otherwise {@literal false}.
     */
    @Override
    public boolean isSuspended() {
        return notifications.isSuspended();
    }

    /**
     * Suspends or activate raising of events.
     *
     * @param value {@literal true} to suspend events; {@literal false}, to activate events.
     */
    @Override
    public void setSuspended(final boolean value) {
        notifications.setSuspended(value);
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
        this.attributes.retainAll(attributes);
    }

    void disableNotificationsExcept(final Set<String> events) {
        this.notifications.retainAll(events);
    }

    void disableOperationsExcept(final Set<String> operations) {
        this.operations.retainAll(operations);
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
    public void close() throws Exception{
        attributes.close();
        notifications.unsubscribeAll();
        notifications.close();
        operations.close();
        super.close();
        connectionManager.close();
    }
}