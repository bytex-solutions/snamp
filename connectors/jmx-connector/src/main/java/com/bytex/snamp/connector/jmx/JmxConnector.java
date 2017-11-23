package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.health.ConnectionProblem;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.connector.health.ResourceConnectorMalfunction;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.notifications.NotificationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.connector.operations.OperationRepository;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import javax.annotation.Nonnull;
import javax.management.*;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connector.jmx.JmxConnectorDescriptionProvider.*;

/**
 * Represents JMX connector.
 * @author Roman Sakno
 */
final class JmxConnector extends AbstractManagedResourceConnector implements
        AttributeManager,
        AttributeRepository.AttributeReader<JmxAttributeInfo>,
        AttributeRepository.AttributeWriter<JmxAttributeInfo>,
        OperationManager,
        OperationRepository.OperationInvoker<JmxOperationInfo>,
        NotificationManager,
        NotificationBroadcaster,
        NotificationListener,
        ConnectionEstablishedEventHandler{

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

    private static final class JmxNotificationRepository extends NotificationRepository<JmxNotificationInfo> {
        private static final long serialVersionUID = 8002543577613173192L;
        private final Multiset<ObjectName> listeningContext;
        private transient final Logger logger;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public JmxNotificationRepository() {
            listeningContext = HashMultiset.create();
            logger = LoggerProvider.getLoggerForObject(this);
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeInt(listeningContext.size());
            for(final ObjectName target: listeningContext)
                out.writeObject(target);
            super.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            for (int size = in.readInt(); size > 0; size--)
                listeningContext.add((ObjectName) in.readObject());
            super.readExternal(in);
        }

        //not thread safe
        private void enableListening(final ObjectName target, final JmxConnectionManager connectionManager, final NotificationListener listener) throws Exception {
            if (listeningContext.count(target) == 0)
                connectionManager.handleConnection(connection -> {
                    connection.addNotificationListener(target, listener, null, null);
                    return null;
                });
            listeningContext.add(target);
        }

        void enableListening(final JmxNotificationMetadata metadata, final JmxConnectionManager connectionManager, final NotificationListener listener) throws Exception{
            enableListening(metadata.getOwner(), connectionManager, listener);
        }

        void enableListening(final MBeanServerConnection connection, final NotificationListener listener) throws IOException, InstanceNotFoundException {
            //for each MBean object assigns notification listener
            for (final ObjectName target : listeningContext.elementSet())
                connection.addNotificationListener(target, listener, null, null);
        }

        private void disableListening(final ObjectName target, final JmxConnectionManager connectionManager, final NotificationListener listener) {
            if (listeningContext.count(target) > 0)
                try {
                    connectionManager.handleConnection(connection -> {
                        connection.removeNotificationListener(target, listener);
                        return null;
                    });
                } catch (final Exception e) {
                    logger.log(Level.WARNING, "Failed to unsubscribe from events provided by object " + target, e);
                } finally {
                    listeningContext.remove(target);
                }
        }

        void disableListening(final JmxNotificationMetadata metadata,
                              final JmxConnectionManager connectionManager,
                              final NotificationListener listener) {
            disableListening(metadata.getOwner(), connectionManager, listener);
        }
    }

    private final ObjectName globalObjectName;
    private final JmxNotificationRepository notifications;
    private final AttributeRepository<JmxAttributeInfo> attributes;
    @Aggregation(cached = true)
    private final JmxConnectionManager connectionManager;
    private final OperationRepository<JmxOperationInfo> operations;
    private final ExecutorService threadPool;

    JmxConnector(final String resourceName,
                 final JmxConnectionOptions connectionOptions) {
        super(resourceName);
        this.connectionManager = connectionOptions.createConnectionManager();
        this.globalObjectName = connectionOptions.getGlobalObjectName();
        this.threadPool = connectionOptions.getThreadPool();
        this.notifications = new JmxNotificationRepository();
        this.attributes = new AttributeRepository<>();
        this.operations = new OperationRepository<>();
    }

    void init() throws IOException {
        connectionManager.connect();
        connectionManager.addReconnectionHandler(this);
    }

    @Override
    public void connectionEstablished(final MBeanServerConnection connection) throws IOException, JMException {
        notifications.enableListening(connection, this);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(this);
        try {
            notification.setUserData(UserDataExtractor.getUserData(notification));
        } catch (final OpenDataException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, String.format("Unable to process user data %s in notification %s",
                    notification.getUserData(),
                    notification.getType()), e);
        }
        notifications.emitSingle(notification.getType(), metadata -> notification);
    }

    /**
     * Disables all notifications except specified in the collection.
     *
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
    @Override
    public void retainNotifications(final Set<String> events) {
        retainFeatures(notifications, events);
    }

    @Override
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
        notifications.addNotificationListener(listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        notifications.removeNotificationListener(listener);
    }

    private static Map<String, NotificationDescriptor> discoverNotifications(final MBeanServerConnection connection,
                                                                       final ObjectName objectName) throws IOException, JMException {
        final Map<String, NotificationDescriptor> result = new HashMap<>();
        for (final MBeanNotificationInfo sourceNotification : connection.getMBeanInfo(objectName).getNotifications())
            for (final String category : sourceNotification.getNotifTypes()) {
                final NotificationDescriptor descriptor = new NotificationDescriptor(OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
                result.put(category, descriptor);
            }
        return result;
    }

    private static Map<String, NotificationDescriptor> discoverNotifications(final MBeanServerConnection connection) throws IOException, JMException {
        final Map<String, NotificationDescriptor> result = new HashMap<>();
        for(final ObjectName target: connection.queryNames(null, null))
            result.putAll(discoverNotifications(connection, target));
        return result;
    }

    /**
     * Discover notifications.
     *
     * @return A map of discovered notifications that can be enabled using method {@link #enableNotifications(String, NotificationDescriptor)}.
     * @since 2.0
     */
    @Override
    public Map<String, NotificationDescriptor> discoverNotifications() {
        try {
            return globalObjectName == null ?
                    connectionManager.handleConnection(JmxConnector::discoverNotifications) :
                    connectionManager.handleConnection(connection -> discoverNotifications(connection, globalObjectName));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Unable to discover notifications", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Gets an array of supported notifications.
     *
     * @return An array of supported notifications.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifications.getNotificationInfo();
    }

    private JmxNotificationInfo createNotification(final String category,
                                                   final NotificationDescriptor metadata,
                                                   final ObjectName owner,
                                                   final boolean useRegexp) throws Exception {
        if (useRegexp)
            return createNotification(category, metadata, connectionManager.resolveName(owner), false);
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
        if (eventData == null)
            throw new IllegalArgumentException(String.format("%s notification is not supported", category));
        else {
            notifications.enableListening(eventData, connectionManager, this);
            return eventData;
        }
    }

    private JmxNotificationInfo createNotification(final String category,
                                                    final NotificationDescriptor metadata,
                                                    final ObjectName namespace) throws Exception {
        return createNotification(category, metadata, namespace, useRegexpOption(metadata));
    }

    @Nonnull
    private JmxNotificationInfo createNotification(final String category, final NotificationDescriptor descriptor) throws Exception {
        return createNotification(category, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
    }

    /**
     * Enables managed resource notification.
     *
     * @param category   The notification category.
     * @param descriptor The notification configuration options.
     * @since 2.0
     */
    @Override
    public void enableNotifications(final String category, final NotificationDescriptor descriptor) throws JMException {
        addFeature(notifications, category, descriptor, this::createNotification);
    }

    @Override
    protected void removedFeature(final MBeanFeatureInfo feature) {
        if (feature instanceof JmxNotificationMetadata)
            notifications.disableListening((JmxNotificationMetadata) feature, connectionManager, this);
    }

    /**
     * Disables notifications of the specified category.
     *
     * @param category Category of notifications to disable.
     * @return {@literal true}, if notification is disabled successfully
     * @since 2.0
     */
    @Override
    public boolean disableNotifications(final String category) {
        return removeFeature(notifications, category);
    }

    @Nonnull
    private static JmxAttributeInfo createPlainAttribute(final JmxConnectionManager connectionManager,
                                                         final ObjectName namespace,
                                                         final String attributeName,
                                                         final AttributeDescriptor metadata) throws JMException, IOException, InterruptedException {
        //extracts JMX attribute metadata
        final MBeanAttributeInfo targetAttr = connectionManager.handleConnection(connection -> {
            for (final MBeanAttributeInfo attr : connection.getMBeanInfo(namespace).getAttributes())
                if (Objects.equals(attr.getName(), metadata.getAlternativeName().orElse(attributeName))) return attr;
            return null;
        });
        if (targetAttr == null)
            throw JMExceptionUtils.attributeNotFound(attributeName);
        else
            return new JmxAttributeInfo(attributeName, targetAttr, namespace, metadata);
    }

    @Nonnull
    private static JmxAttributeInfo createAttribute(final JmxConnectionManager connectionManager,
                                             final String attributeName,
                                              final AttributeDescriptor metadata,
                                              final boolean useRegexp,
                                              final ObjectName namespace) throws JMException, IOException, InterruptedException {
        //creates JMX attribute provider based on its metadata and connection options.
        if (namespace == null)
            throw new IllegalArgumentException("JMX ObjectName is not specified");
        else if (useRegexp)
            return createAttribute(connectionManager, attributeName, metadata, false, connectionManager.resolveName(namespace));
        else
            return createPlainAttribute(connectionManager, namespace, attributeName, metadata);
    }

    @Nonnull
    private JmxAttributeInfo createAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        return createAttribute(connectionManager,
                attributeName,
                descriptor,
                useRegexpOption(descriptor),
                globalObjectName == null ? getObjectName(descriptor) : globalObjectName);
    }

    @Override
    public void addAttribute(final String attributeName, final AttributeDescriptor descriptor) throws JMException {
        addFeature(attributes, attributeName, descriptor, this::createAttribute);
    }

    @Override
    public boolean removeAttribute(final String attributeName) {
        return removeFeature(attributes, attributeName);
    }

    @Override
    public void retainAttributes(final Set<String> attributes) {
        retainFeatures(this.attributes, attributes);
    }

    @Override
    public Object getAttributeValue(final JmxAttributeInfo attribute) throws Exception {
        return attribute.getValue(connectionManager);
    }

    @Override
    public void setAttributeValue(final JmxAttributeInfo attribute, final Object value) throws Exception {
        attribute.setValue(connectionManager, value);
    }

    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return attributes.getAttribute(attributeName, this);
    }

    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        attributes.setAttribute(attribute, this);
    }

    @Override
    public AttributeList getAttributes() throws MBeanException, ReflectionException {
        return attributes.getAttributes(this, threadPool, null);
    }

    @Override
    public JmxAttributeInfo[] getAttributeInfo() {
        return getFeatureInfo(attributes, JmxAttributeInfo.class);
    }

    private static Map<String, AttributeDescriptor> discoverAttributes(final MBeanServerConnection connection,
                                                                final ObjectName objectName) throws IOException, JMException {
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        for (final MBeanAttributeInfo sourceAttribute : connection.getMBeanInfo(objectName).getAttributes()) {
            final AttributeDescriptor descriptor = new AttributeDescriptor(Duration.ofSeconds(10), OBJECT_NAME_PROPERTY, objectName.getCanonicalName());
            result.put(sourceAttribute.getName(), descriptor);
        }
        return result;
    }

    private static Map<String, AttributeDescriptor> discoverAttributes(final MBeanServerConnection connection) throws IOException, JMException {
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        for (final ObjectName objectName : connection.queryNames(null, null))
            result.putAll(discoverAttributes(connection, objectName));
        return result;
    }

    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        try {
            return globalObjectName == null ?
                    connectionManager.handleConnection(JmxConnector::discoverAttributes) :
                    connectionManager.handleConnection(connection -> discoverAttributes(connection, globalObjectName));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Unable to discover attributes", e);
        }
        return Collections.emptyMap();
    }

    private static JmxOperationInfo createOperation(final JmxConnectionManager connectionManager,
                                                     final String operationName,
                                                     final OperationDescriptor descriptor,
                                                     final ObjectName owner,
                                                     final boolean useRegexp) throws Exception{
        if(useRegexp)
            return createOperation(connectionManager, operationName, descriptor, connectionManager.resolveName(owner), false);
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

    @Nonnull
    private JmxOperationInfo createOperation(final String userDefinedName,
                                                final OperationDescriptor descriptor) throws Exception {
        switch (descriptor.getAlternativeName().orElse(userDefinedName)){
            case JmxSimulateConnectionAbort.NAME:
                return new JmxSimulateConnectionAbort(userDefinedName, descriptor);
            default:
                return createOperation(connectionManager, userDefinedName, descriptor, globalObjectName == null ? getObjectName(descriptor) : globalObjectName, useRegexpOption(descriptor));
        }
    }

    @Override
    public void enableOperation(final String operationName, final OperationDescriptor descriptor) throws JMException {
        addFeature(operations, operationName, descriptor, this::createOperation);
    }

    @Override
    public boolean disableOperation(final String operationName) {
        return removeFeature(operations, operationName);
    }

    @Override
    public void retainOperations(final Set<String> operations) {
        retainFeatures(this.operations, operations);
    }

    @Override
    public Object invokeOperation(final OperationRepository.OperationCallInfo<JmxOperationInfo> callInfo) throws Exception {
        return callInfo.getOperation().invoke(connectionManager, callInfo.toArray());
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        return operations.invoke(actionName, params, signature, this);
    }

    @Override
    public JmxOperationInfo[] getOperationInfo() {
        return getFeatureInfo(operations, JmxOperationInfo.class);
    }

    private static Map<String, OperationDescriptor> discoverOperations(final MBeanServerConnection connection,
                                                                final ObjectName objectName) throws JMException, IOException {
        final Map<String, OperationDescriptor> result = new HashMap<>();
        for (final MBeanOperationInfo sourceOperation : connection.getMBeanInfo(objectName).getOperations()) {
            result.put(sourceOperation.getName(), new OperationDescriptor(Duration.ofSeconds(10), OBJECT_NAME_PROPERTY, objectName.getCanonicalName()));
        }
        return result;
    }

    private static Map<String, OperationDescriptor> discoverOperations(final MBeanServerConnection connection) throws IOException, JMException{
        final Map<String, OperationDescriptor> result = new HashMap<>();
        for(final ObjectName objectName: connection.queryNames(null, null))
            result.putAll(discoverOperations(connection, objectName));
        return result;
    }

    @Override
    public Map<String, OperationDescriptor> discoverOperations() {
        try {
            return globalObjectName == null ?
                    connectionManager.handleConnection(JmxConnector::discoverOperations) :
                    connectionManager.handleConnection(connection -> discoverOperations(connection, globalObjectName));
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Unable to discover operations", e);
        }
        return Collections.emptyMap();
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

    @Override
    protected MetricsSupport createMetricsReader(){
        return assembleMetricsReader(attributes.metrics, notifications.metrics, operations.metrics);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws java.lang.Exception Some I/O error occurs.
     */
    @Override
    public void close() throws Exception {
        connectionManager.removeReconnectionHandler(this);
        removeFeatures(attributes);
        removeFeatures(operations);
        removeFeatures(notifications);
        Utils.closeAll(super::close, connectionManager);
    }
}