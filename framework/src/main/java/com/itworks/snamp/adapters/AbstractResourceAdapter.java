package com.itworks.snamp.adapters;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.concurrent.AsyncEventListener;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.core.RichLogicalOperation;
import com.itworks.snamp.internal.*;
import com.itworks.snamp.internal.annotations.Temporary;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.jmx.JMExceptionUtils;
import com.itworks.snamp.jmx.WellKnownType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.*;
import javax.management.openmbean.OpenType;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.internal.Utils.getBundleContextByObject;
import static com.itworks.snamp.internal.Utils.getStackTrace;

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
public abstract class AbstractResourceAdapter extends AbstractAggregator implements ResourceAdapter{
    private static final Multimap<String, WeakReference<ResourceAdapterEventListener>> listeners = HashMultimap.create(10, 3);
    private static final ExecutorService eventExecutor = Executors.newSingleThreadExecutor(new GroupedThreadFactory("ADAPTER_EVENTS"));

    private static final class AdapterLogicalOperation extends RichLogicalOperation {
        private static final String ADAPTER_INSTANCE_NAME_PROPERTY = "adapterInstanceName";

        private AdapterLogicalOperation(final String operationName,
                                        final String adapterInstanceName){
            super(operationName, ADAPTER_INSTANCE_NAME_PROPERTY, adapterInstanceName);
        }

        private static AdapterLogicalOperation restarting(final String adapterInstanceName){
            return new AdapterLogicalOperation("restart", adapterInstanceName);
        }

        private static AdapterLogicalOperation connectorChangesDetected(final String adapterInstanceName){
            return new AdapterLogicalOperation("processResourceConnectorChanges", adapterInstanceName);
        }
    }

    private static abstract class UnsupportedInternalOperation extends UnsupportedOperationException{
        private static final long serialVersionUID = 4546952459420219703L;

        private UnsupportedInternalOperation(final String message){
            super(message);
        }
    }

    private static final class UnsupportedResourceRemovedOperation extends UnsupportedInternalOperation{
        private static final long serialVersionUID = -7621404696086381259L;

        private UnsupportedResourceRemovedOperation(final String resourceName){
            super(String.format("resourceRemoved for %s is not supported", resourceName));
        }
    }

    private static final class UnsupportedResourceAddedOperation extends UnsupportedInternalOperation{
        private static final long serialVersionUID = 122167705023320271L;

        private UnsupportedResourceAddedOperation(final String resourceName){
            super(String.format("resourceRemoved for %s is not supported", resourceName));
        }
    }

    /**
     * Represents an accessor for individual management attribute.
     * This class cannot be inherited.
     * <p>
     *     This accessor can be used for retrieving and changing value of the attribute.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class AttributeAccessor implements AttributeValueReader, Consumer<Object, JMException> {
        private final AttributeSupport attributeSupport;
        private final MBeanAttributeInfo metadata;

        private AttributeAccessor(final String attributeID,
                                  final AttributeConfiguration attributeConfig,
                                  final AttributeSupport attributeSupport) throws JMException {
            this.metadata = (this.attributeSupport = attributeSupport)
                    .connectAttribute(attributeID,
                            attributeConfig.getAttributeName(),
                            attributeConfig.getReadWriteTimeout(),
                            new ConfigParameters(attributeConfig));
            if(metadata == null)
                throw JMExceptionUtils.attributeNotFound(attributeConfig.getAttributeName());
        }

        /**
         * Disconnects this attribute.
         * @return {@literal true}, if this attribute is disconnected successfully; otherwise, {@literal false}.
         */
        public boolean disconnect(){
            return attributeSupport.disconnectAttribute(metadata.getName());
        }

        /**
         * Gets type of this attribute.
         * @return The type of this attribute.
         */
        public WellKnownType getType(){
            return CustomAttributeInfo.getType(metadata);
        }

