package com.itworks.snamp.adapters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.concurrent.AsyncEventListener;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.ResourceEvent;
import com.itworks.snamp.connectors.ResourceEventListener;
import com.itworks.snamp.connectors.attributes.AttributeAddedEvent;
import com.itworks.snamp.connectors.attributes.AttributeRemovingEvent;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.NotificationAddedEvent;
import com.itworks.snamp.connectors.notifications.NotificationRemovingEvent;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.core.RichLogicalOperation;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.WeakMultimap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
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
                for (final String name : newParameters.keySet())
                    if(!Objects.equals(parameters.get(name), newParameters.get(name)))
                        return false;
                return true;
            }
            else return false;
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
        final FeatureAccessor<MBeanAttributeInfo, AttributeSupport> accessor =
                addFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.connect(event.getSource());
    }

    private void attributeRemoved(final AttributeRemovingEvent event){
        final FeatureAccessor<MBeanAttributeInfo, ?> accessor =
                removeFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.disconnect();
    }

    private void notificationAdded(final NotificationAddedEvent event){
        final FeatureAccessor<MBeanNotificationInfo, NotificationSupport> accessor =
                addFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.connect(event.getSource());
    }

    private void notificationRemoved(final NotificationRemovingEvent event){
        final FeatureAccessor<MBeanNotificationInfo, ?> accessor = removeFeatureImpl(event.getResourceName(), event.getFeature());
        if(accessor != null)
            accessor.disconnect();
    }

    /**
     * Handles resource event.
     *
     * @param event An event to handle.
     * @see com.itworks.snamp.connectors.FeatureAddedEvent
     * @see com.itworks.snamp.connectors.FeatureRemovingEvent
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
    protected abstract <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName,
                                       final M feature) throws Exception;

    private <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeatureImpl(final String resourceName,
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
    protected abstract Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws Exception;

    private Iterable<? extends FeatureAccessor<?, ?>> removeAllFeaturesImpl(final String resourceName){
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
    protected abstract <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName,
                                                                                        final M feature) throws Exception;

    private <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeatureImpl(final String resourceName,
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
                for(final FeatureAccessor<?, ?> accessor: removeAllFeaturesImpl(resourceName))
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
     * @throws java.io.IOException An exception occurred during adapter releasing.
     */
    @Override
    public final void close() throws IOException {
        try {
            tryStop();
        }
        catch (final IOException e){
            throw e;
        }
        catch (final Exception e){
            throw new IOException(String.format("Unable to release resources associated with %s adapter instance", adapterInstanceName), e);
        }
        finally {
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
