package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeDescriptorRead;
import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationDescriptorRead;
import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationDescriptorRead;
import com.bytex.snamp.core.LoggerProvider;

import javax.annotation.Nonnull;
import javax.management.*;
import javax.management.openmbean.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.connector.jmx.JmxConnectorDescriptionProvider.*;
import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector implements HealthCheckSupport {
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
        String getType();
    }

    private interface JmxNotificationMetadata extends JmxFeatureMetadata, NotificationDescriptorRead{
        String[] getNotifTypes();
    }

    private interface JmxOperationMetadata extends JmxFeatureMetadata, OperationDescriptorRead{
        String getReturnType();
        MBeanParameterInfo[] getSignature();
        int getImpact();
    }

    private static abstract class JmxOperationInfo extends MBeanOperationInfo implements JmxOperationMetadata {
        private static final long serialVersionUID = 3762430398491875048L;
        private final OperationDescriptor descriptor;

        private JmxOperationInfo(final String name,
                                final String description,
                                final MBeanParameterInfo[] signature,
                                final String type,
                                final int impact,
                                final OperationDescriptor descriptor) {
            super(name, descriptor.getDescription(description), signature, type, impact, descriptor);
            this.descriptor = descriptor;
        }

        @Override
        public final String getAlias(){
            return OperationDescriptor.getName(this);
        }

        @Override
        public final OperationDescriptor getDescriptor() {
            return firstNonNull(descriptor, OperationDescriptor.EMPTY_DESCRIPTOR);
        }

        abstract Object invoke(final JmxConnectionManager connectionManager, final Object[] arguments) throws Exception;
    }

    private static final class JmxSimulateConnectionAbort extends JmxOperationInfo {
        private static final String NAME = "simulateConnectionAbort";
        private static final long serialVersionUID = -99848306646362293L;

        private JmxSimulateConnectionAbort(final String name, final OperationDescriptor descriptor) {
            super(name, "Simulates JMX connection abort", new MBeanParameterInfo[0], SimpleType.VOID.getClassName(), ACTION, descriptor);
        }

        @Override
        public ObjectName getOwner() {
            return null;
        }

        @Override
        Void invoke(final JmxConnectionManager connectionManager, final Object[] arguments) throws IOException, InterruptedException {
            connectionManager.simulateConnectionAbort();
            return null;
        }
    }

    private static final class JmxProxyOperation extends JmxOperationInfo {
        private static final long serialVersionUID = -2143203631423581065L;
        private final ObjectName operationOwner;

        private JmxProxyOperation(final String operationID,
                                 final MBeanOperationInfo nativeOp,
                                 final ObjectName owner,
                                 OperationDescriptor descriptor) {
            super(operationID,
                    nativeOp.getDescription(),
                    nativeOp.getSignature(),
                    nativeOp.getReturnType(),
                    nativeOp.getImpact(),
                    descriptor.setFields(nativeOp.getDescriptor()));
            this.operationOwner = owner;
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
        Object invoke(final JmxConnectionManager connectionManager,
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

        private JmxOperationRepository(final String resourceName,
                                       final ObjectName globalName,
                                       final JmxConnectionManager connectionManager){
            super(resourceName, JmxOperationInfo.class);
            this.globalObjectName = globalName;
            this.connectionManager = connectionManager;
        }

        private static JmxOperationInfo connectOperation(final JmxConnectionManager connectionManager,
                                                         final String operationName,
                                                         final OperationDescriptor descriptor,
                                                         final ObjectName owner,
                                                         final boolean useRegexp) throws Exception{
            if(useRegexp)
                return connectOperation(connectionManager, operationName, descriptor, connectionManager.resolveName(owner), false);
            final MBeanOperationInfo metadata = connectionManager.handleConnection(connection -> {
                for(final MBeanOperationInfo candidate: connection.getMBeanInfo(owner).getOperations())
                    if(Objects.equals(descriptor.getAlternativeName().orElse(operationName), candidate.getName()) && checkSignature(descriptor, candidate.getSignature()))
                        return candidate;
                return null;
            });
            if(metadata != null)
                return new JmxProxyOperation(operationName, metadata, owner, descriptor);
            else throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist in '%s' object", descriptor.getAlternativeName().orElse(operationName), owner)));

        }

        private JmxOperationInfo connectOperation(final String userDefinedName,
                                                  final OperationDescriptor descriptor,
                                                  final ObjectName owner,
                                                  final boolean useRegexp) throws Exception {
            return connectOperation(connectionManager, userDefinedName, descriptor, owner, useRegexp);
        }

        private JmxOperationInfo connectOperation(final String userDefinedName,
                                                  final OperationDescriptor descriptor,
                                                  final ObjectName owner) throws Exception{
            return connectOperation(userDefinedName, descriptor, owner, useRegexpOption(descriptor));
        }

        @Override
        protected JmxOperationInfo connectOperation(final String userDefinedName,
                                                    final OperationDescriptor descriptor) throws Exception {
            switch (descriptor.getAlternativeName().orElse(userDefinedName)){
                case JmxSimulateConnectionAbort.NAME:
                    return new JmxSimulateConnectionAbort(userDefinedName, descriptor);
                default:
                    return connectOperation(userDefinedName, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
            }
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
            return callInfo.getOperation().invoke(connectionManager,
                    callInfo.toArray());
        }

        private Map<String, OperationDescriptor> discoverOperations(final MBeanServerConnection connection,
                                                                    final ObjectName objectName) throws JMException, IOException {
            final Map<String, OperationDescriptor> result = new HashMap<>();
            for (final MBeanOperationInfo sourceOperation : connection.getMBeanInfo(objectName).getOperations()) {
                final OperationDescriptor descriptor = createDescriptor(config -> config.put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName()));
                result.put(sourceOperation.getName(), descriptor);
            }
            return result;
        }

        private Map<String, OperationDescriptor> discoverOperations(final MBeanServerConnection connection) throws IOException, JMException{
            final Map<String, OperationDescriptor> result = new HashMap<>();
            for(final ObjectName objectName: connection.queryNames(null, null))
                result.putAll(discoverOperations(connection, objectName));
            return result;
        }

        @Override
        public Map<String, OperationDescriptor> discoverOperations() {
            try {
                return globalObjectName == null ?
                        connectionManager.handleConnection(this::discoverOperations) :
                        connectionManager.handleConnection(connection -> discoverOperations(connection, globalObjectName));
            } catch (final Exception e) {
                failedToExpand(Level.WARNING, e);
            }
            return Collections.emptyMap();
        }
    }

    private final static class JmxNotificationInfo extends AbstractNotificationInfo implements JmxNotificationMetadata{
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
                    descriptor.getDescription(nativeNotif.getDescription()),
                    descriptor.setFields(nativeNotif.getDescriptor()));
            this.eventOwner = eventOwner;
        }

        @Override
        public String getAlias() {
            return NotificationDescriptor.getName(this);
        }

        @Override
        public ObjectName getOwner(){
            return eventOwner;
        }
    }

    private static final class JmxAttributeRepository extends AbstractAttributeRepository<JmxAttributeInfo> {
        private final JmxConnectionManager connectionManager;
        private final ObjectName globalObjectName;

        private JmxAttributeRepository(final String resourceName,
                                       final ObjectName globalName,
                                       final JmxConnectionManager connectionManager){
            super(resourceName, JmxAttributeInfo.class);
            this.globalObjectName = globalName;
            this.connectionManager = connectionManager;
        }

        private static JmxAttributeInfo createPlainAttribute(final JmxConnectionManager connectionManager,
                                                             final ObjectName namespace,
                                                      final String attributeName,
                                                      final AttributeDescriptor metadata) throws Exception{
            //extracts JMX attribute metadata
            final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(connection -> {
                for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                    if (Objects.equals(attr.getName(), metadata.getAlternativeName().orElse(attributeName))) return attr;
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

        private Map<String, AttributeDescriptor> discoverAttributes(final MBeanServerConnection connection,
                                                                    final ObjectName objectName) throws IOException, JMException {
            final Map<String, AttributeDescriptor> result = new HashMap<>();
            for (final MBeanAttributeInfo sourceAttribute : connection.getMBeanInfo(objectName).getAttributes()) {
                final AttributeDescriptor descriptor = createDescriptor(config -> config.put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName()));
                result.put(sourceAttribute.getName(), descriptor);
            }
            return result;
        }

        private Map<String, AttributeDescriptor> discoverAttributes(final MBeanServerConnection connection) throws IOException, JMException {
            final Map<String, AttributeDescriptor> result = new HashMap<>();
            for (final ObjectName objectName : connection.queryNames(null, null))
                result.putAll(discoverAttributes(connection, objectName));
            return result;
        }

        @Override
        public Map<String, AttributeDescriptor> discoverAttributes() {
            try {
                return globalObjectName == null ?
                        connectionManager.handleConnection(this::discoverAttributes) :
                        connectionManager.handleConnection(connection -> discoverAttributes(connection, globalObjectName));
            } catch (final Exception e) {
                failedToExpand(Level.WARNING, e);
            }
            return Collections.emptyMap();
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

    private static final class JmxNotificationRepository extends AbstractNotificationRepository<JmxNotificationInfo> implements NotificationListener, ConnectionEstablishedEventHandler {
        private final JmxConnectionManager connectionManager;
        private final ExecutorService listenerInvoker;
        private final ObjectName globalObjectName;

        private JmxNotificationRepository(final String resourceName,
                                          final ObjectName globalName,
                                          final JmxConnectionManager connectionManager,
                                          final ExecutorService threadPool) {
            super(resourceName, JmxNotificationInfo.class);
            this.connectionManager = connectionManager;
            this.globalObjectName = globalName;
            this.connectionManager.addReconnectionHandler(this);
            listenerInvoker = threadPool;
        }

        /**
         * Gets an executor used to execute event listeners.
         *
         * @return Executor service.
         */
        @Override
        @Nonnull
        protected ExecutorService getListenerExecutor() {
            return listenerInvoker;
        }

        private Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        private Set<ObjectName> getNotificationTargets() {
            return Arrays.stream(getNotificationInfo())
                    .map(JmxNotificationInfo::getOwner)
                    .collect(Collectors.toSet());
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
        protected void disconnectNotifications(final JmxNotificationInfo metadata) {
            final Set<ObjectName> targets = getNotificationTargets();
            if (!targets.contains(metadata.getOwner()))
                try {
                    disableListening(metadata.getOwner());
                } catch (final Exception e) {
                    getLogger().log(Level.WARNING, String.format("Unable to unsubscribe from %s", metadata.getOwner()), e);
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
                        if (Objects.equals(notifType, metadata.getAlternativeName().orElse(category)))
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
        protected JmxNotificationInfo connectNotifications(final String category,
                                                          final NotificationDescriptor metadata) throws Exception {
            return enableNotifications(category, metadata, globalObjectName == null ? getObjectName(metadata) : globalObjectName);
        }

        private Map<String, NotificationDescriptor> discoverNotifications(final MBeanServerConnection connection,
                                                                          final ObjectName objectName) throws IOException, JMException {
            final Map<String, NotificationDescriptor> result = new HashMap<>();
            for (final MBeanNotificationInfo sourceNotification : connection.getMBeanInfo(objectName).getNotifications())
                for (final String notifType : sourceNotification.getNotifTypes()) {
                    final NotificationDescriptor descriptor = createDescriptor(config -> config.put(OBJECT_NAME_PROPERTY, objectName.getCanonicalName()));
                    result.put(notifType, descriptor);
                }
            return result;
        }

        private Map<String, NotificationDescriptor> discoverNotifications(final MBeanServerConnection connection) throws IOException, JMException {
            final Map<String, NotificationDescriptor> result = new HashMap<>();
            for (final ObjectName objectName : connection.queryNames(null, null))
                result.putAll(discoverNotifications(connection, objectName));
            return result;
        }

        @Override
        public Map<String, NotificationDescriptor> discoverNotifications() {
            try {
                return globalObjectName == null ?
                        connectionManager.handleConnection(this::discoverNotifications) :
                        connectionManager.handleConnection(connection -> discoverNotifications(connection, globalObjectName));
            } catch (final Exception e) {
                failedToExpand(Level.WARNING, e);
            }
            return Collections.emptyMap();
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
            try {
                notification.setUserData(UserDataExtractor.getUserData(notification));
            } catch (final OpenDataException | IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to process user data %s in notification %s",
                        notification.getUserData(),
                        notification.getType()), e);
            }
            fire(notification, false);
        }

        private void unsubscribeAll() throws Exception {
            for (final ObjectName target : getNotificationTargets())
                disableListening(target);
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
    private static final class JmxAttributeInfo extends OpenMBeanAttributeInfoSupport implements JmxAttributeMetadata {
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
                    metadata.getDescription(nativeAttr.getDescription()),
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
            return firstNonNull(descriptor, AttributeDescriptor.EMPTY_DESCRIPTOR);
        }

        /**
         * Returns the attribute owner.
         * @return An owner of this attribute.
         */
        @Override
        public ObjectName getOwner(){
            return namespace;
        }

        @Override
        public String getAlias(){
            return AttributeDescriptor.getName(this);
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
    @Aggregation(cached = true)
    private final JmxNotificationRepository notifications;
    @Aggregation(cached = true)
    private final JmxAttributeRepository attributes;
    @Aggregation(cached = true)
    private final JmxConnectionManager connectionManager;
    @Aggregation(cached = true)
    private final JmxOperationRepository operations;
    private final String resourceName;
    private final boolean canExpand;

    private JmxConnector(final String resourceName,
                 final JmxConnectionOptions connectionOptions) {
        this.resourceName = resourceName;
        this.connectionManager = connectionOptions.createConnectionManager(getLogger());
        this.notifications = new JmxNotificationRepository(resourceName,
                connectionOptions.getGlobalObjectName(),
                connectionManager,
                connectionOptions.getThreadPool());
        this.notifications.setSource(this);
        canExpand = connectionOptions.getGlobalObjectName() != null;
        this.attributes = new JmxAttributeRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
        this.operations = new JmxOperationRepository(resourceName, connectionOptions.getGlobalObjectName(), connectionManager);
    }

    JmxConnector(final String resourceName,
                 final ManagedResourceInfo configuration) throws MalformedURLException, MalformedObjectNameException {
        this(resourceName, new JmxConnectionOptions(configuration.getConnectionString(), configuration));
        setConfiguration(configuration);
    }

    void init() throws IOException {
        connectionManager.connect();
    }

    /**
     * Determines whether the connected managed resource is alive.
     *
     * @return Status of the remove managed resource.
     */
    @Override
    @Nonnull
    public HealthStatus getStatus() {
        try {
            return connectionManager.handleConnection(connection -> {
                connection.getMBeanCount();      //call method using RMI
                return new OkStatus();
            });
        } catch (final InterruptedException e) {
            return new ResourceConnectorMalfunction(new ReflectionException(e));
        } catch (final JMException e) {
            return new ResourceConnectorMalfunction(e);
        } catch (final IOException e) {
            return new ConnectionProblem(e);
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    protected MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes, notifications, operations);
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

    @Override
    public Collection<? extends MBeanFeatureInfo> expandAll() {
        if (canExpand)
            return super.expandAll();
        else {
            getLogger().warning(String.format("Cannot expand resource %s because global MBean name is not specified", resourceName));
            return Collections.emptyList();
        }
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