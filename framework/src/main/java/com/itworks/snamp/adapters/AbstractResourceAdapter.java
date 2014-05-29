package com.itworks.snamp.adapters;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeValue;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.connectors.notifications.NotificationUtils;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.semantics.ThreadSafe;
import org.osgi.framework.*;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.itworks.snamp.connectors.notifications.NotificationUtils.NotificationEvent;
import static com.itworks.snamp.internal.Utils.getBundleContextByObject;
import static com.itworks.snamp.internal.Utils.isInstanceOf;

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
public abstract class AbstractResourceAdapter extends AbstractAggregator implements FrameworkService, AllServiceListener, AutoCloseable{
    /**
     * Represents resource management model based on notifications.
     * @param <TNotificationView> Type of the notification metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class AbstractNotificationsModel<TNotificationView> extends HashMap<String, TNotificationView> implements EventHandler{
        private ServiceRegistration<EventHandler> registration;

        /**
         * Initializes a new notifications-based resource management model.
         */
        protected AbstractNotificationsModel(){
            super(10);
            registration = null;
        }

        /**
         * Creates subscription list ID.
         * @param resourceName User-defined name of the managed resource which can emit the notification.
         * @param eventName User-defined name of the event.
         * @return A new unique subscription list ID.
         */
        protected String makeSubscriptionListID(final String resourceName, final String eventName){
            return String.format("%s-%s-%s", hashCode(), resourceName, eventName);
        }

        /**
         * Creates a new notification metadata representation.
         * @param prefix The namespace of the event.
         * @param postfix The resource-local identifier of the event.
         * @param notifMeta The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        protected abstract TNotificationView createNotificationView(final String prefix, final String postfix, final NotificationMetadata notifMeta);

        /**
         * Processes SNMP notification.
         * @param notif The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        protected abstract void handleNotification(final Notification notif, final TNotificationView notificationMetadata);

        /**
         * Handles an event received through OSGi message pipe as SNAMP notification.
         * @param event The event that occurred.
         */
        @Override
        public final void handleEvent(final Event event) {
            final NotificationEvent notif = new NotificationEvent(event);
            if(containsKey(notif.getSubscriptionListID()))
                handleNotification(notif, get(notif.getSubscriptionListID()));
        }

        private void startListening(final BundleContext context, final Collection<String> topics) {
            if(registration != null)
                registration.unregister();
            final Dictionary<String, Object> identity = new Hashtable<>();
            identity.put(EventConstants.EVENT_TOPIC, topics.toArray(new String[topics.size()]));
            registration = context.registerService(EventHandler.class, this, identity);
        }

