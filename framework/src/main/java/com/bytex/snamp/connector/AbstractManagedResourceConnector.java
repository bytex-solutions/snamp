package com.bytex.snamp.connector;

import com.bytex.snamp.*;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.attributes.AttributeRepository;
import com.bytex.snamp.connector.metrics.ImmutableMetrics;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.notifications.NotificationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.connector.operations.OperationRepository;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.*;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.Action.fromAcceptor;
import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.Action.fromConsumer;

/**
 * Represents an abstract class for building custom management connector.
 * <p>
 *     This class provides a base support for the following management mechanisms:
 *     <ul>
 *         <li>{@link AttributeManager} for resource management using attributes.</li>
 *         <li>{@link NotificationManager} to receive management notifications from the managed resource.</li>
 *         <li>{@link OperationManager} for resource management using operations.</li>
 *     </ul>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public abstract class AbstractManagedResourceConnector extends AbstractAggregator implements ManagedResourceConnector, Localizable {
    /**
     * Factory of resource feature.
     * @param <F> Type of feature to be produced.
     * @param <I> Type of additional input argument used for construction of feature.
     * @since 2.1
     */
    @FunctionalInterface
    protected interface FeatureFactory<I, F extends MBeanFeatureInfo>{
        @Nonnull
        F createFeature(final String featureName, final I input) throws Exception;
    }

    private static final class AttributeSupport<F extends MBeanAttributeInfo> extends AttributeRepository<F> {
        private static final long serialVersionUID = -6411098770607230891L;
        private FeatureFactory<? super AttributeDescriptor, ? extends F> attributeFactory;
        private AttributeReader<? super F> attributeReader;
        private AttributeWriter<? super F> attributeWriter;
        private ExecutorService executor;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public AttributeSupport() {
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeObject(attributeFactory);
            out.writeObject(attributeReader);
            out.writeObject(attributeWriter);
            out.writeObject(executor);
            super.writeExternal(out);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            attributeFactory = (FeatureFactory<AttributeDescriptor, F>) in.readObject();
            attributeReader = (AttributeReader<F>) in.readObject();
            attributeWriter = (AttributeWriter<F>) in.readObject();
            executor = (ExecutorService) in.readObject();
            super.readExternal(in);
        }

        AttributeManager createManager(@Nonnull final AbstractManagedResourceConnector owner) {
            final class DefaultAttributeManager implements AttributeManager {
                @Override
                public void addAttribute(final String attributeName, final AttributeDescriptor descriptor) throws JMException {
                    owner.addFeature(AttributeSupport.this, attributeName, descriptor, attributeFactory);
                }

                @Override
                public boolean removeAttribute(final String attributeName) {
                    return owner.removeFeature(AttributeSupport.this, attributeName);
                }

                @Override
                public void retainAttributes(final Set<String> attributes) {
                    owner.retainFeatures(AttributeSupport.this, attributes);
                }

                @Override
                public Map<String, AttributeDescriptor> discoverAttributes() {
                    return owner.discoverAttributes();
                }

                private boolean equalsRepository(final FeatureRepository<?> repository) {
                    return AttributeSupport.this.equals(repository);
                }

                private boolean equalsConnector(final ManagedResourceConnector connector) {
                    return owner.equals(connector);
                }

                private boolean equals(final DefaultAttributeManager other) {
                    return other.equalsConnector(owner) && other.equalsRepository(AttributeSupport.this);
                }

                @Override
                public boolean equals(final Object other) {
                    return getClass().isInstance(other) && equals((DefaultAttributeManager) other);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(owner, AttributeSupport.this);
                }

                @Override
                public String toString() {
                    return owner.toString();
                }
            }
            return new DefaultAttributeManager();
        }

        Object getAttribute(final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
            if (attributeReader == null)
                throw JMExceptionUtils.unreadableAttribute(attributeName, MBeanException::new);
            else
                return getAttribute(attributeName, attributeReader);
        }

        void setAttribute(final Attribute attribute) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            if (attributeWriter == null)
                throw JMExceptionUtils.unwritableAttribute(attribute.getName(), MBeanException::new);
            else
                setAttribute(attribute, attributeWriter);
        }

        AttributeList getAttributes() throws MBeanException, ReflectionException {
            if (attributeReader == null)
                return new AttributeList();
            else if (executor == null)
                return getAttributes(attributeReader);
            else
                return getAttributes(attributeReader, executor, null);
        }
    }

    /**
     * Notification emitter that can be used to emit notifications.
     * @param <F> Type of notifications in the repository.
     * @since 2.1
     */
    protected interface NotificationEmitter<F extends MBeanNotificationInfo>{
        <I> void emitNotifications(final String category, final I input, final BiFunction<? super I, ? super F, Stream<Notification>> notificationFactory);
        void emitNotifications(final String category, final Function<? super F, Stream<Notification>> notificationFactory);
    }

    private static final class NotificationSupport<F extends MBeanNotificationInfo> extends NotificationRepository<F> implements NotificationEmitter<F>{
        private static final long serialVersionUID = 5910049029834039555L;
        private FeatureFactory<? super NotificationDescriptor, ? extends F> notificationFactory;
        private ExecutorService executor;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public NotificationSupport() {

        }

        @Override
        public <I> void emitNotifications(final String category, final I input, final BiFunction<? super I, ? super F, Stream<Notification>> notificationFactory) {
            if (executor == null)
                emitStream(category, input, notificationFactory);
            else
                emitStream(category, input, notificationFactory, executor);
        }

        @Override
        public void emitNotifications(final String category, final Function<? super F, Stream<Notification>> notificationFactory) {
            if (executor == null)
                emitStream(category, notificationFactory);
            else
                emitStream(category, notificationFactory, executor);
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeObject(notificationFactory);
            out.writeObject(executor);
            super.writeExternal(out);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            notificationFactory = (FeatureFactory<? super NotificationDescriptor, ? extends F>) in.readObject();
            executor = (ExecutorService) in.readObject();
            super.readExternal(in);
        }

        NotificationManager createManager(@Nonnull final AbstractManagedResourceConnector owner) {
            final class DefaultNotificationManager implements NotificationManager {
                @Override
                public void enableNotifications(final String category, final NotificationDescriptor descriptor) throws JMException {
                    owner.addFeature(NotificationSupport.this, category, descriptor, notificationFactory);
                }

                @Override
                public boolean disableNotifications(final String category) {
                    return owner.removeFeature(NotificationSupport.this, category);
                }

                @Override
                public void retainNotifications(final Set<String> events) {
                    owner.retainFeatures(NotificationSupport.this, events);
                }

                @Override
                public Map<String, NotificationDescriptor> discoverNotifications() {
                    return owner.discoverNotifications();
                }

                private boolean equalsRepository(final FeatureRepository<?> repository) {
                    return NotificationSupport.this.equals(repository);
                }

                private boolean equalsConnector(final ManagedResourceConnector connector) {
                    return owner.equals(connector);
                }

                private boolean equals(final DefaultNotificationManager other) {
                    return other.equalsConnector(owner) && other.equalsRepository(NotificationSupport.this);
                }

                @Override
                public boolean equals(final Object other) {
                    return getClass().isInstance(other) && equals((DefaultNotificationManager) other);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(owner, NotificationSupport.this);
                }

                @Override
                public String toString() {
                    return owner.toString();
                }
            }
            return new DefaultNotificationManager();
        }
    }

    private static final class OperationSupport<F extends MBeanOperationInfo> extends OperationRepository<F>{
        private static final long serialVersionUID = 1788863469686299483L;
        private FeatureFactory<? super OperationDescriptor, ? extends F> operationFactory;
        private OperationInvoker<F> operationInvoker;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public OperationSupport(){

        }

        Object invoke(final String actionName, final Object[] arguments, final String[] signature) throws MBeanException, ReflectionException {
            if (operationInvoker == null)
                throw new MBeanException(JMExceptionUtils.operationNotFound(actionName));
            else
                return invoke(actionName, arguments, signature, operationInvoker);
        }

        OperationManager createManager(final AbstractManagedResourceConnector owner){
            final class DefaultOperationManager implements OperationManager{

                @Override
                public void enableOperation(final String operationName, final OperationDescriptor descriptor) throws JMException {
                    owner.addFeature(OperationSupport.this, operationName, descriptor, operationFactory);
                }

                @Override
                public boolean disableOperation(final String operationName) {
                    return owner.removeFeature(OperationSupport.this, operationName);
                }

                @Override
                public void retainOperations(final Set<String> operations) {
                    owner.retainFeatures(OperationSupport.this, operations);
                }

                @Override
                public Map<String, OperationDescriptor> discoverOperations() {
                    return owner.discoverOperations();
                }

                private boolean equalsRepository(final FeatureRepository<?> repository) {
                    return OperationSupport.this.equals(repository);
                }

                private boolean equalsConnector(final ManagedResourceConnector connector) {
                    return owner.equals(connector);
                }

                private boolean equals(final DefaultOperationManager other){
                    return other.equalsRepository(OperationSupport.this) && other.equalsConnector(owner);
                }

                @Override
                public boolean equals(final Object other) {
                    return getClass().isInstance(other) && equals((DefaultOperationManager) other);
                }

                @Override
                public int hashCode(){
                    return Objects.hash(owner, OperationSupport.this);
                }

                @Override
                public String toString() {
                    return owner.toString();
                }
            }
            return new DefaultOperationManager();
        }
    }

    /**
     * Provides tuning of functionality for managed resource connector.
     * @since 2.1
     */
    protected static final class Functionality {
        private AttributeSupport<?> attributeSupport;
        private NotificationSupport<?> notificationSupport;
        private OperationSupport<?> operationSupport;

        public <F extends MBeanAttributeInfo> void enableAttributeSupport(@Nonnull final FeatureFactory<? super AttributeDescriptor, ? extends F> attributeFactory,
                                                                          final AttributeRepository.AttributeReader<? super F> reader,
                                                                          final AttributeRepository.AttributeWriter<? super F> writer) {
            final AttributeSupport<F> attributeSupport = new AttributeSupport<>();
            attributeSupport.attributeFactory = Objects.requireNonNull(attributeFactory);
            attributeSupport.attributeReader = reader;
            attributeSupport.attributeWriter = writer;
            this.attributeSupport = attributeSupport;
        }

        public <F extends MBeanAttributeInfo, S extends FeatureFactory<? super AttributeDescriptor, ? extends F> & AttributeRepository.AttributeReader<? super F> & AttributeRepository.AttributeWriter<? super F>> void enableAttributeSupport(@Nonnull final S attributeSupport) {
            enableAttributeSupport(attributeSupport, attributeSupport, attributeSupport);
        }

        public <F extends MBeanNotificationInfo> NotificationEmitter<F> enableNotificationSupport(@Nonnull final FeatureFactory<? super NotificationDescriptor, ? extends F> notificationFactory) {
            final NotificationSupport<F> notificationSupport = new NotificationSupport<>();
            notificationSupport.notificationFactory = Objects.requireNonNull(notificationFactory);
            this.notificationSupport = notificationSupport;
            return notificationSupport;
        }

        public <F extends MBeanOperationInfo> void enableOperationSupport(@Nonnull final FeatureFactory<? super OperationDescriptor, ? extends F> operationFactory,
                                                                          final OperationRepository.OperationInvoker<F> invoker) {
            final OperationSupport<F> operationSupport = new OperationSupport<>();
            operationSupport.operationFactory = Objects.requireNonNull(operationFactory);
            operationSupport.operationInvoker = invoker;
            this.operationSupport = operationSupport;
        }

        public <F extends MBeanOperationInfo, S extends FeatureFactory<? super OperationDescriptor, ? extends F> & OperationRepository.OperationInvoker<F>> void enableOperationSupport(@Nonnull S operationSupport){
            enableOperationSupport(operationSupport, operationSupport);
        }

        public void setExecutor(final ExecutorService executor) {
            if (attributeSupport != null)
                attributeSupport.executor = executor;
            if (notificationSupport != null)
                notificationSupport.executor = executor;
        }
    }

    private final LazyReference<MetricsSupport> metrics;
    private final WeakEventListenerList<ResourceEventListener, ResourceEvent> listeners;
    //override
    private final AttributeSupport<?> attributeSupport;
    private final NotificationSupport<?> notificationSupport;
    private final OperationSupport<?> operationSupport;

    /**
     * Name of the managed resource.
     */
    protected final String resourceName;
    /**
     * Logger associated with this instance of connector.
     */
    protected final Logger logger;

    /**
     * Initializes a new instance of managed resource connector.
     * @param resourceName Name of managed resource.
     * @param functionality Functionality provided by the connector instead of direct override of methods.
     */
    protected AbstractManagedResourceConnector(final String resourceName, @Nonnull final Functionality functionality) {
        this.resourceName = Objects.requireNonNull(resourceName);
        this.logger = LoggerProvider.getLoggerForObject(this);
        attributeSupport = functionality.attributeSupport;
        notificationSupport = functionality.notificationSupport;
        operationSupport = functionality.operationSupport;
        metrics = LazyReference.strong();
        listeners = new WeakEventListenerList<>(ResourceEventListener::resourceModified);
    }

    /**
     * Initializes a new instance of managed resource connector.
     * <p>
     *     If you use this constructor then derived class should explicitly override the following methods:
     *     <ul>
     *         <li>{@link #getAttributeInfo()}</li>
     *         <li>{@link #getAttribute(String)}</li>
     *         <li>{@link #setAttribute(Attribute)}</li>
     *         <li>{@link #getAttributes()}</li>
     *         <li>{@link #getNotificationInfo()} and implements {@link NotificationBroadcaster}</li>
     *         <li>{@link #getOperationInfo()}</li>
     *         <li>{@link #invoke(String, Object[], String[])}</li>
     *     </ul>
     * @param resourceName Name of managed resource.
     */
    protected AbstractManagedResourceConnector(final String resourceName) {
        this(resourceName, new Functionality());
    }

    /**
     * Gets membership in cluster.
     * @return Membership in cluster.
     */
    protected final ClusterMember getClusterMembership(){
        return ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public final void addResourceEventListener(final ResourceEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public final void removeResourceEventListener(final ResourceEventListener listener) {
        listeners.remove(listener);
    }

    @SafeVarargs
    protected static MetricsSupport assembleMetricsReader(final Supplier<? extends Metric>... metrics) {
        return new ImmutableMetrics(metrics, Supplier::get);
    }

    protected static MetricsSupport assembleMetricsReader(final Metric... metrics){
        return new ImmutableMetrics(metrics);
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attributeName The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException Attribute doesn't exist.
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeSupport == null)
            throw JMExceptionUtils.attributeNotFound(attributeName);
        else
            return attributeSupport.getAttribute(attributeName);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException Attribute doesn't exist.
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        if (attributeSupport == null)
            throw JMExceptionUtils.attributeNotFound(attribute.getName());
        else
            attributeSupport.setAttribute(attribute);
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
        final AttributeList result = new AttributeList();
        for (final String attributeName : attributes)
            try {
                result.add(new Attribute(attributeName, getAttribute(attributeName)));
            } catch (final JMException e) {
                logger.log(Level.SEVERE, "Unable to read attribute " + attributeName, e);
            }
        return result;
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
        switch (attributes.size()){
            case 1:
                try {
                    setAttribute(attributes.asList().get(0));
                } catch (final JMException e) {
                    logger.log(Level.SEVERE, "Unable to set attribute " + attributes.get(0), e);
                }
            case 0:
                return attributes;
            default:
                final AttributeList result = new AttributeList(attributes.size());
                for (final Attribute attribute : attributes.asList()) {
                    try {
                        setAttribute(attribute);
                        result.add(attribute);
                    } catch (final JMException e) {
                        logger.log(Level.SEVERE, "Unable to set attribute " + attribute.getName(), e);
                    }
                }
                return result;
        }
    }

    /**
     * Gets the values of all attributes.
     *
     * @return The values of all attributes
     * @since 2.1
     */
    @Override
    public AttributeList getAttributes() throws MBeanException, ReflectionException {
        return attributeSupport == null ? new AttributeList() : attributeSupport.getAttributes();
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     * @throws javax.management.MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        if(operationSupport == null)
            throw new MBeanException(JMExceptionUtils.operationNotFound(actionName));
        else
            return operationSupport.invoke(actionName, params, signature);
    }

    /**
     * Returns the localized description of this connector.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this connector.
     */
    @Override
    public String toString(final Locale locale) {
        return resourceName;
    }

    /**
     * Gets an array of supported attributes.
     * @return An array of supported attributes.
     */
    public MBeanAttributeInfo[] getAttributeInfo() {
        return attributeSupport == null ?
                ArrayUtils.emptyArray(MBeanAttributeInfo[].class):
                getFeatureInfo(attributeSupport, MBeanAttributeInfo.class);
    }

    /**
     * Gets an array of supported notifications.
     * @return An array of supported notifications.
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notificationSupport == null ?
                ArrayUtils.emptyArray(MBeanNotificationInfo[].class) :
                notificationSupport.getNotificationInfo();
    }

    /**
     * Gets an array of supported operations.
     * @return An array of supported operations.
     */
    public MBeanOperationInfo[] getOperationInfo() {
        return operationSupport == null ?
                ArrayUtils.emptyArray(MBeanOperationInfo[].class) :
                getFeatureInfo(operationSupport, MBeanOperationInfo.class);
    }

    /**
     * Creates a new reader of metrics provided by this resource connector.
     * <p>
     *     You should not mark implementation method
     *     with annotation {@link Aggregation}.
     *     The easiest way to implement this
     *     method is to call method {@link #assembleMetricsReader(Metric...)}.
     * @return A new reader of metrics provided by this resource connector.
     */
    protected MetricsSupport createMetricsReader() {
        final List<Metric> metrics = new LinkedList<>();
        if (attributeSupport != null)
            metrics.add(attributeSupport.metrics);
        if (notificationSupport != null)
            metrics.add(notificationSupport.metrics);
        if (operationSupport != null)
            metrics.add(operationSupport.metrics);
        return new ImmutableMetrics(metrics);
    }

    /**
     * Gets metrics associated with this instance of the resource connector.
     * @return Connector metrics.
     * @throws IllegalStateException This connector is closed.
     */
    @Aggregation    //already cached in the field
    @SpecialUse(SpecialUse.Case.REFLECTION)
    public final MetricsSupport getMetrics() {
        return metrics.get(this, AbstractManagedResourceConnector::createMetricsReader);
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClass().getName(),
                toString(Locale.getDefault()),
                getAttributeInfo(),
                emptyArray(MBeanConstructorInfo[].class),
                getOperationInfo(),
                getNotificationInfo());
    }

    private static Consumer<Throwable> getLogger(final Logger logger, final String message){
        return e -> logger.log(Level.SEVERE, message, e);
    }

    private Consumer<Throwable> getLogger(final String message){
        return getLogger(logger, message);
    }

    /**
     * Notifies all listeners attached to this resource connector that some feature of this connector is modified.
     * @param feature Modiifed feature.
     * @param modifier Modification.
     * @since 2.1
     */
    protected final void featureModified(final MBeanFeatureInfo feature, final FeatureModifiedEvent.Modifier modifier){
        listeners.fire(new FeatureModifiedEvent(this, resourceName, feature, modifier));
    }

    /**
     * Triggered by {@linkplain #removeFeature(AbstractConcurrentResourceAccessor, String)} for feature cleanup.
     * @param feature The feature to clean.
     * @since 2.1
     * @see #removeFeature(AbstractConcurrentResourceAccessor, String) 
     */
    protected void removedFeature(final MBeanFeatureInfo feature) {
        Convert.toType(feature, SafeCloseable.class).ifPresent(SafeCloseable::close);
    }

    /**
     * Converts repository of features into array of features.
     * @param features Repository of features.
     * @param featureType Type of array elements.
     * @param <F> Type of features in repository.
     * @return An array of features.
     * @since 2.1
     * @see #getAttributeInfo()
     * @see #getNotificationInfo()
     * @see #getOperationInfo()
     */
    protected static <F extends MBeanFeatureInfo> F[] getFeatureInfo(@Nonnull final AbstractConcurrentResourceAccessor<? extends Map<String, ? extends F>> features,
                                                             @Nonnull final Class<F> featureType) {
        return features.read(f -> f.values().stream().toArray(ArrayUtils.arrayConstructor(featureType)));
    }

    private <F extends MBeanFeatureInfo> boolean removeFeature(final Map<String, F> features,
                                                               final String featureName){
        F feature = features.get(featureName);
        if (feature != null) {
            featureModified(feature, FeatureModifiedEvent.Modifier.REMOVING);
            feature = features.remove(featureName);
            removedFeature(feature);
            return true;
        } else
            return false;
    }

    /**
     * Remove feature from repository.
     * @param features Repository of features.
     * @param featureName Name of feature to remove.
     * @return {@literal true}, if feature is removed successfully; otherwise, {@literal false}.
     * @since 2.1
     */
    protected final boolean removeFeature(@Nonnull final AbstractConcurrentResourceAccessor<? extends Map<String, ? extends MBeanFeatureInfo>> features,
                                                                       final String featureName) {
        return features.write(f -> removeFeature(f, featureName),
                null,
                getLogger("Failed to remove feature"))
                .orElse(false);
    }

    private void removeFeatures(final Map<String, ? extends MBeanFeatureInfo> features){
        for (final Iterator<? extends MBeanFeatureInfo> iterator = features.values().iterator(); iterator.hasNext(); ) {
            final MBeanFeatureInfo feature = iterator.next();
            featureModified(feature, FeatureModifiedEvent.Modifier.REMOVING);
            iterator.remove();
            removedFeature(feature);
        }
    }

    /**
     * Removes all features from the repository.
     * @param features Repository of features.
     * @since 2.1
     */
    protected final void removeFeatures(@Nonnull final AbstractConcurrentResourceAccessor<? extends Map<String, ? extends MBeanFeatureInfo>> features) {
        features.write(fromConsumer(this::removeFeatures),
                null,
                getLogger("Failed to remove all features"));
    }

    private Set<String> retainFeatures(final Map<String, ? extends MBeanFeatureInfo> features,
                                       final Set<String> featuresToHold){
        final Set<String> removedFeatures = Sets.difference(features.keySet(), featuresToHold);
        for(final String featureName: removedFeatures)
            removeFeature(features, featureName);
        return removedFeatures;
    }

    /**
     * Retain features in the repository.
     * @param features Repository of features.
     * @param featuresToHold Names of features to retain.
     * @return A set of removed features.
     * @since 2.1
     */
    protected final Set<String> retainFeatures(@Nonnull final AbstractConcurrentResourceAccessor<? extends Map<String, ? extends MBeanFeatureInfo>> features,
                                                                     final Set<String> featuresToHold) {
        return features.write(f -> retainFeatures(f, featuresToHold),
                null,
                getLogger("Failed to retain features"))
                .orElseGet(Collections::emptySet);
    }

    private <I, F extends MBeanFeatureInfo> void addFeature(final Map<String, F> features,
                                                            final String featureName,
                                                            final I input,
                                                            @Nonnull final FeatureFactory<? super I, ? extends F> factory) throws Exception {
        F holder = features.get(featureName);
        //if feature exists then we should check whether the input arguments
        //are equal to the existing feature options
        if (holder != null) {
            if (Objects.equals(input, holder.getDescriptor()))
                return;
            else {
                //remove attribute
                featureModified(holder, FeatureModifiedEvent.Modifier.REMOVING);
                holder = features.remove(featureName);
                removedFeature(holder);
                //...and register again
            }
        }
        holder = factory.createFeature(featureName, input);
        features.put(featureName, holder);
        featureModified(holder, FeatureModifiedEvent.Modifier.ADDED);
    }

    /**
     * Adds a new feature to the repository.
     * @param features Repository of features.
     * @param featureName Name of the feature to be added.
     * @param input Additional input argument for factory.
     * @param factory Feature factory.
     * @param <I> Type of input argument for factory.
     * @param <F> Type of features in repository.
     * @throws JMException Unable to create a new feature.
     * @since 2.1
     */
    protected final <I, F extends MBeanFeatureInfo> void addFeature(@Nonnull final AbstractConcurrentResourceAccessor<? extends Map<String, F>> features,
                                                                    final String featureName,
                                                                    final I input,
                                                                    @Nonnull final FeatureFactory<? super I, ? extends F> factory) throws JMException {
        try {
            features.write(fromAcceptor(f -> addFeature(f, featureName, input, factory)), null);
        } catch (final JMException e) {
            throw e;
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        }
    }

    protected Map<String, AttributeDescriptor> discoverAttributes(){
        return Collections.emptyMap();
    }

    protected Map<String, NotificationDescriptor> discoverNotifications(){
        return Collections.emptyMap();
    }

    protected Map<String, OperationDescriptor> discoverOperations(){
        return Collections.emptyMap();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        Optional<?> result;
        if (objectType.isAssignableFrom(AttributeManager.class) && attributeSupport != null)
            result = Optional.of(attributeSupport.createManager(this));
        else if (objectType.isAssignableFrom(NotificationManager.class) && notificationSupport != null)
            result = Optional.of(notificationSupport.createManager(this));
        else if (objectType.isAssignableFrom(NotificationBroadcaster.class) && notificationSupport != null)
            result = Optional.of(notificationSupport);
        else if (objectType.isAssignableFrom(OperationSupport.class) && operationSupport != null)
            result = Optional.of(operationSupport.createManager(this));
        else
            return super.queryObject(objectType);
        return result.map(objectType::cast);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        if(attributeSupport != null)
            removeFeatures(attributeSupport);
        if(notificationSupport != null)
            removeFeatures(notificationSupport);
        if(operationSupport != null)
            removeFeatures(operationSupport);
        metrics.remove();
        listeners.clear();
        clearCache();
    }
}
