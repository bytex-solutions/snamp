package com.bytex.snamp.gateway;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.connector.*;
import com.bytex.snamp.connector.attributes.AttributeModifiedEvent;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationModifiedEvent;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationModifiedEvent;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.FrameworkServiceState;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
public abstract class AbstractGateway extends AbstractAggregator implements Gateway, ResourceEventListener, ServiceListener{
    @FunctionalInterface
    private interface FeatureModifiedEventFactory<S, F extends MBeanFeatureInfo>{
        FeatureModifiedEvent<F> createEvent(final S sender,
                                            final String resourceName,
                                            final F feature);
    }

    private static final class GatewayLoggingScope extends LoggingScope {

        private GatewayLoggingScope(final AbstractGateway requester,
                                    final String operationName){
            super(requester, operationName);
        }

        static GatewayLoggingScope connectorChangesDetected(final AbstractGateway requester) {
            return new GatewayLoggingScope(requester, "processResourceConnectorChanges");
        }
    }

    @Immutable
    private static final class InternalState {
        final ImmutableMap<String, String> parameters;
        final FrameworkServiceState state;

        private InternalState(final FrameworkServiceState state, final Map<String, String> params) {
            this.state = state;
            this.parameters = ImmutableMap.copyOf(params);
        }

        private InternalState(){
            this(FrameworkServiceState.CREATED, ImmutableMap.of());
        }

        InternalState setParameters(final Map<String, String> value) {
            return new InternalState(state, value);
        }

        InternalState transition(final FrameworkServiceState value) {
            switch (value) {
                case CLOSED:
                    return null;
                default:
                    return new InternalState(value, parameters);
            }
        }