        private void stopListening(){
            if(registration != null)
                try {
                    registration.unregister();
                }
                finally {
                    registration = null;
                }
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
    public static final class AttributeAccessor implements AttributeMetadata {
        private final AttributeSupport attributeSupport;
        private final String attributeID;
        private final TimeSpan readWriteTimeout;

        private AttributeAccessor(final String attributeID,
                                  final AttributeConfiguration attributeConfig,
                                  final AttributeSupport attributeSupport){
            if(attributeSupport.connectAttribute(attributeID,
                    attributeConfig.getAttributeName(),
                    attributeConfig.getParameters()) == null)
                throw new IllegalArgumentException(String.format("Unable to register attribute %s", attributeConfig.getAttributeName()));
            this.attributeSupport = attributeSupport;
            this.attributeID = attributeID;
            this.readWriteTimeout = attributeConfig.getReadWriteTimeout();
        }

        private AttributeMetadata getMetadataAndCheckState() throws IllegalStateException{
            final AttributeMetadata attributeMeta = attributeSupport.getAttributeInfo(attributeID);
            if(attributeMeta == null) throw new IllegalStateException(String.format("Attribute %s is not available.", attributeID));
            else return attributeMeta;
        }

        /**
         * Gets value of the attribute.
         * @param attributeType The type of the attribute value.
         * @param defaultValue The default value of the attribute if it is not available.
         * @return The value of the attribute.
         * @throws java.lang.IllegalArgumentException Unsupported attribute type.
         * @throws TimeoutException Attribute value cannot be obtained during the configured duration.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        public <T> T getValue(final Class<T> attributeType, final T defaultValue) throws TimeoutException, IllegalArgumentException, IllegalStateException{
            if(attributeType == null) throw new IllegalArgumentException("attributeType is null.");
            final TypeConverter<T> converter = getType().getProjection(attributeType);
            if(converter == null)
                throw new IllegalArgumentException(String.format("Invalid type %s of attribute %s",
                    attributeType,
                        getName()));
            final Object result = attributeSupport.getAttribute(attributeID, readWriteTimeout, defaultValue);
            return attributeType.isInstance(result) ? attributeType.cast(result) : converter.convertFrom(result);
        }

        /**
         * Gets value of the attribute.
         * @return The value of the attribute.
         * @throws TimeoutException Attribute value cannot be obtained during the configured duration.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        public AttributeValue<?> getValue() throws TimeoutException, IllegalStateException{
            final Object result = attributeSupport.getAttribute(attributeID, readWriteTimeout, null);
            return new AttributeValue<>(result, getType());
        }

        /**
         * Sets the value of the attribute.
         * @param value The value of the attribute.
         * @return {@literal true}, if the attribute is changed successfully; otherwise, {@literal false}.
         * @throws java.util.concurrent.TimeoutException Attribute value cannot be changed during the configured duration.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        public boolean setValue(final Object value) throws TimeoutException, IllegalStateException{
            return attributeSupport.setAttribute(attributeID, readWriteTimeout, value);
        }

        /**
         * Returns the system name of the attribute.
         * @return The attribute name.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String getName() throws IllegalStateException{
            return getMetadataAndCheckState().getName();
        }

        /**
         * Returns the localized name of this attribute.
         *
         * @param locale The locale of the display name. If it is {@literal null} then returns display name
         *               in the default locale.
         * @return The localized name of this attribute.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String getDisplayName(final Locale locale) throws IllegalStateException{
            return getMetadataAndCheckState().getDisplayName(locale);
        }

        /**
         * Determines whether the value of this attribute can be obtained.
         * @return {@literal true}, if attribute value can be obtained; otherwise, {@literal false}.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean canRead() throws IllegalStateException{
            return getMetadataAndCheckState().canRead();
        }

        /**
         * Determines whether the value of this attribute can be changed.
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean canWrite() throws IllegalStateException{
            return getMetadataAndCheckState().canWrite();
        }

        /**
         * Determines whether the value of the attribute can be cached after first reading
         * and supplied as real attribute value before first write.
         *
         * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean cacheable() throws IllegalStateException{
            return getMetadataAndCheckState().cacheable();
        }

        /**
         * Returns the type of the attribute value.
         *
         * @return The type of the attribute value.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public ManagementEntityType getType() throws IllegalStateException{
            return getMetadataAndCheckState().getType();
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String getDescription(final Locale locale) throws IllegalStateException{
            return getMetadataAndCheckState().getDescription(locale);
        }

        /**
         * The number of additional metadata parameters.
         *
         * @return The number of additional metadata parameters.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public int size() throws IllegalStateException{
            return getMetadataAndCheckState().size();
        }

        /**
         * Returns <tt>true</tt> if this map contains no key-value mappings.
         *
         * @return <tt>true</tt> if this map contains no key-value mappings.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean isEmpty() throws IllegalStateException{
            return getMetadataAndCheckState().isEmpty();
        }

        /**
         * Returns <tt>true</tt> if this map contains a mapping for the specified
         * key.  More formally, returns <tt>true</tt> if and only if
         * this map contains a mapping for a key <tt>k</tt> such that
         * <tt>(key==null ? k==null : key.equals(k))</tt>.
         *
         * @param key key whose presence in this map is to be tested
         * @return <tt>true</tt> if this map contains a mapping for the specified key.
         * @throws ClassCastException   if the key is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified key is null and this map
         *                              does not permit null keys
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean containsKey(final Object key) throws IllegalStateException{
            return getMetadataAndCheckState().containsKey(key);
        }

        /**
         * Returns <tt>true</tt> if this map maps one or more keys to the
         * specified value.
         *
         * @param value value whose presence in this map is to be tested
         * @return <tt>true</tt> if this map maps one or more keys to the
         * specified value
         * @throws ClassCastException   if the value is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified value is null and this
         *                              map does not permit null values
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public boolean containsValue(final Object value) throws IllegalStateException{
            return getMetadataAndCheckState().containsValue(value);
        }

        /**
         * Returns the value to which the specified key is mapped,
         * or {@code null} if this map contains no mapping for the key.
         *
         * @param key the key whose associated value is to be returned
         * @return the value to which the specified key is mapped, or
         * {@code null} if this map contains no mapping for the key
         * @throws ClassCastException   if the key is of an inappropriate type for
         *                              this map
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException if the specified key is null and this map
         *                              does not permit null keys
         *                              (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String get(final Object key) throws IllegalStateException{
            return getMetadataAndCheckState().get(key);
        }

        /**
         * Associates the specified value with the specified key in this map
         * (optional operation).  If the map previously contained a mapping for
         * the key, the old value is replaced by the specified value.
         *
         * @param key   key with which the specified value is to be associated
         * @param value value to be associated with the specified key
         * @return the previous value associated with <tt>key</tt>, or
         * <tt>null</tt> if there was no mapping for <tt>key</tt>.
         * (A <tt>null</tt> return can also indicate that the map
         * previously associated <tt>null</tt> with <tt>key</tt>,
         * if the implementation supports <tt>null</tt> values.)
         * @throws UnsupportedOperationException if the <tt>put</tt> operation
         *                                       is not supported by this map
         * @throws ClassCastException            if the class of the specified key or value
         *                                       prevents it from being stored in this map
         * @throws NullPointerException          if the specified key or value is null
         *                                       and this map does not permit null keys or values
         * @throws IllegalArgumentException      if some property of the specified key
         *                                       or value prevents it from being stored in this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String put(final String key, final String value) throws IllegalStateException{
            return getMetadataAndCheckState().put(key, value);
        }

        /**
         * Removes the mapping for a key from this map if it is present
         * (optional operation).
         *
         * @param key key whose mapping is to be removed from the map
         * @return the previous value associated with <tt>key</tt>, or
         * <tt>null</tt> if there was no mapping for <tt>key</tt>.
         * @throws UnsupportedOperationException if the <tt>remove</tt> operation
         *                                       is not supported by this map
         * @throws ClassCastException            if the key is of an inappropriate type for
         *                                       this map
         *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws NullPointerException          if the specified key is null and this
         *                                       map does not permit null keys
         *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public String remove(final Object key) throws IllegalStateException{
            return getMetadataAndCheckState().remove(key);
        }

        /**
         * Copies all of the mappings from the specified map to this map
         * (optional operation).
         *
         * @param m mappings to be stored in this map
         * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
         *                                       is not supported by this map
         * @throws ClassCastException            if the class of a key or value in the
         *                                       specified map prevents it from being stored in this map
         * @throws NullPointerException          if the specified map is null, or if
         *                                       this map does not permit null keys or values, and the
         *                                       specified map contains null keys or values
         * @throws IllegalArgumentException      if some property of a key or value in
         *                                       the specified map prevents it from being stored in this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public void putAll(final Map<? extends String, ? extends String> m) throws IllegalStateException{
            getMetadataAndCheckState().putAll(m);
        }

        /**
         * Removes all of the mappings from this map (optional operation).
         * The map will be empty after this call returns.
         *
         * @throws UnsupportedOperationException if the <tt>clear</tt> operation
         *                                       is not supported by this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @Override
        public void clear() throws IllegalStateException{
            getMetadataAndCheckState().clear();
        }

        /**
         * Returns a {@link java.util.Set} view of the keys contained in this map.
         *
         * @return a set view of the keys contained in this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Set<String> keySet() throws IllegalStateException{
            return getMetadataAndCheckState().keySet();
        }

        /**
         * Returns a {@link java.util.Collection} view of the values contained in this map.
         *
         * @return a collection view of the values contained in this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Collection<String> values() throws IllegalStateException{
            return getMetadataAndCheckState().values();
        }

        /**
         * Returns a {@link java.util.Set} view of the mappings contained in this map.
         *
         * @return a set view of the mappings contained in this map
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         */
        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Entry<String, String>> entrySet() throws IllegalStateException{
            return getMetadataAndCheckState().entrySet();
        }
    }

    /**
     * Represents resource management model based on attributes.
     * <p>
     *     The derived class should not contain any management logic, just a factory
     *     for domain-specific representation of the management attribute and a collection of it.
     * </p>
     * @param <TAttributeView> Type of the domain-specific representation of the management attribute.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractAttributesModel<TAttributeView> extends HashMap<String, TAttributeView>{

        /**
         * Initializes a new resource management model based on a set of attributes.
         */
        protected AbstractAttributesModel(){
            super(10);
        }

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         *     The identifier must be unique through all instances of the resource adapter.
         * </p>
         * @param resourceName User-defined name of the managed resource which supply the attribute.
         * @param attributeUserDefinedName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @ThreadSafe
        protected String makeAttributeID(final String resourceName, final String attributeUserDefinedName){
            return String.format("%s-%s-%s", hashCode(), resourceName, attributeUserDefinedName);
        }

        /**
         * Creates a new domain-specific representation of the management attribute.
         * @param prefix The namespace of the attribute.
         * @param postfix The resource-local identifier of the attribute.
         * @param accessor An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @ThreadSafe
        protected abstract TAttributeView createAttributeView(final String prefix, final String postfix, final AttributeAccessor accessor);
    }


    private static final class ManagedResourceConnectorConsumer implements AllServiceListener, AutoCloseable{
        public final ManagedResourceConfiguration resourceConfiguration;
        private ServiceReferenceHolder<ManagedResourceConnector<?>> resourceConnector;
        /**
         * The name of the managed resource.
         */
        public final String resourceName;

        /**
         * Initializes a new resource connector consumer.
         * @param resourceName User-defined name of the managed resource.
         * @param config The configuration of the managed resource. Cannot be {@literal null}.
         */
        public ManagedResourceConnectorConsumer(final String resourceName, final ManagedResourceConfiguration config){
            this.resourceConfiguration = config;
            this.resourceConnector = null;
            this.resourceName = resourceName;
        }

        private <T> T queryWeakObject(final Class<T> queryObject){
            if(resourceConnector != null){
                final T obj = resourceConnector.getService().queryObject(queryObject);
                if(obj == null) return null;
                return Utils.weakReference(obj, queryObject);
            }
            else return null;
        }

        public final AttributeSupport getWeakAttributeSupport(){
            return queryWeakObject(AttributeSupport.class);
        }

        public final NotificationSupport getWeakNotificationSupport(){
            return queryWeakObject(NotificationSupport.class);
        }

        /**
         * Determines whether the resource connector supports attributes.
         * @return {@literal true}, if the connector supports attributes; otherwise, {@literal false}.
         */
        public final boolean isAttributesSupported(){
            return resourceConnector != null && resourceConnector.getService().queryObject(AttributeSupport.class) != null;
        }

        /**
         * Determines whether the resource connector supports notifications.
         * @return {@literal true}, if the connector supports notification; otherwise, {@literal false}.
         */
        public final boolean isNotificationsSupported(){
            return resourceConnector != null && resourceConnector.getService().queryObject(NotificationSupport.class) != null;
        }

        /**
         * Determines whether the resource connector is referenced.
         * @return {@literal true}, if the resource connector is referenced; otherwise, {@literal false}.
         */
        public final boolean isReferenced(){
            return resourceConnector != null;
        }

        private synchronized void processResourceConnector(final ServiceReference<ManagedResourceConnector<?>> connectorRef, final int eventType){
            if(Objects.equals(AbstractManagedResourceActivator.getResourceName(connectorRef), resourceName))
                switch (eventType){
                    case ServiceEvent.REGISTERED:
                        if(resourceConnector == null)
                            resourceConnector = new ServiceReferenceHolder<>(getBundleContextByObject(this), connectorRef);
                        break;
                    case ServiceEvent.UNREGISTERING:
                    case ServiceEvent.MODIFIED_ENDMATCH:
                        if(resourceConnector != null)
                            resourceConnector.clear(getBundleContextByObject(this));
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
            if(isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class))
                processResourceConnector((ServiceReference<ManagedResourceConnector<?>>)event.getServiceReference(), event.getType());
        }

        /**
         * Releases reference to the resource connector.
         */
        public void close(){
            if(resourceConnector != null)
                resourceConnector.clear(getBundleContextByObject(this));
            resourceConnector = null;
        }
    }

