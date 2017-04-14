package com.bytex.snamp.gateway;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.*;
import com.bytex.snamp.connector.attributes.AttributeModifiedEvent;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationModifiedEvent;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationModifiedEvent;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.collect.*;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.WillNotClose;
import javax.management.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Represents a base class for constructing custom gateway.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractGateway extends AbstractStatefulFrameworkServiceTracker<ManagedResourceConnector, ManagedResourceConnectorClient, Map<String, String>> implements Gateway, ResourceEventListener{
    @FunctionalInterface
    private interface FeatureModifiedEventFactory<S, F extends MBeanFeatureInfo>{
        FeatureModifiedEvent<F> createEvent(final S sender,
                                            final String resourceName,
                                            final F feature);
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
        super(ManagedResourceConnector.class, new InternalState<>(ImmutableMap.of()));
        this.instanceName = instanceName;
        gatewayType = Gateway.getGatewayType(getClass()).intern();
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

    /**
     * Starts the gateway instance.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param parameters Gateway startup parameters.
     * @throws java.lang.Exception Unable start to gateway instance.
     */
    @Override
    protected abstract void start(final Map<String, String> parameters) throws Exception;

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

    @Override
    protected final String getServiceId(final ManagedResourceConnectorClient client) {
        return client.getManagedResourceName();
    }

    /**
     * Returns filter used to query services from OSGi Service Registry.
     *
     * @return A filter used to query services from OSGi Service Registry.
     */
    @Nonnull
    @Override
    protected ManagedResourceFilterBuilder createServiceFilter() {
        return ManagedResourceConnectorClient.filterBuilder();
    }

    @Nonnull
    @Override
    protected final ManagedResourceConnectorClient createClient(final ServiceReference<ManagedResourceConnector> serviceRef) throws InstanceNotFoundException {
        return new ManagedResourceConnectorClient(getBundleContext(), serviceRef);
    }

    @OverridingMethodsMustInvokeSuper
    protected void addResource(final String resourceName, @WillNotClose final ManagedResourceConnector connector){
        //add gateway as a listener
        connector.addResourceEventListener(this);
        //expose all features
        exposeFeatures(resourceName, connector, AttributeSupport.class, AttributeSupport::getAttributeInfo, AttributeModifiedEvent::attributedAdded);
        exposeFeatures(resourceName, connector, OperationSupport.class, OperationSupport::getOperationInfo, OperationModifiedEvent::operationAdded);
        exposeFeatures(resourceName, connector, NotificationSupport.class, NotificationSupport::getNotificationInfo, NotificationModifiedEvent::notificationAdded);
    }

    @Override
    protected final synchronized void addService(@WillNotClose final ManagedResourceConnectorClient connector) {
        final String resourceName = getServiceId(connector);
        if (trackedServices.contains(resourceName))
            getLogger().info(String.format("Resource %s is already attached to gateway %s", resourceName, instanceName));
        else
            addResource(resourceName, connector);
    }

    @OverridingMethodsMustInvokeSuper
    protected void removeResource(final String resourceName, @WillNotClose final ManagedResourceConnector connector){
        connector.removeResourceEventListener(this);
        removeAllFeaturesImpl(resourceName).forEach(FeatureAccessor::close);
    }

    @Override
    protected final synchronized void removeService(@WillNotClose final ManagedResourceConnectorClient connector) {
        final String resourceName = getServiceId(connector);
        if (trackedServices.contains(resourceName))
            removeResource(resourceName, connector);
        else
            getLogger().info(String.format("Resource %s is already detached from gateway %s", resourceName, instanceName));
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
     * Releases all resources associated with this gateway.
     * @throws java.io.IOException An exception occurred during gateway releasing.
     */
    @Override
    public final void close() throws IOException {
        try {
            super.close();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(String.format("Unable to release resources associated with %s gateway instance", instanceName), e);
        }
    }

    @Override
    protected final void started(){
        GatewayEventBus.notifyInstanceStarted(gatewayType, this);
    }

    @Override
    protected final void stopped(){
        GatewayEventBus.notifyInstanceStopped(gatewayType, this);
    }
    
    /**
     * Returns a string representation of this gateway instance.
     * @return A string representation of this gateway instance.
     */
    @Override
    public String toString() {
        return gatewayType + ':' + instanceName;
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
