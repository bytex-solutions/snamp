package com.bytex.snamp.gateway;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.ResourceEvent;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AttributeAddedEvent;
import com.bytex.snamp.connector.attributes.AttributeRemovingEvent;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationAddedEvent;
import com.bytex.snamp.connector.notifications.NotificationRemovingEvent;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationAddedEvent;
import com.bytex.snamp.connector.operations.OperationRemovingEvent;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import javax.annotation.concurrent.Immutable;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents a base class for constructing custom gateway.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractGateway extends AbstractAggregator implements Gateway, ResourceEventListener{
    private static final class GatewayLoggingScope extends LoggingScope {

        private GatewayLoggingScope(final AbstractGateway requester,
                                    final String operationName){
            super(requester, operationName);
        }

        private static GatewayLoggingScope connectorChangesDetected(final AbstractGateway requester) {
            return new GatewayLoggingScope(requester, "processResourceConnectorChanges");
        }
    }

    @Immutable
    private static final class InternalState {
        private final ImmutableMap<String, String> parameters;
        private final GatewayState state;

        private InternalState(final GatewayState state, final ImmutableMap<String, String> params) {
            this.state = state;
            this.parameters = params;
        }

        private static InternalState initialState() {
            return new InternalState(GatewayState.CREATED, ImmutableMap.of());
        }

        private InternalState setParameters(final Map<String, String> value) {
            return new InternalState(state, ImmutableMap.copyOf(value));
        }

        private InternalState transition(final GatewayState value) {
            return new InternalState(value, parameters);
        }

        private static InternalState finalState() {
            return null;
        }

        private boolean parametersAreEqual(final Map<String, String> newParameters) {
            if (parameters.size() == newParameters.size()) {
                for (final Map.Entry<String, String> entry : newParameters.entrySet())
                    if (!Objects.equals(parameters.get(entry.getKey()), entry.getValue()))
                        return false;
                return true;
            } else return false;
        }

        private boolean equals(final InternalState other) {
            return state.equals(other.state) && parametersAreEqual(other.parameters);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof InternalState && equals((InternalState) other);
        }

        @Override
        public String toString() {
            return state.toString();
        }

        @Override
        public int hashCode() {
            return state.hashCode() & parameters.hashCode();
        }
    }

    /**
     * Represents base implementation of {@link Gateway.FeatureBindingInfo} interface.
     * @param <M> Type of the feature.
     */
    protected static abstract class AbstractFeatureBindingInfo<M extends MBeanFeatureInfo> implements FeatureBindingInfo<M>{
        private final M metadata;

        protected AbstractFeatureBindingInfo(final M metadata){
            this.metadata = Objects.requireNonNull(metadata);
        }

        /**
         * Gets metadata of the feature as it is supplied by connected resources.
         *
         * @return The metadata of the feature.
         */
        @Override
        public final M getMetadata() {
            return metadata;
        }
    }

    protected final static class ReadOnlyFeatureBindingInfo<M extends MBeanFeatureInfo> extends AbstractFeatureBindingInfo<M>{
        private final ImmutableMap<String, ?> properties;

        public ReadOnlyFeatureBindingInfo(final FeatureAccessor<M> accessor,
                                          final Map<String, ?> advancedProps) {
            super(accessor.getMetadata());
            properties = ImmutableMap.<String, Object>builder()
                    .putAll(advancedProps)
                    .putAll(DescriptorUtils.toMap(accessor.get().getDescriptor(), true))
                    .build();
        }

        public ReadOnlyFeatureBindingInfo(final FeatureAccessor<M> accessor){
            this(accessor, ImmutableMap.<String, String>of());
        }

        public ReadOnlyFeatureBindingInfo(final FeatureAccessor<M> accessor,
                                          final String propertyName,
                                          final Object propertyValue){
            this(accessor, ImmutableMap.of(propertyName, propertyValue));
        }

        public ReadOnlyFeatureBindingInfo(final FeatureAccessor<M> accessor,
                                          final String property1,
                                          final Object value1,
                                          final String property2,
                                          final Object value2){
            this(accessor, ImmutableMap.of(property1, value1, property2, value2));
        }

        /**
         * Gets binding property such as URL, OID or any other information
         * describing how this feature is exposed to the outside world.
         *
         * @param propertyName The name of the binding property.
         * @return The value of the binding property.
         */
        @Override
        public Object getProperty(final String propertyName) {
            return properties.get(propertyName);
        }

        /**
         * Gets all supported properties.
         *
         * @return A set of all supported properties.
         */
        @Override
        public ImmutableSet<String> getProperties() {
            return properties.keySet();
        }

        /**
         * Always return {@literal false}.
         *
         * @param propertyName The name of the property to change.
         * @param value        A new property value.
         * @return {@literal true}, if the property supports modification and changed successfully; otherwise, {@literal false}.
         */
        @Override
        public boolean setProperty(final String propertyName, final Object value) {
            return false;
        }
    }

    private volatile InternalState mutableState;
    private final String instanceName;

    /**
     * Initializes a new instance of gateway.
     * @param instanceName The name of the gateway instance.
     */
    protected AbstractGateway(final String instanceName) {
        this.instanceName = instanceName;
        mutableState = InternalState.initialState();
    }

    /**
     * Gets name of this gateway instance.
     * @return The name of the gateway instance.
     */
    @Override
    public final String getInstanceName(){
        return instanceName;
    }

    /**
     * Gets state of this gateway.
     * @return The state of this gateway.
     */
    @Override
    public final GatewayState getState(){
        final InternalState current = mutableState;
        return current != null ? current.state : GatewayState.CLOSED;
    }

    private void attributeAdded(final AttributeAddedEvent event){
        final FeatureAccessor<MBeanAttributeInfo> accessor =
                addFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    private void attributeRemoved(final AttributeRemovingEvent event){
        final FeatureAccessor<MBeanAttributeInfo> accessor =
                removeFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    private void notificationAdded(final NotificationAddedEvent event){
        final FeatureAccessor<MBeanNotificationInfo> accessor =
                addFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    private void notificationRemoved(final NotificationRemovingEvent event){
        final FeatureAccessor<MBeanNotificationInfo> accessor = removeFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    private void operationAdded(final OperationAddedEvent event){
        final FeatureAccessor<MBeanOperationInfo> accessor =
                addFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    private void operationRemoved(final OperationRemovingEvent event){
        final FeatureAccessor<MBeanOperationInfo> accessor = removeFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.processEvent(event);
    }

    /**
     * Handles resource event.
     *
     * @param event An event to handle.
     * @see com.bytex.snamp.connector.FeatureAddedEvent
     * @see com.bytex.snamp.connector.FeatureRemovingEvent
     */
    @Override
    public final void handle(final ResourceEvent event) {
        if(event instanceof AttributeAddedEvent)
            attributeAdded((AttributeAddedEvent) event);
        else if(event instanceof AttributeRemovingEvent)
            attributeRemoved((AttributeRemovingEvent)event);
        else if(event instanceof NotificationAddedEvent)
            notificationAdded((NotificationAddedEvent)event);
        else if(event instanceof NotificationRemovingEvent)
            notificationRemoved((NotificationRemovingEvent)event);
        else if(event instanceof OperationAddedEvent)
            operationAdded((OperationAddedEvent)event);
        else if(event instanceof OperationRemovingEvent)
            operationRemoved((OperationRemovingEvent)event);
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource extended
     * with the specified feature.
     * @param resourceName The name of the managed resource.
     * @param feature A new feature of the managed resource.
     * @param <M> Type of the managed resource feature.
     * @return A new instance of the resource feature accessor. May be {@literal null}.
     * @see AttributeAccessor
     * @see NotificationAccessor
     */
    protected abstract <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName,
                                       final M feature) throws Exception;

    private <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeatureImpl(final String resourceName,
                                                                             final M feature){
        try {
            return addFeature(resourceName, feature);
        } catch (final Exception e) {
            failedToAddFeature(resourceName, feature, e);
            return null;
        }
    }

    private void failedToAddFeature(final String resourceName, final MBeanFeatureInfo feature, final Exception e) {
        getLogger().log(Level.WARNING, String.format("Failed to add %s resource feature %s", resourceName, feature), e);
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource
     * was removed from SNAMP.
     * @param resourceName The name of the resource.
     * @return Read-only collection of features tracked by this gateway. Cannot be {@literal null}.
     */
    protected abstract Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception;

    private Stream<? extends FeatureAccessor<?>> removeAllFeaturesImpl(final String resourceName){
        try {
            return removeAllFeatures(resourceName);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, String.format("Failed to remove %s resource features", resourceName), e);
            return Stream.empty();
        }
    }

    /**
     *
     * Invokes automatically by SNAMP infrastructure when the feature was removed
     * from the specified resource.
     * @param resourceName The name of the managed resource.
     * @param feature The resource feature that was removed.
     * @param <M> The type of the resource feature.
     * @return An instance of the feature accessor used by this gateway. May be {@literal null}.
     */
    protected abstract <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName,
                                                                                        final M feature) throws Exception;

    private <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeatureImpl(final String resourceName,
                                                                             final M feature){
        try {
            return removeFeature(resourceName, feature);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, String.format("Failed to remove %s resource feature %s", resourceName, feature), e);
            return null;
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    /**
     * Starts the gateway instance.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param parameters Gateway startup parameters.
     * @throws java.lang.Exception Unable to gateway instance.
     */
    protected abstract void start(final Map<String, String> parameters) throws Exception;

    /**
     * Updates this gateway with a new configuration parameters.
     * <p>
     *     In the default implementation this method causes restarting
     *     of this gateway instance that affects availability of the gateway.
     *     You should override this method if custom gateway
     *     supports soft update (without affecting availability).
     * </p>
     * @param current The current configuration parameters.
     * @param newParameters A new configuration parameters.
     * @throws Exception Unable to update this gateway.
     */
    protected void update(final Map<String, String> current,
                          final Map<String, String> newParameters) throws Exception {
        if (!current.equals(newParameters)) {
            tryStop();
            tryStart(newParameters);
        }
    }

    final synchronized boolean tryUpdate(final Map<String, String> newParameters) throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case STARTED:
                //compare parameters
                if(!currentState.parametersAreEqual(newParameters)) {
                    final InternalState newState = currentState.setParameters(newParameters);
                    update(currentState.parameters, newState.parameters);
                    mutableState = newState;
                    return true;
                }
                else return false;
            default:
                return false;
        }
    }

    private static GatewayUpdatedCallback gatewayUpdatedNotifier(final AbstractGateway gatewayInstance) {
        final class GatewayUpdatedCallbackImpl extends WeakReference<AbstractGateway> implements GatewayUpdatedCallback {
            private GatewayUpdatedCallbackImpl() {
                super(gatewayInstance);
            }

            @Override
            public void updated() {
                final AbstractGateway gateway = get();
                if (gateway != null)
                    GatewayEventBus.notifyInstanceUpdated(gateway.getGatewayType(), gateway);
            }
        }

        return new GatewayUpdatedCallbackImpl();
    }

    /**
     * Begin or prolong updating the internal structure of this gateway.
     * @param manager The update manager.
     * @param callback The callback used to notify about ending of the updating process.
     */
    protected final void beginUpdate(final GatewayUpdateManager manager,
                                     GatewayUpdatedCallback callback) {
        if (callback == null)
            callback = gatewayUpdatedNotifier(this);
        else
            callback = GatewayUpdateManager.combineCallbacks(callback, gatewayUpdatedNotifier(this));
        if (manager.beginUpdate(callback))
            GatewayEventBus.notifyInstanceUpdating(getGatewayType(), this);
    }

    /**
     * Begin or prolong updating the internal structure of this gateway.
     * @param manager The updating manager.
     */
    protected final void beginUpdate(final GatewayUpdateManager manager){
        beginUpdate(manager, null);
    }

    private synchronized void addResource(final ServiceReference<ManagedResourceConnector> resourceRef) {
        final String resourceName = ManagedResourceConnectorClient.getManagedResourceName(resourceRef);
        final BundleContext context = getBundleContext();
        final ManagedResourceConnector connector = context.getService(resourceRef);
        if (connector != null)
            try {
                //add gateway as a listener
                connector.addResourceEventListener(this);
                //expose all features
                final AttributeSupport attributeSupport = connector.queryObject(AttributeSupport.class);
                if(attributeSupport != null)
                    Arrays.stream(attributeSupport.getAttributeInfo()).forEach(metadata -> attributeAdded(new AttributeAddedEvent(attributeSupport, resourceName, metadata)));
                final NotificationSupport notificationSupport = connector.queryObject(NotificationSupport.class);
                if(notificationSupport != null)
                    Arrays.stream(notificationSupport.getNotificationInfo()).forEach(metadata -> notificationAdded(new NotificationAddedEvent(notificationSupport, resourceName, metadata)));
                final OperationSupport operationSupport = connector.queryObject(OperationSupport.class);
                if(operationSupport != null)
                    Arrays.stream(operationSupport.getOperationInfo()).forEach(metadata -> operationAdded(new OperationAddedEvent(operationSupport, resourceName, metadata)));
            } finally {
                context.ungetService(resourceRef);
            }
    }

    private synchronized void removeResource(final ServiceReference<ManagedResourceConnector> resourceRef){
        final String resourceName = ManagedResourceConnectorClient.getManagedResourceName(resourceRef);
        final BundleContext context = getBundleContext();
        final ManagedResourceConnector connector = context.getService(resourceRef);
        if(connector != null)
            try{
                connector.removeResourceEventListener(this);
                removeAllFeaturesImpl(resourceName).forEach(FeatureAccessor::close);
            }
            finally {
                context.ungetService(resourceRef);
            }
    }

    final synchronized boolean tryStart(final Map<String, String> params) throws Exception{
        final InternalState currentState = mutableState;
        switch (currentState.state){
            case CREATED:
            case STOPPED:
                //explore all available resources
                final Collection<ServiceReference<ManagedResourceConnector>> resources =
                        getBundleContext().getServiceReferences(ManagedResourceConnector.class, null);
                resources.forEach(this::addResource);
                InternalState newState = currentState.setParameters(params);
                start(newState.parameters);
                mutableState = newState.transition(GatewayState.STARTED);
                started();
                ManagedResourceConnectorClient.addResourceListener(getBundleContext(), this);
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
                    resources.forEach(this::removeResource);
                }
                finally {
                    getBundleContext().removeServiceListener(this);
                    mutableState = currentState.transition(GatewayState.STOPPED);
                }
                stopped();
                return true;
            default:
                return false;
        }
    }

    /**
     * Stops the gateway instance.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @throws java.lang.Exception Unable to stop gateway instance.
     */
    protected abstract void stop() throws Exception;

    /**
     * Captures reference to the managed resource connector.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        if (ManagedResourceConnector.isResourceConnector(event.getServiceReference()))
            try (final LoggingScope logger = GatewayLoggingScope.connectorChangesDetected(this)) {
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
                        logger.info(String.format("Unexpected event %s captured by gateway %s for resource %s",
                                event.getType(),
                                instanceName,
                                        resourceName));
                }
            }
    }

    /**
     * Releases all resources associated with this gateway.
     * @throws java.io.IOException An exception occurred during gateway releasing.
     */
    @Override
    public final void close() throws IOException {
        try {
            tryStop();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(String.format("Unable to release resources associated with %s gateway instance", instanceName), e);
        } finally {
            mutableState = InternalState.finalState();
            clearCache();
        }
    }

    private void started(){
        GatewayEventBus.notifyInstanceStarted(getGatewayType(), this);
    }

    private void stopped(){
        GatewayEventBus.notifyInstanceStopped(getGatewayType(), this);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * Returns a string representation of this gateway instance.
     * @return A string representation of this gateway instance.
     */
    @Override
    public String toString() {
        return instanceName;
    }

    public final String getGatewayType() {
        return Gateway.getGatewayType(getClass());
    }

    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        return ImmutableMultimap.of();
    }

    protected static <TAccessor extends AttributeAccessor & FeatureBindingInfo<MBeanAttributeInfo>> Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getBindings(final AttributeSet<TAccessor> model){
        final Multimap<String, TAccessor> result = HashMultimap.create();
        model.forEachAttribute(result::put);
        return result;
    }

    protected static <TAccessor extends NotificationAccessor & FeatureBindingInfo<MBeanNotificationInfo>> Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getBindings(final NotificationSet<TAccessor> model){
        final Multimap<String, TAccessor> result = HashMultimap.create();
        model.forEachNotification(result::put);
        return result;
    }

    protected static <TAccessor extends OperationAccessor & FeatureBindingInfo<MBeanOperationInfo>> Multimap<String, ? extends FeatureBindingInfo<MBeanOperationInfo>> getBindings(final OperationSet<TAccessor> model){
        final Multimap<String, TAccessor> result = HashMultimap.create();
        model.forEachOperation(result::put);
        return result;
    }
}