        boolean parametersAreEqual(final Map<String, String> newParameters) {
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
            return Objects.hash(state, parameters);
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
    protected final String gatewayType;

    /**
     * Gets name of this instance.
     */
    protected final String instanceName;

    /**
     * Initializes a new instance of gateway.
     * @param instanceName The name of the gateway instance.
     */
    protected AbstractGateway(final String instanceName) {
        this.instanceName = instanceName;
        mutableState = new InternalState();
        gatewayType = Gateway.getGatewayType(getClass()).intern();
    }

    @Nonnull
    @Override
    public final Map<String, String> getConfiguration() {
        final InternalState currentState = mutableState;
        return currentState == null ? ImmutableMap.of() : currentState.parameters;
    }

    /**
     * Gets state of this gateway.
     * @return The state of this gateway.
     */
    @Override
    public final FrameworkServiceState getState(){
        final InternalState current = mutableState;
        return current != null ? current.state : FrameworkServiceState.CLOSED;
    }

    private <F extends MBeanFeatureInfo> void featureModified(final FeatureModifiedEvent<F> event){
        final FeatureAccessor<F> accessor;
        switch (event.getType()) {
            case ADDED:
                accessor = addFeatureImpl(event.getResourceName(), event.getFeature());
                break;
            case REMOVING:
                accessor = removeFeatureImpl(event.getResourceName(), event.getFeature());
                break;
            default:
                return;
        }
        if(accessor != null)
            accessor.processEvent(event);
    }

    /**
     * Handles resource event.
     *
     * @param event An event to handle.
     * @see com.bytex.snamp.connector.FeatureModifiedEvent
     */
    @Override
    public final void handle(final ResourceEvent event) {
        if (event instanceof AttributeModifiedEvent)
            featureModified((AttributeModifiedEvent) event);
        else if (event instanceof OperationModifiedEvent)
            featureModified((OperationModifiedEvent) event);
        else if (event instanceof NotificationModifiedEvent)
            featureModified((NotificationModifiedEvent) event);
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
     * @return {@literal true}, if the gateway is modified; otherwise, {@literal false}.
     * @throws Exception Unable to update this gateway.
     */
    protected boolean update(final Map<String, String> current,
                          final Map<String, String> newParameters) throws Exception {
        doStop();
        doStart(newParameters);
        return false;
    }

    @Override
    public final synchronized void update(@Nonnull final Map<String, String> newParameters) throws Exception {
        final InternalState currentState = mutableState;
        switch (currentState.state) {
            case CREATED:
                doStart(newParameters);
            case STARTED:
                //compare parameters
                if (!currentState.parametersAreEqual(newParameters) && update(currentState.parameters, newParameters))
                    mutableState = currentState.setParameters(newParameters);
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
                    GatewayEventBus.notifyInstanceUpdated(gateway.gatewayType, gateway);
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
            GatewayEventBus.notifyInstanceUpdating(gatewayType, this);
    }

    /**
     * Begin or prolong updating the internal structure of this gateway.
     * @param manager The updating manager.
     */
    protected final void beginUpdate(final GatewayUpdateManager manager){
        beginUpdate(manager, null);
    }

    private <S, F extends MBeanFeatureInfo> void exposeFeatures(final String resourceName,
                                                                final Aggregator connector,
                                                                final Class<S> supportType,
                                                                final Function<? super S, F[]> features,
                                                                final FeatureModifiedEventFactory<S, F> eventFactory){
        final S support = connector.queryObject(supportType);
        if(support != null)
            for(final F feature: features.apply(support))
                featureModified(eventFactory.createEvent(support, resourceName, feature));
    }

    @MethodStub
    protected void resourceAdded(final ManagedResourceConnectorClient resourceConnector){

    }

    private synchronized void addResource(final ManagedResourceConnectorClient connector) {
        resourceAdded(connector);
        //add gateway as a listener
        connector.addResourceEventListener(this);
        //expose all features
        final String resourceName = connector.getManagedResourceName();
        exposeFeatures(resourceName, connector, AttributeSupport.class, AttributeSupport::getAttributeInfo, AttributeModifiedEvent::attributedAdded);
        exposeFeatures(resourceName, connector, OperationSupport.class, OperationSupport::getOperationInfo, OperationModifiedEvent::operationAdded);
        exposeFeatures(resourceName, connector, NotificationSupport.class, NotificationSupport::getNotificationInfo, NotificationModifiedEvent::notificationAdded);
    }

    @MethodStub
    protected void resourceRemoved(final ManagedResourceConnectorClient resourceConnector){

    }

    private synchronized void removeResource(final ManagedResourceConnectorClient connector) {
        connector.removeResourceEventListener(this);
        removeAllFeaturesImpl(connector.getManagedResourceName()).forEach(FeatureAccessor::close);
        resourceRemoved(connector);
    }

    private synchronized void doStart(final Map<String, String> params) throws Exception {
        final InternalState currentState = mutableState;
        switch (currentState.state) {
            case CREATED:
            case STOPPED:
                //explore all available resources
                final BundleContext context = getBundleContext();
                for (final String resourceName : ManagedResourceConnectorClient.getResources(context)) {
                    final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                    if (client != null)
                        try {
                            addResource(client);
                        } finally {
                            client.release(context);
                        }
                }
                InternalState newState = currentState.setParameters(params);
                start(newState.parameters);
                mutableState = newState.transition(FrameworkServiceState.STARTED);
                started();
                ManagedResourceConnectorClient.filterBuilder().addServiceListener(context, this);
        }
    }

    private synchronized void doStop() throws Exception {
        final InternalState currentState = mutableState;
        switch (currentState.state) {
            case STARTED:
                try {
                    stop();
                    final BundleContext context = getBundleContext();
                    for (final String resourceName : ManagedResourceConnectorClient.getResources(context)) {
                        final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                        if (client != null)
                            try {
                                removeResource(client);
                            } finally {
                                client.release(context);
                            }
                    }
                } finally {
                    getBundleContext().removeServiceListener(this);
                    mutableState = currentState.transition(FrameworkServiceState.STOPPED);
                }
                stopped();
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
        if (ManagedResourceConnector.isResourceConnector(event.getServiceReference())) {
            final BundleContext context = getBundleContext();
            @SuppressWarnings("unchecked")
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, (ServiceReference<ManagedResourceConnector>) event.getServiceReference());
            try (final LoggingScope logger = GatewayLoggingScope.connectorChangesDetected(this)) {
                switch (event.getType()) {
                    case ServiceEvent.MODIFIED_ENDMATCH:
                    case ServiceEvent.UNREGISTERING:
                        removeResource(client);
                        return;
                    case ServiceEvent.REGISTERED:
                        addResource(client);
                        return;
                    default:
                        logger.info(String.format("Unexpected event %s captured by gateway %s for resource %s",
                                event.getType(),
                                instanceName,
                                client.getManagedResourceName()));
                }
            } finally {
                client.release(context);
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
            doStop();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(String.format("Unable to release resources associated with %s gateway instance", instanceName), e);
        } finally {
            mutableState = mutableState.transition(FrameworkServiceState.CLOSED);
            clearCache();
        }
    }

    private void started(){
        GatewayEventBus.notifyInstanceStarted(gatewayType, this);
    }

    private void stopped(){
        GatewayEventBus.notifyInstanceStopped(gatewayType, this);
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
