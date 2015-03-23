package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.concurrent.AsyncEventListener;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.connectors.*;
import com.itworks.snamp.connectors.attributes.*;
import com.itworks.snamp.connectors.notifications.NotificationAddedEvent;
import com.itworks.snamp.connectors.notifications.NotificationRemovedEvent;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.connectors.notifications.TypeBasedNotificationFilter;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.core.RichLogicalOperation;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.WeakMultimap;
import com.itworks.snamp.jmx.JMExceptionUtils;
import com.itworks.snamp.jmx.WellKnownType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import javax.management.*;
import javax.management.openmbean.OpenType;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Represents a base class for constructing custom resource adapters.
 * <p>
 *     Resource adapter is not an OSGi service because this is front-end SNAMP component.
 *     Therefore, an instance of the adapter is not accessible through OSGi environment.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractResourceAdapter extends AbstractAggregator implements ResourceAdapter, ResourceEventListener{
    private static final Multimap<String, WeakReference<ResourceAdapterEventListener>> listeners = HashMultimap.create(10, 3);
    private static final ExecutorService eventExecutor = Executors.newSingleThreadExecutor(new GroupedThreadFactory("ADAPTER_EVENTS"));

    private static final class AdapterLogicalOperation extends RichLogicalOperation {
        private static final String ADAPTER_INSTANCE_NAME_PROPERTY = "adapterInstanceName";

        private AdapterLogicalOperation(final String operationName,
                                        final String adapterInstanceName){
            super(operationName, ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstanceName);
        }

        private static AdapterLogicalOperation connectorChangesDetected(final String adapterInstanceName){
            return new AdapterLogicalOperation("processResourceConnectorChanges", adapterInstanceName);
        }
    }

    /**
     * Represents an abstract class for all managed resource feature accessor.
     * This class cannot be derived directly from your code.
     * @param <M> The type of the managed resource feature.
     * @param <S> The type of the feature supporter.
     */
    protected static abstract class FeatureAccessor<M extends MBeanFeatureInfo, S extends FeatureSupport> {
        private final M metadata;

        private FeatureAccessor(final M metadata){
            this.metadata = Objects.requireNonNull(metadata);
        }

        /**
         * Gets metadata of the feature associated with this accessor.
         * @return The metadata of the feature associated with this accessor.
         */
        public final M getMetadata(){
            return metadata;
        }

        abstract void connect(final S value);

        abstract void disconnect();

        @Override
        public String toString() {
            return getMetadata().toString();
        }
    }

    /**
     * Exposes access to the individual notification.
     * @author Roman Sakno
     * @since 1.0
     */
    public static abstract class NotificationAccessor extends FeatureAccessor<MBeanNotificationInfo, NotificationSupport> implements NotificationListener {
        private NotificationSupport notificationSupport;

        protected NotificationAccessor(final MBeanNotificationInfo metadata) {
            super(metadata);
            this.notificationSupport = null;
        }

        @Override
        final void connect(final NotificationSupport value) {
            this.notificationSupport = value;
            if(value != null)
                value.addNotificationListener(this, createFilter(), null);
        }

        @Override
        final void disconnect() {
            try {
                final NotificationSupport ns = this.notificationSupport;
                if(ns != null)
                    ns.removeNotificationListener(this);
            }
            catch (ListenerNotFoundException ignored) {
            }
            finally {
                this.notificationSupport = null;
            }
        }

        /**
         * Gets notification type.
         * @return The notification type.
         */
        public final String getType(){
            return getMetadata().getNotifTypes()[0];
        }

        /**
         * Creates a new notification filter for this type of the metadata.
         * @return A new notification filter.
         * @see javax.management.MBeanNotificationInfo#getNotifTypes()
         */
        public final NotificationFilter createFilter(){
            return new TypeBasedNotificationFilter(getMetadata());
        }
    }

    /**
     * Exposes access to individual management attribute.
     * <p>
     *     This accessor can be used for retrieving and changing value of the attribute.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static class AttributeAccessor extends FeatureAccessor<MBeanAttributeInfo, AttributeSupport> implements AttributeValueReader, Consumer<Object, JMException> {
        private AttributeSupport attributeSupport;

        /**
         * Initializes a new attribute accessor.
         * @param metadata The metadata of the attribute. Cannot be {@literal null}.
         */
        public AttributeAccessor(final MBeanAttributeInfo metadata) {
            super(metadata);
            attributeSupport = null;
        }

        private AttributeSupport verifyOnDisconnected() throws AttributeNotFoundException {
            final AttributeSupport as = attributeSupport;
            if(as == null)
                throw JMExceptionUtils.attributeNotFound(getMetadata().getName());
            else return as;
        }

        @Override
        final void connect(final AttributeSupport value) {
            attributeSupport = value;
        }

        @Override
        final void disconnect() {
            attributeSupport = null;
        }

        /**
         * Gets name of the attribute.
         * @return The name of the attribute.
         */
        public final String getName(){
            return getMetadata().getName();
        }

        /**
         * Gets type of this attribute.
         * @return The type of this attribute.
         */
        public final WellKnownType getType(){
            return CustomAttributeInfo.getType(getMetadata());
        }

        /**
         * Gets JMX Open Type of this attribute.
         * @return The type of this attribute.
         */
        public final OpenType<?> getOpenType(){
            return AttributeDescriptor.getOpenType(getMetadata());
        }

        /**
         * Changes the value of the attribute.
         * @param value A new attribute value.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws MBeanException Internal connector error.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Value type mismatch.
         */
        public final void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            verifyOnDisconnected().setAttribute(new Attribute(getName(), value));
        }

        /**
         * Changes the value of the attribute.
         * @param value A new attribute value.
         * @throws javax.management.JMException Internal connector error.
         * @throws InvalidAttributeValueException Value type mismatch.
         * @throws AttributeNotFoundException This attribute is disconnected.
         */
        @Override
        public final void accept(final Object value) throws JMException {
            setValue(value);
        }

        /**
         * Gets attribute value.
         * @return The attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         */
        public final Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
            return verifyOnDisconnected().getAttribute(getName());
        }

        /**
         * Gets attribute value in typed manner.
         * @param valueType The expected type of the attribute.
         * @param <T> The expected type of the attribute.
         * @return The typed attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         */
        public final  <T> T getValue(final TypeToken<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
            final Object result = getValue();
            try {
                return TypeTokens.cast(result, valueType);
            }
            catch (final ClassCastException e){
                throw new InvalidAttributeValueException(e.getMessage());
            }
        }

        /**
         * Gets attribute value in typed manner.
         * @param valueType The expected type of the attribute.
         * @param <T> The expected type of the attribute.
         * @return The typed attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         */
        public final  <T> T getValue(final Class<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
            return getValue(TypeToken.of(valueType));
        }

        /**
         * Gets attribute value in typed manner.
         * @param valueType The expected type of the attribute.
         * @return The typed attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         */
        public final Object getValue(final WellKnownType valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
            return getValue(valueType.getType());
        }

        /**
         * Gets attribute value in typed manner.
         * @param valueType The expected type of the attribute.
         * @param <T> The expected type of the attribute.
         * @return The typed attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         */
        @SuppressWarnings("unchecked")
        public final  <T> T getValue(final OpenType<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
            final Object result = getValue();
            if(valueType.isValue(result)) return (T)result;
            else throw new InvalidAttributeValueException(String.format("Value %s is not of type %s", result, valueType));
        }

        /**
         * Gets attribute value and type.
         * @return The attribute value and type.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         */
        public final AttributeValue getRawValue() throws MBeanException, AttributeNotFoundException, ReflectionException{
            return new AttributeValue(getName(), getValue(), getType());
        }

        private <I, O> O getValue(final TypeToken<I> valueType,
                                  final AttributeInputValueConverter<O> converter) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            final Function<? super I, ? extends O> f = converter.getConverter(valueType);
            if(f == null) throw new InvalidAttributeValueException(String.format("Converter for %s doesn't exist", valueType));
            else {
                final I attributeValue;
                try{
                    attributeValue = TypeTokens.cast(getValue(), valueType);
                }
                catch (final ClassCastException e){
                    throw new InvalidAttributeValueException(e.getMessage());
                }
                return f.apply(attributeValue);
            }
        }

        /**
         * Gets attribute value converted into the adapter-specific type.
         * @param converter The attribute value converter. Cannot be {@literal null}.
         * @param <T> Type of the adapter-specific value.
         * @return The adapter-specific value of the attribute.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         */
        public final <T> T getValue(final AttributeInputValueConverter<T> converter) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException, ReflectionException {
            final WellKnownType type = getType();
            if (type != null)
                return getValue(type.getTypeToken(), converter);
            else
                return getValue(TypeToken.of(getRawType()), converter);
        }

        public final Class<?> getRawType() throws ReflectionException{
            try {
                return Class.forName(getMetadata().getType());
            } catch (ClassNotFoundException e) {
                throw new ReflectionException(e);
            }
        }

        private <I, O> void setValue(final I input,
                                     final TypeToken<O> outputType,
                                     final AttributeOutputValueConverter<I> converter) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException, ReflectionException {
            final Function<? super I, ? extends O> f = converter.getConverter(outputType);
            if(f == null) throw new InvalidAttributeValueException(String.format("Converter for %s doesn't exist", outputType));
            else setValue(f.apply(input));
        }

        /**
         * Modifies attribute using adapter-specific value.
         * @param value The adapter-specific value to be converted into the attribute value.
         * @param converter The adapter-specific value converter. Cannot be {@literal null}.
         * @param <I> Type of the adapter-specific value.
         * @throws ReflectionException Internal connector error.
         * @throws MBeanException Internal connector error.
         * @throws InvalidAttributeValueException Attribute type mismatch.
         * @throws AttributeNotFoundException This attribute is disconnected.
         */
        public final <I> void setValue(final I value, final AttributeOutputValueConverter<I> converter) throws ReflectionException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
            final WellKnownType type = getType();
            if (type != null) setValue(value, type.getTypeToken(), converter);
            else
                setValue(value, TypeToken.of(getRawType()), converter);
        }

        /**
         * Gets attribute value.
         *
         * @return The attribute value.
         * @throws javax.management.JMException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         */
        @Override
        public final Object call() throws JMException {
            return getValue();
        }
    }

    private static final class InternalState{
        private final ImmutableMap<String, String> parameters;
        private final AdapterState state;

        private InternalState(final AdapterState state, final ImmutableMap<String, String> params){
            this.state = state;
            this.parameters = params;
        }

        private static InternalState initialState(){
            return new InternalState(AdapterState.CREATED, ImmutableMap.<String, String>of());
        }

        private InternalState setParameters(final Map<String, String> value){
            return new InternalState(state, ImmutableMap.copyOf(value));
        }

        private InternalState setAdapterState(final AdapterState value){
            return new InternalState(value, parameters);
        }

        private static InternalState finalState(){
            return new InternalState(AdapterState.CLOSED, ImmutableMap.<String, String>of());
        }
    }

    private InternalState mutableState;
    private final String adapterInstanceName;
    private final WriteOnceRef<ResourceAdapterEventListener> listener;

    /**
     * Initializes a new resource adapter.
     * @param instanceName The name of the adapter instance.
     */
    protected AbstractResourceAdapter(final String instanceName) {
        this.adapterInstanceName = instanceName;
        mutableState = InternalState.initialState();
        listener = new WriteOnceRef<>();
    }

    /**
     * Gets name of this adapter instance.
     * @return The name of the adapter instance.
     */
    @Override
    public final String getInstanceName(){
        return adapterInstanceName;
    }

    /**
     * Gets state of this adapter.
     * @return The state of this adapter.
     */
    @Override
    public final AdapterState getState(){
        final InternalState current = mutableState;
        return current != null ? current.state : AdapterState.CLOSED;
    }

    private void attributeAdded(final AttributeAddedEvent event){
        final AttributeSupport as = event.getSource();
        final FeatureAccessor<MBeanAttributeInfo, AttributeSupport> accessor =
                addFeature(as.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.connect(as);
    }

    private void attributeRemoved(final AttributeRemovedEvent event){
        final FeatureAccessor<MBeanAttributeInfo, ?> accessor =
                removeFeature(event.getSource().getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.disconnect();
    }

    private void notificationAdded(final NotificationAddedEvent event){
        final NotificationSupport ns = event.getSource();
        final FeatureAccessor<MBeanNotificationInfo, NotificationSupport> accessor =
                addFeature(ns.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.connect(ns);
    }

    private void notificationRemoved(final NotificationRemovedEvent event){
        final FeatureAccessor<MBeanNotificationInfo, ?> accessor = removeFeature(event.getSource().getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.disconnect();
    }

    /**
     * Handles resource event.
     *
     * @param event An event to handle.
     * @see com.itworks.snamp.connectors.FeatureAddedEvent
     * @see com.itworks.snamp.connectors.FeatureRemovedEvent
     */
    @Override
    public final void handle(final ResourceEvent event) {
        if(event instanceof AttributeAddedEvent)
            attributeAdded((AttributeAddedEvent) event);
        else if(event instanceof AttributeRemovedEvent)
            attributeRemoved((AttributeRemovedEvent)event);
        else if(event instanceof NotificationAddedEvent)
            notificationAdded((NotificationAddedEvent)event);
        else if(event instanceof NotificationRemovedEvent)
            notificationRemoved((NotificationRemovedEvent)event);
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource extended
     * with the specified feature.
     * @param resourceName The name of the managed resource.
     * @param feature A new feature of the managed resource.
     * @param <M> Type of the managed resource feature.
     * @param <S> Type of the object that provides support for the specified feature.
     * @return A new instance of the resource feature accessor. May be {@literal null}.
     * @see AttributeAccessor
     * @see NotificationAccessor
     */
    protected abstract <M extends MBeanFeatureInfo, S extends FeatureSupport> FeatureAccessor<M, S> addFeature(final String resourceName,
                                       final M feature);

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource
     * was removed from SNAMP.
     * @param resourceName The name of the resource.
     * @return Read-only collection of features tracked by this resource adapter. Cannot be {@literal null}.
     */
    protected abstract Collection<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName);

    /**
     * Invokes automatically by SNAMP infrastructure when the feature was removed
     * from the specified resource.
     * @param resourceName The name of the managed resource.
     * @param feature The resource feature that was removed.
     * @param <M> The type of the resource feature.
     * @return An instance of the feature accessor used by this resource adapter. May be {@literal null}.
     */
    protected abstract <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName,
                                                                                        final M feature);

    /**
     * Starts the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param parameters Adapter startup parameters.
     * @throws java.lang.Exception Unable to start adapter.
     */
    protected abstract void start(final Map<String, String> parameters) throws Exception;

    /**
     * Updates this adapter with a new configuration parameters.
     * <p>
     *     In the default implementation this method causes restarting
     *     of this adapter that affects availability of the adapter.
     *     You should override this method if custom resource adapter
     *     supports soft update (without affecting availability).
     * </p>
     * @param current The current configuration parameters.
     * @param newParameters A new configuration parameters.
     * @throws Exception
     */
    protected synchronized void update(final Map<String, String> current,
                          final Map<String, String> newParameters) throws Exception{
        if(!Utils.mapsAreEqual(current, newParameters)) {
            tryStop();
            tryStart(newParameters);
        }
    }

    final synchronized boolean tryUpdate(final Map<String, String> newParameters) throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case STARTED:
                final InternalState newState = currentState.setParameters(newParameters);
                update(currentState.parameters, newState.parameters);
                mutableState = newState;
                return true;
            default:
                return false;
        }
    }

    private void adapterStarted(){
        final ResourceAdapterEventListener listener = this.listener.get();
        if(listener != null)
            listener.adapterStarted(new ResourceAdapterEvent(this));
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Adapter %s is started. Context: %s",
                    adapterInstanceName,
                    LogicalOperation.current()));
        }
    }

    private void adapterStopped(){
        final ResourceAdapterEventListener listener = this.listener.get();
        if(listener != null)
            listener.adapterStopped(new ResourceAdapterEvent(this));
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Adapter %s is stopped. Context: %s",
                    adapterInstanceName,
                    LogicalOperation.current()));
        }
    }

    private static ResourceAdapterEventListener createListener(final String adapterName){
        return new ResourceAdapterEventListener() {
            @Override
            public void adapterStarted(final ResourceAdapterEvent e) {
                AbstractResourceAdapter.adapterStarted(adapterName, e);
            }

            @Override
            public void adapterStopped(final ResourceAdapterEvent e) {
                AbstractResourceAdapter.adapterStopped(adapterName, e);
            }
        };
    }

    final boolean tryStart(final String adapterName, final Map<String, String> params) throws Exception {
        return this.listener.set(createListener(adapterName)) && tryStart(params);
    }

    private synchronized void addResource(final ServiceReference<ManagedResourceConnector> resourceRef) {
        final BundleContext context = getBundleContext();
        final ManagedResourceConnector connector = context.getService(resourceRef);
        if (connector != null)
            try {
                //add adapter as a listener
                connector.addResourceEventListener(this);
                //expose all features
                final AttributeSupport attributeSupport = connector.queryObject(AttributeSupport.class);
                if(attributeSupport != null)
                    for(final MBeanAttributeInfo metadata: attributeSupport.getAttributeInfo())
                        attributeAdded(new AttributeAddedEvent(attributeSupport, metadata));
                final NotificationSupport notificationSupport = connector.queryObject(NotificationSupport.class);
                if(notificationSupport != null)
                    for(final MBeanNotificationInfo metadata: notificationSupport.getNotificationInfo())
                        notificationAdded(new NotificationAddedEvent(notificationSupport, metadata));
            } finally {
                context.ungetService(resourceRef);
            }
    }

    private synchronized void removeResource(final ServiceReference<ManagedResourceConnector> resourceRef){
        final BundleContext context = getBundleContext();
        final ManagedResourceConnector connector = context.getService(resourceRef);
        if(connector != null)
            try{
                connector.removeResourceEventListener(this);
                for(final FeatureAccessor<?, ?> accessor: removeAllFeatures(connector.getResourceName()))
                    accessor.disconnect();
            }
            finally {
                context.ungetService(resourceRef);
            }
    }

    private synchronized boolean tryStart(final Map<String, String> params) throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case CREATED:
            case STOPPED:
                //explore all available resources
                final Collection<ServiceReference<ManagedResourceConnector>> resources =
                        getBundleContext().getServiceReferences(ManagedResourceConnector.class, null);
                for(final ServiceReference<ManagedResourceConnector> resourceRef: resources)
                    addResource(resourceRef);
                InternalState newState = currentState.setParameters(params);
                start(newState.parameters);
                mutableState = newState.setAdapterState(AdapterState.STARTED);
                adapterStarted();
                return true;
            default:
                return false;
        }
    }

    private synchronized boolean tryStop() throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case STARTED:
                try {
                    stop();
                    final BundleContext context = getBundleContext();
                    final Collection<ServiceReference<ManagedResourceConnector>> resources =
                            context.getServiceReferences(ManagedResourceConnector.class, null);
                    for(final ServiceReference<ManagedResourceConnector> resourceRef: resources)
                        removeResource(resourceRef);
                }
                finally {
                    mutableState = currentState.setAdapterState(AdapterState.STOPPED);
                }
                adapterStopped();
                return true;
            default:
                return false;
        }
    }

    /**
     * Stops the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @throws java.lang.Exception Unable to stop adapter.
     */
    protected abstract void stop() throws Exception;

    /**
     * Captures reference to the managed resource connectors.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        if (ManagedResourceConnectorClient.isResourceConnector(event.getServiceReference()))
            try (final LogicalOperation ignored = AdapterLogicalOperation.connectorChangesDetected(adapterInstanceName)) {
                @SuppressWarnings("unchecked")
                final ServiceReference<ManagedResourceConnector> connectorRef = (ServiceReference<ManagedResourceConnector>) event.getServiceReference();
                final String resourceName = ManagedResourceConnectorClient.getManagedResourceName(connectorRef);
                switch (event.getType()) {
                    case ServiceEvent.MODIFIED_ENDMATCH:
                    case ServiceEvent.UNREGISTERING:
                        removeResource(connectorRef);
                        return;
                    case ServiceEvent.REGISTERED:
                        addResource(connectorRef);
                        return;
                    default:
                        try (final OSGiLoggingContext logger = getLoggingContext()) {
                            logger.info(String.format("Unexpected event %s captured by adapter %s for resource %s. Context: %s",
                                    event.getType(),
                                    adapterInstanceName,
                                    resourceName,
                                    LogicalOperation.current()));
                        }
                }
            }
    }

    /**
     * Gets name of the logger associated with the specified resource adapter.
     * @param adapterName The name of the resource adapter.
     * @return The name of the logger.
     */
    public static String getLoggerName(final String adapterName){
        return String.format("com.itworks.snamp.adapters.%s", adapterName);
    }

    /**
     * Gets logger associated with the specified resource adapter.
     * @param adapterName The name of the resource adapter.
     * @return The logger of the adapter.
     */
    public static Logger getLogger(final String adapterName){
        return Logger.getLogger(getLoggerName(adapterName));
    }

    /**
     * Releases all resources associated with this adapter.
     * @throws Exception An exception occurred during adapter releasing.
     */
    @Override
    public final void close() throws Exception {
        try {
            tryStop();
        } finally {
            mutableState = InternalState.finalState();
        }
    }

    private BundleContext getBundleContext(){
        return getBundleContextByObject(this);
    }

    private OSGiLoggingContext getLoggingContext(){
        return OSGiLoggingContext.get(getLogger(), getBundleContext());
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    @Aggregation
    public abstract Logger getLogger();

    /**
     * Returns a string representation of this adapter.
     * @return A string representation of this adapter.
     */
    @Override
    public String toString() {
        return adapterInstanceName;
    }

    private static void adapterStarted(final String adapterName,
                               final ResourceAdapterEvent event){
        synchronized (listeners){
            WeakMultimap.removeUnused(listeners);
            for(final WeakReference<ResourceAdapterEventListener> listenerRef: listeners.get(adapterName)) {
                final ResourceAdapterEventListener listener = listenerRef.get();
                if (listener instanceof AsyncEventListener)
                    eventExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.adapterStarted(event);
                        }
                    });
                else if(listener != null)
                    listener.adapterStarted(event);
            }
        }
    }

    private static void adapterStopped(final String adapterName,
                                       final ResourceAdapterEvent event){
        synchronized (listeners){
            WeakMultimap.removeUnused(listeners);
            for(final WeakReference<ResourceAdapterEventListener> listenerRef: listeners.get(adapterName)){
                final ResourceAdapterEventListener listener = listenerRef.get();
                if(listener instanceof AsyncEventListener)
                    eventExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.adapterStopped(event);
                        }
                    });
                else if(listener != null) listener.adapterStopped(event);
            }
        }
    }

    static boolean addEventListener(final String adapterName,
                                           final ResourceAdapterEventListener listener){
        if(adapterName == null || adapterName.isEmpty() || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.put(listeners, adapterName, listener);
        }
    }

    static boolean removeEventListener(final String adapterName,
                                              final ResourceAdapterEventListener listener){
        if(adapterName == null || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.remove(listeners, adapterName, listener) > 0;
        }
    }
}