    private final KeyedObjects<String, ManagedResourceConnectorConsumer> connectors;
    private AdapterState state;

    /**
     * Initializes a new resource adapter.
     * @param resources A collection of managed resources to be exposed in protocol-specific manner
     *                  to the outside world.
     */
    protected AbstractResourceAdapter(final Map<String, ManagedResourceConfiguration> resources){
        connectors = new AbstractKeyedObjects<String, ManagedResourceConnectorConsumer>(resources.size()){
            @Override
            public String getKey(final ManagedResourceConnectorConsumer item) {
                return item.resourceName;
            }
        };
        for(final Map.Entry<String, ManagedResourceConfiguration> resourceConfig: resources.entrySet())
            connectors.put(new ManagedResourceConnectorConsumer(resourceConfig.getKey(), resourceConfig.getValue()));
        state = AdapterState.CREATED;
    }

    /**
     * Gets state of this adapter.
     * @return The state of this adapter.
     */
    public final AdapterState getState(){
        return state;
    }

    /**
     * Populates the model with management attributes.
     * <p>
     *     This method extracts management attributes via managed resource connectors
     *     and put them into the model. If managed resource connector doesn't support
     *     {@link com.itworks.snamp.connectors.attributes.AttributeSupport} interface
     *     then it will be ignore and management attributes will not be added into the model.
     *     It is recommended to call this method inside of {@link #start()} method.
     * </p>
     * @param <TAttributeView> Type of the attribute metadata representation.
     * @param attributesModel The model to populate. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException attributesModel is {@literal null}.
     */
    protected final <TAttributeView> void populateModel(final AbstractAttributesModel<TAttributeView> attributesModel){
        if(attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        for(final ManagedResourceConnectorConsumer consumer: connectors.values())
            if(consumer.isAttributesSupported()){
                final AttributeSupport support = consumer.getWeakAttributeSupport();
                final Map<String, AttributeConfiguration> attributes = consumer.resourceConfiguration.getElements(AttributeConfiguration.class);
                if(attributes == null) continue;
                for(final Map.Entry<String, AttributeConfiguration> entry: attributes.entrySet()){
                    final String attributeID = attributesModel.makeAttributeID(consumer.resourceName,
                            entry.getKey());
                    final AttributeAccessor accessor = new AttributeAccessor(attributeID, entry.getValue(), support);
                    attributesModel.put(attributeID, attributesModel.createAttributeView(consumer.resourceName,
                            entry.getKey(),
                            accessor));
                }
            }
            else getLogger().log(Level.INFO, String.format("Managed resource connector %s (connection string %s) doesn't support attributes.",
                    consumer.resourceConfiguration.getConnectionType(),
                    consumer.resourceConfiguration.getConnectionString()));
    }

    /**
     * Populates model with notifications and starts listening for incoming notifications.
     * <p>
     *     This method enables notifications via managed resource connectors and
     *     put notification metadata into the model. If managed resource connector
     *     doesn't support {@link com.itworks.snamp.connectors.notifications.NotificationSupport} interface
     *     then it will be ignored and notifications will not be added into the model.
     *     It is recommended to call this method inside {@link #start()} method.
     * </p>
     * @param notificationsModel The model to populate. Cannot be {@literal null}.
     * @param <TNotificationView> Type of the notification metadata.
     * @throws java.lang.IllegalArgumentException notificationsModel is {@literal null}.
     */
    protected final <TNotificationView> void populateModel(final AbstractNotificationsModel<TNotificationView> notificationsModel){
        if(notificationsModel == null) throw new IllegalArgumentException("notificationsModel is null.");
        final Collection<String> topics = new HashSet<>(10);
        for(final ManagedResourceConnectorConsumer consumer: connectors.values())
            if(consumer.isNotificationsSupported()){
                final NotificationSupport support = consumer.getWeakNotificationSupport();
                final Map<String, EventConfiguration> events = consumer.resourceConfiguration.getElements(EventConfiguration.class);
                if(events == null) continue;
                for(final Map.Entry<String, EventConfiguration> entry: events.entrySet()){
                    final String listID = notificationsModel.makeSubscriptionListID(consumer.resourceName, entry.getKey());
                    final EventConfiguration eventConfig = entry.getValue();
                    final NotificationMetadata metadata = support.enableNotifications(listID, eventConfig.getCategory(), eventConfig.getParameters());
                    if(metadata != null) {
                        notificationsModel.put(listID, notificationsModel.createNotificationView(consumer.resourceName, entry.getKey(), metadata));
                        topics.add(NotificationUtils.getTopicName(consumer.resourceConfiguration.getConnectionType(), metadata.getCategory(), listID));
                    }
                    else getLogger().log(Level.WARNING, String.format("Event %s cannot be enabled for %s resource.", eventConfig.getCategory(), consumer.resourceConfiguration.getConnectionString()));
                }
            }
        //starts listening for events received through EventAdmin
        notificationsModel.startListening(Utils.getBundleContextByObject(notificationsModel), topics);
    }

    /**
     * Removes all listeners, disable notifications and stops the listening for incoming notifications.
     * <p>
     *     It is recommended to call this method inside of {@link #stop()} method.
     * </p>
     * @param notificationsModel The model to clear. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException notificationsModel is {@literal null}.
     */
    protected final void clearModel(final AbstractNotificationsModel<?> notificationsModel){
        if(notificationsModel == null) throw new IllegalArgumentException("notificationsModel is null.");
        notificationsModel.stopListening();
        for(final ManagedResourceConnectorConsumer consumer: connectors.values())
            if(consumer.isReferenced() && consumer.isNotificationsSupported()){
                final NotificationSupport support = consumer.getWeakNotificationSupport();
                for(final String listID: notificationsModel.keySet())
                    support.disableNotifications(listID);
            }
        notificationsModel.clear();
    }

    /**
     * Removes all attributes from the model and disconnect each attribute from the managed
     * resource connector.
     * <p>
     *     It is recommended to call this method inside of {@link #stop()} method.
     * </p>
     * @param attributesModel The model to clear. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException attributesModel is {@literal null}.
     */
    protected final void clearModel(final AbstractAttributesModel<?> attributesModel){
        if(attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        for(final ManagedResourceConnectorConsumer consumer: connectors.values())
            if(consumer.isReferenced() && consumer.isAttributesSupported()) {
                final AttributeSupport attributeProvider = consumer.getWeakAttributeSupport();
                for(final String attributeID: attributesModel.keySet())
                    attributeProvider.disconnectAttribute(attributeID);
            }
        attributesModel.clear();
    }

    void update(final BundleContext context) throws InvalidSyntaxException {
        ServiceReference<?>[] refs = context.getAllServiceReferences(ManagedResourceConnector.class.getName(), null);
        if(refs == null) refs = new ServiceReference<?>[0];
        for(final ServiceReference<?> r: refs)
            serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, r));
    }