        /**
         * Gets JMX Open Type of this attribute.
         * @return The type of this attribute.
         */
        public OpenType<?> getOpenType(){
            return AttributeDescriptor.getOpenType(metadata);
        }

        /**
         * Changes the value of the attribute.
         * @param value A new attribute value.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws MBeanException Internal connector error.
         * @throws ReflectionException Internal connector error.
         * @throws InvalidAttributeValueException Value type mismatch.
         */
        public void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
            attributeSupport.setAttribute(new Attribute(getName(), value));
        }

        /**
         * Changes the value of the attribute.
         * @param value A new attribute value.
         * @throws javax.management.JMException Internal connector error.
         * @throws InvalidAttributeValueException Value type mismatch.
         * @throws AttributeNotFoundException This attribute is disconnected.
         */
        @Override
        public void accept(final Object value) throws JMException {
            setValue(value);
        }

        /**
         * Gets attribute value.
         * @return The attribute value.
         * @throws MBeanException Internal connector error.
         * @throws AttributeNotFoundException This attribute is disconnected.
         * @throws ReflectionException Internal connector error.
         */
        public Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
            return attributeSupport.getAttribute(getName());
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
        public <T> T getValue(final TypeToken<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
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
        public <T> T getValue(final Class<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
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
        public Object getValue(final WellKnownType valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
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
        public <T> T getValue(final OpenType<T> valueType) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException{
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
        public AttributeValue getRawValue() throws MBeanException, AttributeNotFoundException, ReflectionException{
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
        public <T> T getValue(final AttributeInputValueConverter<T> converter) throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException, ReflectionException {
            final WellKnownType type = getType();
            if (type != null)
                return getValue(type.getTypeToken(), converter);
            else
                return getValue(TypeToken.of(getRawType()), converter);
        }

        public Class<?> getRawType() throws ReflectionException{
            try {
                return Class.forName(metadata.getType());
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
        public <I> void setValue(final I value, final AttributeOutputValueConverter<I> converter) throws ReflectionException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
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
        public Object call() throws JMException {
            return getValue();
        }

        /**
         * Gets the name of the attribute.
         * @return The name of the attribute.
         */
        public String getName(){
            return getMetadata().getName();
        }

        /**
         * Gets the attribute metadata associated with this attribute.
         * @return The attribute metadata.
         */
        public MBeanAttributeInfo getMetadata(){
            return metadata;
        }

        /**
         * Gets identifier of this attribute.
         * @return The identifier of this attribute.
         */
        @Override
        public String toString() {
            return metadata.getName();
        }
    }

    /**
     * Represents connector of the managed resource attribute.
     * This class cannot be inherited or instantiated directly from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class AttributeConnector{
        private final AttributeSupport attributes;
        private final AttributeConfiguration attributeConfig;

        private AttributeConnector(final AttributeSupport attrs,
                                   final AttributeConfiguration config){
            this.attributes = attrs;
            this.attributeConfig = config;
        }

        /**
         * Connects a new attribute and assign the name for it.
         * @param attributeID The name of the connected attribute.
         * @return The connected attribute.
         * @throws JMException Unable to connect attribute.
         */
        public AttributeAccessor connect(final String attributeID) throws JMException{
            return new AttributeAccessor(attributeID, attributeConfig, attributes);
        }
    }

    /**
     * Represents connector of the managed resource notification.
     * This class cannot be inherited or instantiated directly from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static final class NotificationConnector{
        private final EventConfiguration eventConfig;
        private final NotificationSupport notifications;

        private NotificationConnector(final NotificationSupport notifs,
                                      final EventConfiguration config){
            this.notifications = notifs;
            this.eventConfig = config;
        }

        /**
         * Enables a new notification.
         * @param listID The name of the notification to be enabled.
         * @return The metadata of the enabled notification.
         * @throws JMException Could not enable notification.
         */
        public MBeanNotificationInfo enable(final String listID) throws JMException{
            return notifications.enableNotifications(listID, eventConfig.getCategory(), new ConfigParameters(eventConfig));
        }
    }

    /**
     * Represents adapter-specific view of the managed resource notifications.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface NotificationsModel extends NotificationListener{
        /**
         * Registers a new notification in this model.
         * @param resourceName The name of the resource that supplies the specified notification.
         * @param userDefinedName Resource-scoped unique identifier of the notification specified by SNAMP administrator.
         * @param category The notification category.
         * @param connector The notification connector.
         */
        void addNotification(final String resourceName,
                             final String userDefinedName,
                             final String category,
                             final NotificationConnector connector);

        /**
         * Removes the notification from this model.
         * @param resourceName The name of the resource that supplies the specified notification.
         * @param userDefinedName Resource-scoped unique identifier of the notification specified by SNAMP administrator.
         * @param category The notification category.
         * @return The enabled notification removed from this model.
         */
        MBeanNotificationInfo removeNotification(final String resourceName,
                                                 final String userDefinedName,
                                                 final String category);

        /**
         * Removes all notifications from this model.
         */
        void clear();

        /**
         * Determines whether this model is empty.
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        boolean isEmpty();
    }

    /**
     * Represents adapter-specific view of the managed resource attributes.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface AttributesModel{
        /**
         * Registers a new attribute in this model.
         * <p>
         *     Don't forget to call {@link com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeConnector#connect(String)}
         *     method and save the connected attribute into the internal model structure.
         * </p>
         * @param resourceName The name of the resource that supplies the specified attribute.
         * @param userDefinedName Resource-scoped unique identifier of the attribute specified by SNAMP administrator.
         * @param attributeName The name of the attribute as it is exposed by resource connector.
         * @param connector The attribute connector.
         */
        void addAttribute(final String resourceName,
                          final String userDefinedName,
                          final String attributeName,
                          final AttributeConnector connector);

        /**
         * Removes the attribute from this model.
         * @param resourceName The name of the resource that supplies the specified attribute.
         * @param userDefinedName Resource-scoped unique identifier of the attribute specified by SNAMP administrator.
         * @param attributeName The name of the attribute as it is exposed by resource connector.
         * @return The connected attribute removed from this accessor.
         */
        AttributeAccessor removeAttribute(final String resourceName,
                                          final String userDefinedName,
                                        final String attributeName);

        /**
         * Removes all attributes from this model.
         */
        void clear();

        /**
         * Determines whether this model is empty.
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        boolean isEmpty();
    }

    private static final class ManagedResourceConnectorConsumer implements ServiceListener, AutoCloseable{
        private final ManagedResourceConfiguration resourceConfiguration;
        private ServiceReferenceHolder<ManagedResourceConnector<?>> resourceConnector;
        private final BundleContext context;

        /**
         * The name of the managed resource.
         */
        private final String resourceName;

        private ManagedResourceConnectorConsumer(final BundleContext context,
                                                 final String resourceName,
                                                 final ManagedResourceConfiguration config){
            this.resourceConfiguration = config;
            this.resourceConnector = null;
            this.resourceName = resourceName;
            this.context = context;
        }

        private <T> T queryWeakObject(final Class<T> queryObject){
            return resourceConnector != null ?
                    resourceConnector.getService().queryObject(queryObject):
                    null;
        }

        private AttributeSupport getWeakAttributeSupport(){
            return queryWeakObject(AttributeSupport.class);
        }

        private NotificationSupport getWeakNotificationSupport(){
            return queryWeakObject(NotificationSupport.class);
        }

        /**
         * Determines whether the resource connector supports attributes.
         * @return {@literal true}, if the connector supports attributes; otherwise, {@literal false}.
         */
        private boolean isAttributesSupported(){
            return resourceConnector != null && resourceConnector.getService().queryObject(AttributeSupport.class) != null;
        }

        /**
         * Determines whether the resource connector supports notifications.
         * @return {@literal true}, if the connector supports notification; otherwise, {@literal false}.
         */
        private boolean isNotificationsSupported(){
            return resourceConnector != null && resourceConnector.getService().queryObject(NotificationSupport.class) != null;
        }

        /**
         * Determines whether the resource connector is referenced.
         * @return {@literal true}, if the resource connector is referenced; otherwise, {@literal false}.
         */
        private boolean isReferenced(){
            return resourceConnector != null;
        }

        private synchronized void processResourceConnector(final ServiceReference<ManagedResourceConnector<?>> connectorRef,
                                                           final int eventType){
            if(Objects.equals(ManagedResourceConnectorClient.getManagedResourceName(connectorRef), resourceName))
                switch (eventType){
                    case ServiceEvent.REGISTERED:
                        if(resourceConnector != null){
                            resourceConnector.release(context);
                        }
                        resourceConnector = new ServiceReferenceHolder<>(context, connectorRef);
                        break;
                    case ServiceEvent.UNREGISTERING:
                    case ServiceEvent.MODIFIED_ENDMATCH:
                        if(resourceConnector != null)
                            resourceConnector.release(context);
                        resourceConnector = null;
                }
        }

        /**
         * Receives notification that a service has had a lifecycle change.
         *
         * @param event The {@code ServiceEvent} object.
         */
        @SuppressWarnings("unchecked")
        @Override
        public void serviceChanged(final ServiceEvent event) {
            if(ManagedResourceConnectorClient.isResourceConnector(event.getServiceReference()))
                processResourceConnector(
                        (ServiceReference<ManagedResourceConnector<?>>)event.getServiceReference(),
                        event.getType());
        }

        private boolean connect() {
            final ServiceReference<ManagedResourceConnector<?>> connectorRef = ManagedResourceConnectorClient.getResourceConnector(context, resourceName);
            if (connectorRef != null) {
                processResourceConnector(
                        connectorRef,
                        ServiceEvent.REGISTERED);
                return true;
            }
            else return false;
        }

        /**
         * Releases reference to the resource connector.
         */
        public void close(){
            if(resourceConnector != null)
                resourceConnector.release(context);
            resourceConnector = null;
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

    private final KeyedObjects<String, ManagedResourceConnectorConsumer> connectors;
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
        connectors = createConnectors();
    }

    private static KeyedObjects<String, ManagedResourceConnectorConsumer> createConnectors(){
        return new AbstractKeyedObjects<String, ManagedResourceConnectorConsumer>(10) {
            private static final long serialVersionUID = -326619927154548260L;

            @Override
            public String getKey(final ManagedResourceConnectorConsumer item) {
                return item.resourceName;
            }
        };
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

    private void populateResources(final BundleContext context,
                                   final Consumer<ManagedResourceConnectorConsumer, ? extends Exception> configHandler) throws Exception {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdmin = new ServiceReferenceHolder<>(context,
                ConfigurationAdmin.class);
        try {
            PersistentConfigurationManager.forEachResource(configAdmin.getService(), new RecordReader<String, ManagedResourceConfiguration, Exception>() {
                @Override
                public void read(final String resourceName, final ManagedResourceConfiguration resourceConfig) throws Exception {
                    final ManagedResourceConnectorConsumer consumer;
                    if (connectors.containsKey(resourceName))
                        consumer = connectors.get(resourceName);
                    else
                        connectors.put(consumer = new ManagedResourceConnectorConsumer(context, resourceName, resourceConfig));
                    if (consumer.isReferenced() || consumer.connect())
                        configHandler.accept(consumer);
                    else
                        connectors.remove(consumer.resourceName);
                }
            });
        } finally {
            configAdmin.release(context);
        }
    }

    /**
     * Populates the model with management attributes.
     * <p>
     *     This method extracts management attributes via managed resource connectors
     *     and put them into the model. If managed resource connector doesn't support
     *     {@link com.itworks.snamp.connectors.attributes.AttributeSupport} interface
     *     then it will be ignore and management attributes will not be added into the model.
     *     It is recommended to call this method inside of {@link #start(java.util.Map)} method.
     * </p>
     * @param attributesModel The model to be populated. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException attributesModel is {@literal null}.
     * @throws javax.management.JMException Internal resource connector error.
     * @throws java.lang.Exception Internal adapter error
     */
    @ThreadSafe(true)
    protected final void populateModel(final AttributesModel attributesModel) throws Exception {
        if (attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        else
            populateResources(getBundleContextByObject(this), new Consumer<ManagedResourceConnectorConsumer, JMException>() {
                @Override
                public void accept(final ManagedResourceConnectorConsumer consumer) throws JMException{
                    if (consumer.isAttributesSupported())
                        enlargeModel(consumer, attributesModel);
                    else if(consumer.isReferenced()) try (final OSGiLoggingContext logger = getLoggingContext()) {
                        logger.info(String.format("Managed resource connector %s (connection string %s) doesn't support attributes. Context: %s",
                                consumer.resourceConfiguration.getConnectionType(),
                                consumer.resourceConfiguration.getConnectionString(),
                                LogicalOperation.current()));
                    }
                    else logConnectorNotExposed(consumer.resourceConfiguration.getConnectionType(), consumer.resourceName);
                }
            });
    }

    private void logConnectorNotExposed(final String connectorType, final String resourceName){
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.log(Level.WARNING, String.format("Managed resource connector %s:%s is not exposed into OSGi environment. Context: %s. Stack trace: %s",
                    connectorType,
                    resourceName,
                    LogicalOperation.current(),
                    getStackTrace()));
        }
    }

    /**
     * Populates model with notifications and starts listening for incoming notifications.
     * <p>
     *     This method enables notifications via managed resource connectors and
     *     put notification metadata into the model. If managed resource connector
     *     doesn't support {@link com.itworks.snamp.connectors.notifications.NotificationSupport} interface
     *     then it will be ignored and notifications will not be added into the model.
     *     It is recommended to call this method inside {@link #start(java.util.Map)} method.
     * </p>
     * @param notificationsModel The model to populate. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException notificationsModel is {@literal null}.
     * @throws javax.management.JMException Internal resource connector error.
     * @throws java.lang.Exception Internal adapter error.
     */
    protected final void populateModel(final NotificationsModel notificationsModel) throws Exception{
        if (notificationsModel == null) throw new IllegalArgumentException("notificationsModel is null.");
        populateResources(getBundleContextByObject(this), new Consumer<ManagedResourceConnectorConsumer, JMException>() {
            @Override
            public void accept(final ManagedResourceConnectorConsumer consumer) throws JMException{
                if (consumer.isNotificationsSupported())
                    enlargeModel(consumer, notificationsModel);
                else if(consumer.isReferenced())
                    try(final OSGiLoggingContext logger = getLoggingContext()){
                        logger.info(String.format("Managed resource connector %s (connection string %s) doesn't support notifications. Context: %s",
                                consumer.resourceConfiguration.getConnectionType(),
                                consumer.resourceConfiguration.getConnectionString(),
                                LogicalOperation.current()));
                    }
                else logConnectorNotExposed(consumer.resourceConfiguration.getConnectionType(), consumer.resourceName);
            }
        });
    }

    /**
     * Removes all listeners, disable notifications and stops the listening for incoming notifications.
     * <p>
     *     It is recommended to call this method inside of {@link #stop()} method.
     * </p>
     * @param notificationsModel The model to release. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException notificationsModel is {@literal null}.
     */
    protected final void clearModel(final NotificationsModel notificationsModel) {
        if (notificationsModel == null) throw new IllegalArgumentException("notificationsModel is null.");
        for(final ManagedResourceConnectorConsumer consumer: connectors.values())
            clearModel(consumer, notificationsModel);
        notificationsModel.clear();
    }

    /**
     * Removes all attributes from the model and disconnect each attribute from the managed
     * resource connector.
     * <p>
     *     It is recommended to call this method inside of {@link #stop()} method.
     * </p>
     * @param attributesModel The model to release. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException attributesModel is {@literal null}.
     */
    protected final void clearModel(final AttributesModel attributesModel) {
        if (attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        for (final ManagedResourceConnectorConsumer consumer : connectors.values())
            clearModel(consumer, attributesModel);
        attributesModel.clear();
    }

    /**
     * Starts the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param parameters Adapter startup parameters.
     * @throws java.lang.Exception Unable to start adapter.
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)
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
    protected void update(final Map<String, String> current,
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

    final synchronized boolean tryStart(final String adapterName, final Map<String, String> params) throws Exception {
        return this.listener.set(createListener(adapterName)) && tryStart(params);
    }

    private boolean tryStart(final Map<String, String> params) throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case CREATED:
            case STOPPED:
                InternalState newState = currentState.setParameters(params);
                start(newState.parameters);
                mutableState = newState.setAdapterState(AdapterState.STARTED);
                adapterStarted();
                return true;
            default:
                return false;
        }
    }

    private boolean tryStop() throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case STARTED:
                try {
                    stop();
                }
                finally {
                    disconnect();
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
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)
     */
    protected abstract void stop() throws Exception;

    private void restart() {
        try (@Temporary final LogicalOperation ignored =
                     AdapterLogicalOperation.restarting(adapterInstanceName)) {
            try {
                tryStop();
            } catch (final Exception e) {
                failedToStopAdapter(Level.SEVERE, e);
            }
            try {
                tryStart(mutableState.parameters);
            } catch (final Exception e) {
                disconnect();
                failedToStartAdapter(Level.SEVERE, e);
            }
        }
    }

    private void enlargeModel(final ManagedResourceConnectorConsumer resource,
                            final AttributesModel model) throws JMException{
        final Map<String, AttributeConfiguration> attributes = resource.resourceConfiguration.getElements(AttributeConfiguration.class);
        if(resource.isAttributesSupported() && attributes != null)
        for (final Map.Entry<String, AttributeConfiguration> entry : attributes.entrySet())
            model.addAttribute(resource.resourceName,
                    entry.getKey(),
                    entry.getValue().getAttributeName(),
                    new AttributeConnector(resource.getWeakAttributeSupport(), entry.getValue()));
    }

    /**
     * Propagates the attributes of newly connected resource to the model.
     * <p>
     *     It is recommended to call this method inside of overridden {@link #resourceAdded(String)} method.
     * </p>
     * @param resourceName The name of newly connected resource.
     * @param model The model to enlarge.
     * @throws javax.management.JMException Internal resource connector error.
     */
    protected final void enlargeModel(final String resourceName,
                                                      final AttributesModel model) throws JMException {
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if (consumer != null)
            enlargeModel(consumer, model);
    }

    private void enlargeModel(final ManagedResourceConnectorConsumer resource,
                              final NotificationsModel model) throws JMException {
        final Map<String, EventConfiguration> notifs = resource.resourceConfiguration.getElements(EventConfiguration.class);
        if(notifs != null && resource.isNotificationsSupported()) {
            for (final Map.Entry<String, EventConfiguration> entry : notifs.entrySet())
                model.addNotification(resource.resourceName,
                        entry.getKey(),
                        entry.getValue().getCategory(),
                        new NotificationConnector(resource.getWeakNotificationSupport(), entry.getValue()));
            if(!model.isEmpty())
                resource.getWeakNotificationSupport().addNotificationListener(model, null, null);
        }
    }

    protected final void enlargeModel(final String resourceName,
                                      final NotificationsModel model) throws JMException {
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if (consumer != null)
            enlargeModel(consumer, model);
    }

    private void clearModel(final ManagedResourceConnectorConsumer resource,
                            final AttributesModel model){
        final Map<String, AttributeConfiguration> disconnectedAttrs = resource.resourceConfiguration.getElements(AttributeConfiguration.class);
        if(disconnectedAttrs != null)
            for(final Map.Entry<String, AttributeConfiguration> entry: disconnectedAttrs.entrySet()){
                final AttributeAccessor accessor = model.removeAttribute(resource.resourceName, entry.getKey(), entry.getValue().getAttributeName());
                if(accessor != null) accessor.disconnect();
            }
    }

    /**
     * Disconnects attributes from model.
     * @param resourceName The name of the managed resource which attributes should be disconnected.
     * @param model The model to update. Cannot be {@literal null}.
     * @see #resourceRemoved(String)
     */
    protected final void clearModel(final String resourceName, final AttributesModel model){
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if(consumer != null) clearModel(consumer, model);
    }

    private void clearModel(final ManagedResourceConnectorConsumer resource,
                            final NotificationsModel model){
        final Map<String, EventConfiguration> disconnectedEvents = resource.resourceConfiguration.getElements(EventConfiguration.class);
        if(disconnectedEvents != null && resource.isNotificationsSupported()){
            final NotificationSupport notifs = resource.getWeakNotificationSupport();
            for(final Map.Entry<String, EventConfiguration> entry: disconnectedEvents.entrySet()){
                final MBeanNotificationInfo metadata = model.removeNotification(resource.resourceName,
                        entry.getKey(),
                        entry.getValue().getCategory());
                if(metadata != null)
                    for(final String notificationID: metadata.getNotifTypes())
                        notifs.disableNotifications(notificationID);
            }
            if(model.isEmpty())
                try {
                    notifs.removeNotificationListener(model);
                } catch (final ListenerNotFoundException e) {
                    try (final OSGiLoggingContext context = getLoggingContext()) {
                        context.log(Level.WARNING,
                                String.format("Failed to disable notifications for %s resource. Context: %s",
                                        resource.resourceName,
                                        LogicalOperation.current()), e);
                    }
                }
        }
    }

    /**
     * Disables events in the model.
     * @param resourceName The name of the managed resource which events should be disabled.
     * @param model The model to update. Cannot be {@literal null}.
     * @see #resourceRemoved(String)
     */
    protected final void clearModel(final String resourceName, final NotificationsModel model) {
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if (consumer != null)
            clearModel(consumer, model);
    }

    /**
     * Invokes when resource connector is in stopping state or resource configuration was removed.
     * <p>
     *     This method will be called automatically by SNAMP infrastructure.
     *     In the default implementation this method throws internal exception
     *     derived from {@link java.lang.UnsupportedOperationException} indicating
     *     that the adapter should be restarted.
     *     It is recommended to use {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)}
     *     and or {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)} to
     *     update your underlying models.
     * </p>
     * @param resourceName The name of the resource to be removed.
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.NotificationsModel)
     */
    protected void resourceRemoved(final String resourceName){
        throw new UnsupportedResourceRemovedOperation(resourceName);
    }

    /**
     * Invokes when a new resource connector is activated or new resource configuration is added.
     * <p>
     *     This method will be called automatically by SNAMP infrastructure.
     *     In the default implementation this method throws internal exception
     *     derived from {@link java.lang.UnsupportedOperationException} indicating
     *     that the adapter should be restarted.
     * </p
     * @param resourceName The name of the resource to be added.
     * @see #enlargeModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AttributesModel)
     */
    protected void resourceAdded(final String resourceName){
        throw new UnsupportedResourceAddedOperation(resourceName);
    }

    private void resourceRemovedImpl(final String resourceName) {
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        boolean restartRequired = false;
        if (consumer != null)
            try {
                resourceRemoved(consumer.resourceName);
            } catch (final UnsupportedResourceRemovedOperation ignored) {
                restartRequired = true;
            } finally {
                consumer.close();
                connectors.remove(consumer.resourceName);
            }
        if (restartRequired)
            restart();
    }

    private void resourceAddedImpl(final String resourceName,
                                   final ServiceReference<ManagedResourceConnector<?>> connectorRef) {
        boolean restartRequired = true;
        ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if (consumer == null) try{
            final ManagedResourceConfiguration config = ManagedResourceConnectorClient.getResourceConfiguration(getBundleContextByObject(this), connectorRef);
            if (config != null) {
                consumer = new ManagedResourceConnectorConsumer(getBundleContextByObject(this),
                        resourceName, config);
                consumer.connect();
                connectors.put(consumer);
                try {
                    resourceAdded(consumer.resourceName);
                    restartRequired = false;
                } catch (final UnsupportedResourceAddedOperation ignored) {
                    restartRequired = true;
                }
            } else restartRequired = false;
        }
        catch (final IOException e){
            try (final OSGiLoggingContext logger = getLoggingContext()) {
                logger.log(Level.WARNING, String.format("Unable to read configuration of newly added resource %s. Context: %s",
                        resourceName,
                        LogicalOperation.current()), e);
            }
        }
        else if (!consumer.isReferenced()) {
            consumer.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, connectorRef));
            if (consumer.isReferenced())
                try {
                    resourceAdded(consumer.resourceName);
                    restartRequired = false;
                } catch (final UnsupportedResourceAddedOperation ignored) {
                    restartRequired = true;
                }
            else restartRequired = false;
        }
        if (restartRequired)
            restart();
    }

    /**
     * Captures reference to the managed resource connectors.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public final synchronized void serviceChanged(final ServiceEvent event) {
        if (ManagedResourceConnectorClient.isResourceConnector(event.getServiceReference()))
            try (final LogicalOperation ignored = AdapterLogicalOperation.connectorChangesDetected(adapterInstanceName)) {
                @SuppressWarnings("unchecked")
                final ServiceReference<ManagedResourceConnector<?>> connectorRef = (ServiceReference<ManagedResourceConnector<?>>) event.getServiceReference();
                final String resourceName = ManagedResourceConnectorClient.getManagedResourceName(connectorRef);
                switch (event.getType()) {
                    case ServiceEvent.UNREGISTERING:
                        resourceRemovedImpl(resourceName);
                        return;
                    case ServiceEvent.REGISTERED:
                        resourceAddedImpl(resourceName, connectorRef);
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

    private void disconnect() {
        for (final ManagedResourceConnectorConsumer consumer : connectors.values())
            consumer.close();
        connectors.clear();
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

    private OSGiLoggingContext getLoggingContext(){
        return OSGiLoggingContext.get(getLogger(), getBundleContextByObject(this));
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
     * Reports an error when starting adapter.
     * @param logLevel Logging level.
     * @param e The failure reason.
     */
    protected void failedToStartAdapter(final Level logLevel, final Exception e) {
        try (final OSGiLoggingContext context = getLoggingContext()) {
            context.log(logLevel,
                    String.format("Failed to start resource adapter %s. Context: %s",
                            adapterInstanceName, LogicalOperation.current()), e);
        }
    }

    /**
     * Reports an error when stopping adapter.
     * @param logLevel Logging level.
     * @param e The failure reason.
     */
    protected void failedToStopAdapter(final Level logLevel, final Exception e){
        try(final OSGiLoggingContext context = getLoggingContext()) {
            context.log(logLevel, String.format("Failed to stop resource adapter %s. Context: %s",
                    adapterInstanceName, LogicalOperation.current()), e);
        }
    }

    /**
     * Gets set of hosted resources.
     * @return A set of hosted resources.
     */
    protected final Set<String> getHostedResources() {
        return connectors.keySet();
    }

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
