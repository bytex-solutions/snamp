package com.bytex.snamp.adapters;

import com.google.common.collect.*;
import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.adapters.modeling.*;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.ResourceEvent;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeAddedEvent;
import com.bytex.snamp.connectors.attributes.AttributeRemovingEvent;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.NotificationAddedEvent;
import com.bytex.snamp.connectors.notifications.NotificationRemovingEvent;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.operations.OperationAddedEvent;
import com.bytex.snamp.connectors.operations.OperationRemovingEvent;
import com.bytex.snamp.core.LogicalOperation;
import com.bytex.snamp.core.OSGiLoggingContext;
import com.bytex.snamp.core.RichLogicalOperation;
import com.bytex.snamp.internal.RecordReader;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.DescriptorUtils;
import org.osgi.framework.*;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

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
    private static final class ResourceAdapterUpdateNotifier extends WeakReference<AbstractResourceAdapter> implements ResourceAdapterUpdatedCallback {

        private ResourceAdapterUpdateNotifier(final AbstractResourceAdapter adapter) {
            super(adapter);
        }

        @Override
        public void updated() {
            final AbstractResourceAdapter adapter = get();
            if (adapter != null)
                ResourceAdapterEventBus.notifyAdapterUpdated(adapter.getAdapterName(), adapter);
        }
    }

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

        private boolean parametersAreEqual(final Map<String, String> newParameters) {
            if(parameters.size() == newParameters.size()) {
                for (final Map.Entry<String, String> entry : newParameters.entrySet())
                    if(!Objects.equals(parameters.get(entry.getKey()), entry.getValue()))
                        return false;
                return true;
            }
            else return false;
        }
    }

    /**
     * Represents base implementation of {@link com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo} interface.
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

    private InternalState mutableState;
    private final String adapterInstanceName;

    /**
     * Initializes a new resource adapter.
     * @param instanceName The name of the adapter instance.
     */
    protected AbstractResourceAdapter(final String instanceName) {
        this.adapterInstanceName = instanceName;
        mutableState = InternalState.initialState();
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
     * @see com.bytex.snamp.connectors.FeatureAddedEvent
     * @see com.bytex.snamp.connectors.FeatureRemovingEvent
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

    /**
     * Writes log that describes exception produced by {@link #addFeature(String, javax.management.MBeanFeatureInfo)}.
     * @param resourceName The name of the resource.
     * @param feature The resource feature.
     * @param e The exception.
     */
    protected void failedToAddFeature(final String resourceName, final MBeanFeatureInfo feature, final Exception e){
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.log(Level.WARNING, String.format("Failed to add %s resource feature %s", resourceName, feature), e);
        }
    }

    /**
     * Invokes automatically by SNAMP infrastructure when the specified resource
     * was removed from SNAMP.
     * @param resourceName The name of the resource.
     * @return Read-only collection of features tracked by this resource adapter. Cannot be {@literal null}.
     */
    protected abstract Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception;

    private Iterable<? extends FeatureAccessor<?>> removeAllFeaturesImpl(final String resourceName){
        try {
            return removeAllFeatures(resourceName);
        } catch (final Exception e) {
            failedToRemoveFeatures(resourceName, e);
            return ImmutableList.of();
        }
    }

    protected void failedToRemoveFeatures(final String resourceName, final Exception e){
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.log(Level.SEVERE, String.format("Failed to remove %s resource features", resourceName), e);
        }
    }

    /**
     *
     * Invokes automatically by SNAMP infrastructure when the feature was removed
     * from the specified resource.
     * @param resourceName The name of the managed resource.
     * @param feature The resource feature that was removed.
     * @param <M> The type of the resource feature.
     * @return An instance of the feature accessor used by this resource adapter. May be {@literal null}.
     */
    protected abstract <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName,
                                                                                        final M feature) throws Exception;

    private <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeatureImpl(final String resourceName,
                                                                             final M feature){
        try {
            return removeFeature(resourceName, feature);
        } catch (final Exception e) {
            failedToRemoveFeature(resourceName, feature, e);
            return null;
        }
    }

    protected void failedToRemoveFeature(final String resourceName,
                                         final MBeanFeatureInfo feature,
                                         final Exception e){
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.log(Level.SEVERE, String.format("Failed to remove %s resource feature %s", resourceName, feature), e);
        }
    }

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
     * @throws Exception Unable to update this adapter.
     */
    protected void update(final Map<String, String> current,
                          final Map<String, String> newParameters) throws Exception{
        if(!Utils.mapsAreEqual(current, newParameters))
            restart(newParameters);
    }

    /**
     * Restarts this resource adapter.
     * @param parameters A new parameters for adapter start.
     * @throws Exception Unable to restart adapter. This is inconsistent exception.
     */
    protected synchronized final void restart(final Map<String, String> parameters) throws Exception{
        tryStop();
        tryStart(parameters);
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

    /**
     * Begin or prolong updating the internal structure of this adapter.
     * @param manager The update manager.
     * @param callback The callback used to notify about ending of the updating process.
     */
    protected final void beginUpdate(final ResourceAdapterUpdateManager manager,
                                     ResourceAdapterUpdatedCallback callback) {
        if (callback == null)
            callback = new ResourceAdapterUpdateNotifier(this);
        else
            callback = ResourceAdapterUpdateManager.combineCallbacks(callback, new ResourceAdapterUpdateNotifier(this));
        if (manager.beginUpdate(callback))
            ResourceAdapterEventBus.notifyAdapterUpdating(getAdapterName(), this);
    }

    /**
     * Begin or prolong updating the internal structure of this adapter.
     * @param manager The updating manager.
     */
    protected final void beginUpdate(final ResourceAdapterUpdateManager manager){
        beginUpdate(manager, null);
    }

    private synchronized void addResource(final ServiceReference<ManagedResourceConnector> resourceRef) {
        final String resourceName = ManagedResourceConnectorClient.getManagedResourceName(resourceRef);
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
                        attributeAdded(new AttributeAddedEvent(attributeSupport, resourceName, metadata));
                final NotificationSupport notificationSupport = connector.queryObject(NotificationSupport.class);
                if(notificationSupport != null)
                    for(final MBeanNotificationInfo metadata: notificationSupport.getNotificationInfo())
                        notificationAdded(new NotificationAddedEvent(notificationSupport, resourceName, metadata));
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
                for(final FeatureAccessor<?> accessor: removeAllFeaturesImpl(resourceName))
                    accessor.close();
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
                for(final ServiceReference<ManagedResourceConnector> resourceRef: resources)
                    addResource(resourceRef);
                InternalState newState = currentState.setParameters(params);
                start(newState.parameters);
                mutableState = newState.setAdapterState(AdapterState.STARTED);
                adapterStarted();
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
                    for(final ServiceReference<ManagedResourceConnector> resourceRef: resources)
                        removeResource(resourceRef);
                }
                finally {
                    getBundleContext().removeServiceListener(this);
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
                        break;
                    case ServiceEvent.REGISTERED:
                        addResource(connectorRef);
                        break;
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
        return String.format("com.bytex.snamp.adapters.%s", adapterName);
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
     * @throws java.io.IOException An exception occurred during adapter releasing.
     */
    @Override
    public final void close() throws IOException {
        try {
            tryStop();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(String.format("Unable to release resources associated with %s adapter instance", adapterInstanceName), e);
        } finally {
            mutableState = InternalState.finalState();
        }
    }

    private void adapterStarted(){
        ResourceAdapterEventBus.notifyAdapterStarted(getAdapterName(), this);
    }

    private void adapterStopped(){
        ResourceAdapterEventBus.notifyAdapterStopped(getAdapterName(), this);
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
    public Logger getLogger(){
        return getLogger(getAdapterName(this));
    }

    /**
     * Returns a string representation of this adapter.
     * @return A string representation of this adapter.
     */
    @Override
    public String toString() {
        return adapterInstanceName;
    }

    public final String getAdapterName(){
        return getAdapterName(this);
    }

    public static String getAdapterName(final Class<? extends ResourceAdapter> adapterType){
        return getAdapterName(FrameworkUtil.getBundle(adapterType));
    }

    public static String getAdapterName(final ResourceAdapter adapter) {
        return getAdapterName(adapter.getClass());
    }

    static boolean isResourceAdapterBundle(final Bundle bnd){
        return bnd != null && bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER) != null;
    }

    private static String getAdapterName(final Dictionary<String, ?> identity) {
        return Objects.toString(identity.get(ADAPTER_NAME_MANIFEST_HEADER), "");
    }

    static String getAdapterName(final Bundle bnd){
        return getAdapterName(bnd.getHeaders());
    }

    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        return ImmutableMultimap.of();
    }

    protected static <TAccessor extends AttributeAccessor & FeatureBindingInfo<MBeanAttributeInfo>> Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getBindings(final AttributeSet<TAccessor> model){
        final Multimap<String, TAccessor> result = HashMultimap.create();
        model.forEachAttribute(new RecordReader<String, TAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final TAccessor accessor) {
                return result.put(resourceName, accessor);
            }
        });
        return result;
    }

    protected static <TAccessor extends NotificationAccessor & FeatureBindingInfo<MBeanNotificationInfo>> Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getBindings(final NotificationSet<TAccessor> model){
        final Multimap<String, TAccessor> result = HashMultimap.create();
        model.forEachNotification(new RecordReader<String, TAccessor, ExceptionPlaceholder>() {
            @Override
            public boolean read(final String resourceName, final TAccessor accessor) {
                return result.put(resourceName, accessor);
            }
        });
        return result;
    }
}