    /**
     * Starts the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    protected abstract boolean start();

    /**
     * Stops the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    protected abstract void stop();

    /**
     * Captures reference to the managed resource connectors.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public final void serviceChanged(final ServiceEvent event) {
        int referencedConnectors = 0;
        for(final ManagedResourceConnectorConsumer consumer: connectors.values()) {
            consumer.serviceChanged(event);
            if(consumer.isReferenced()) referencedConnectors += 1;
        }
        switch (state){
            case CREATED:
            case STOPPED:
                //starts the adapter if all resources are connected
                state = referencedConnectors == connectors.size() && start() ?
                        AdapterState.STARTED:
                        AdapterState.STOPPED;
            return;
            case STARTED:
                if(referencedConnectors != connectors.size())
                    try{
                        stop();
                    }
                    finally {
                        state = AdapterState.STOPPED;
                    }
        }
    }

    /**
     * Gets name of the logger associated with the specified resource adapter.
     * @param adapterName The name of the resource adapter.
     * @return The name of the logger.
     */
    public static String getLoggerName(final String adapterName){
        return String.format("itworks.snamp.adapters.%s", adapterName);
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
     * <p>
     *     You should call base implementation of this method
     *     in the overridden method.
     * </p>
     * @throws Exception An exception occurred during adapter releasing.
     */
    @Override
    public void close() throws Exception{
        try{
            if(state == AdapterState.STARTED)
                stop();
        }
        finally {
            for(final ManagedResourceConnectorConsumer consumer: connectors.values())
                consumer.close();
            connectors.clear();
            state = AdapterState.CLOSED;
        }
    }
}