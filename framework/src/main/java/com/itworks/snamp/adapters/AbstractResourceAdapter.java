package com.itworks.snamp.adapters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.AsyncEventListener;
import com.itworks.snamp.concurrent.GroupedThreadFactory;
import com.itworks.snamp.concurrent.WriteOnceRef;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.connectors.*;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.core.OsgiLoggingContext;
import com.itworks.snamp.core.RichLogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.WeakMultimap;
import com.itworks.snamp.internal.annotations.Temporary;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.mapping.*;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
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

        private String getAdapterInstanceName(){
            return getProperty(ADAPTER_INSTANCE_NAME_PROPERTY, String.class, "");
        }

        private static AdapterLogicalOperation restarting(final String adapterInstanceName){
            return new AdapterLogicalOperation("restart", adapterInstanceName);
        }

        private static AdapterLogicalOperation connectorChangesDetected(final String adapterInstanceName){
            return new AdapterLogicalOperation("processResourceConnectorChanges", adapterInstanceName);
        }
    }

    private static abstract class UnsupportedInternalOperation extends UnsupportedOperationException{
        private UnsupportedInternalOperation(final String message){
            super(message);
        }
    }

    private static final class UnsupportedResourceRemovedOperation extends UnsupportedInternalOperation{
        private UnsupportedResourceRemovedOperation(final String resourceName){
            super(String.format("resourceRemoved for %s is not supported", resourceName));
        }
    }

    private static final class UnsupportedResourceAddedOperation extends UnsupportedInternalOperation{
        private UnsupportedResourceAddedOperation(final String resourceName){
            super(String.format("resourceRemoved for %s is not supported", resourceName));
        }
    }

    /**
     * Represents resource management model based on notifications.
     * @param <TNotificationView> Type of the notification metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractNotificationsModel<TNotificationView> extends HashMap<String, TNotificationView> implements EventHandler{
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
            return String.format("%s-%s-%s", System.identityHashCode(this), resourceName, eventName);
        }

        /**
         * Creates a new notification metadata representation.
         * @param resourceName User-defined name of the managed resource.
         * @param eventName The resource-local identifier of the event.
         * @param notifMeta The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        protected abstract TNotificationView createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta);

        /**
         * Processes SNAMP notification.
         * @param sender The name of the managed resource which emits the notification.
         * @param notif The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        protected abstract void handleNotification(final String sender, final Notification notif, final TNotificationView notificationMetadata);

        /**
         * Handles an event received through OSGi message pipe as SNAMP notification.
         * @param event The event that occurred.
         */
        @Override
        public final void handleEvent(final Event event) {
            final NotificationEvent notif = new NotificationEvent(event);
            if(containsKey(notif.getSubscriptionListID()))
                handleNotification(notif.getSender(), notif, get(notif.getSubscriptionListID()));
        }

        private Set<String> getTopics(){
            if(registration != null){
                final Object topics = registration.getReference().getProperty(EventConstants.EVENT_TOPIC);
                if(topics instanceof String[])
                    return ImmutableSet.copyOf((String[])topics);
                else if(topics instanceof String)
                    return ImmutableSet.of((String)topics);
                else return ImmutableSet.of();
            }
            else return Collections.emptySet();
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
                                  final AttributeSupport attributeSupport) throws AttributeSupportException {
            if (attributeSupport.connectAttribute(attributeID,
                    attributeConfig.getAttributeName(),
                    attributeConfig.getParameters()) == null)
                throw new AttributeSupportException(new IllegalArgumentException(String.format("Unable to register attribute %s", attributeConfig.getAttributeName())));
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
         */
        public <T> T getValue(final TypeToken<T> attributeType, final T defaultValue) throws TimeoutException, IllegalArgumentException {
            try {
                return getValue(attributeType);
            } catch (final AttributeSupportException e) {
                return defaultValue;
            }
        }

        /**
         * Gets value of the attribute.
         * @param attributeType The type of the attribute value.
         * @return The value of the attribute.
         * @throws java.lang.IllegalArgumentException Unsupported attribute type.
         * @throws TimeoutException Attribute value cannot be obtained during the configured duration.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        public <T> T getValue(final TypeToken<T> attributeType) throws TimeoutException, AttributeSupportException {
            if (attributeType == null) throw new IllegalArgumentException("attributeType is null.");
            final TypeConverter<T> converter = getType().getProjection(attributeType);
            if (converter == null)
                throw new IllegalArgumentException(String.format("Invalid type %s of attribute %s",
                        attributeType,
                        getName()));
            final Object result;
            try {
                result = attributeSupport.getAttribute(attributeID, readWriteTimeout);
            } catch (final UnknownAttributeException e) {
                throw new AttributeSupportException(e);
            }
            return TypeLiterals.isInstance(result, attributeType) ?
                    TypeLiterals.cast(result, attributeType) : converter.convertFrom(result);
        }

        /**
         * Gets value of the attribute.
         * @return The value of the attribute.
         * @throws TimeoutException Attribute value cannot be obtained during the configured duration.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        public ManagedEntityValue<?> getValue() throws TimeoutException, AttributeSupportException {
            try {
                final Object result = attributeSupport.getAttribute(attributeID, readWriteTimeout);
                return new ManagedEntityValue<>(result, getType());
            } catch (final UnknownAttributeException e) {
                throw new AttributeSupportException(e);  //never happens
            }
        }

        /**
         * Gets raw value of the attribute without converting to the well-known type.
         * @return The raw value of the attribute.
         * @throws TimeoutException Attribute value cannot be obtained during the configured duration.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        public Object getRawValue() throws TimeoutException, AttributeSupportException {
            try {
                return attributeSupport.getAttribute(attributeID, readWriteTimeout);
            } catch (final UnknownAttributeException e) {
                throw new AttributeSupportException(e);
            }
        }

        /**
         * Sets the value of the attribute.
         * @param value The value of the attribute.
         * @throws java.util.concurrent.TimeoutException Attribute value cannot be changed during the configured duration.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        public void setValue(final Object value) throws TimeoutException, AttributeSupportException {
            try {
                attributeSupport.setAttribute(attributeID, readWriteTimeout, value);
            } catch (final UnknownAttributeException e) {
                throw new AttributeSupportException(e);
            }
        }

        public void setRowSet(final RowSet<?> value) throws TimeoutException, AttributeSupportException{
            //cast is necessary. We should determine whether the RowSet saves the generic actual type
            setValue(TypeLiterals.cast(value, TypeLiterals.ROW_SET));
        }

        public <C> void setRowSet(final Set<String> columns,
                              final List<? extends Map<String, C>> rows) throws TimeoutException, AttributeSupportException {
            setRowSet(columns, Collections.<String>emptySet(), rows);
        }

        public <C> void setRowSet(final Set<String> columns,
                              final Set<String> indexedColumns,
                              final List<? extends Map<String, C>> rows) throws TimeoutException, AttributeSupportException {
            setRowSet(RecordSetUtils.fromRows(columns, indexedColumns, rows));
        }

        public void setNamedRecordSet(final RecordSet<String, ?> value) throws TimeoutException, AttributeSupportException{
            //cast is necessary. We should determine whether the RecordSet saves the generic actual type
            setValue(TypeLiterals.cast(value, TypeLiterals.NAMED_RECORD_SET));
        }

        public void setBoolean(final boolean value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setByte(final byte value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setShort(final short value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setInt(final int value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setLong(final long value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setBigInt(final BigInteger value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setBigDecimal(final BigDecimal value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setFloat(final float value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setDouble(final double value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setDateTime(final Date value) throws TimeoutException, AttributeSupportException{
            setValue(value);
        }

        public void setString(final String value) throws TimeoutException, AttributeSupportException{
            setValue(value);
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
        public ManagedEntityType getType() throws IllegalStateException{
            return getMetadataAndCheckState().getType();
        }

        /**
         * Returns the resolved well-known type of the attribute.
         * @return The resolved well-known type; or {@literal null}, if the managed entity type
         * is not a part of well-known type system.
         * @throws java.lang.IllegalStateException The accessor is disconnected from the managed resource connector.
         * @see com.itworks.snamp.connectors.WellKnownTypeSystem
         */
        public TypeToken<?> getWellKnownType() throws IllegalStateException{
            return WellKnownTypeSystem.getWellKnownType(getType());
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

        /**
         * Determines whether this attribute has the type which is a subtype of the specified type.
         * @param expectedType The expected type.
         * @return {@literal true}, if this attribute has the type which is a subtype of the specified type; otherwise, {@literal false}.
         */
        public boolean hasManagedType(final Class<? extends ManagedEntityType> expectedType) {
            return expectedType.isInstance(getType());
        }

        /**
         * Gets identifier of this attribute.
         * @return The identifier of this attribute.
         */
        @Override
        public String toString() {
            return attributeID;
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
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @ThreadSafe
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName){
            return String.format("%s-%s-%s", System.identityHashCode(this), resourceName, userDefinedAttributeName);
        }

        /**
         * Creates a new domain-specific representation of the management attribute.
         * @param resourceName User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @ThreadSafe
        protected abstract TAttributeView createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor);
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
                            resourceConnector.clear(context);
                        }
                        resourceConnector = new ServiceReferenceHolder<>(context, connectorRef);
                        break;
                    case ServiceEvent.UNREGISTERING:
                    case ServiceEvent.MODIFIED_ENDMATCH:
                        if(resourceConnector != null)
                            resourceConnector.clear(context);
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
                resourceConnector.clear(context);
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
        connectors = new AbstractKeyedObjects<String, ManagedResourceConnectorConsumer>(10) {
            @Override
            public String getKey(final ManagedResourceConnectorConsumer item) {
                return item.resourceName;
            }
        };
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
            configAdmin.clear(context);
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
     * @param <TAttributeView> Type of the attribute metadata representation.
     * @param attributesModel The model to be populated. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException attributesModel is {@literal null}.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal resource connector error.
     * @throws java.lang.Exception Internal adapter error
     */
    @ThreadSafe(true)
    protected final <TAttributeView> void populateModel(final AbstractAttributesModel<TAttributeView> attributesModel) throws Exception {
        if (attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        else
            populateResources(getBundleContextByObject(this), new Consumer<ManagedResourceConnectorConsumer, AttributeSupportException>() {
                @Override
                public void accept(final ManagedResourceConnectorConsumer consumer) throws AttributeSupportException{
                    if (consumer.isAttributesSupported()) {
                        final AttributeSupport support = consumer.getWeakAttributeSupport();
                        final Map<String, AttributeConfiguration> attributes = consumer.resourceConfiguration.getElements(AttributeConfiguration.class);
                        if (attributes == null) return;
                        enlargeModel(consumer.resourceName, attributes, attributesModel, support);
                    }
                    else if(consumer.isReferenced()) try (final OsgiLoggingContext logger = getLoggingContext()) {
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
        try(final OsgiLoggingContext logger = getLoggingContext()){
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
     * @param <TNotificationView> Type of the notification metadata.
     * @throws java.lang.IllegalArgumentException notificationsModel is {@literal null}.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal resource connector error.
     * @throws java.lang.Exception Internal adapter error.
     */
    protected final <TNotificationView> void populateModel(final AbstractNotificationsModel<TNotificationView> notificationsModel) throws Exception{
        if (notificationsModel == null) throw new IllegalArgumentException("notificationsModel is null.");
        final Set<String> topics = new HashSet<>(10);
        populateResources(getBundleContextByObject(this), new Consumer<ManagedResourceConnectorConsumer, NotificationSupportException>() {
            @Override
            public void accept(final ManagedResourceConnectorConsumer consumer) throws NotificationSupportException{
                if (consumer.isNotificationsSupported()) {
                    final NotificationSupport support = consumer.getWeakNotificationSupport();
                    final Map<String, EventConfiguration> events = consumer.resourceConfiguration.getElements(EventConfiguration.class);
                    if (events == null) return;
                    enlargeModel(consumer.resourceName,
                            consumer.resourceConfiguration.getConnectionType(),
                            consumer.resourceConfiguration.getConnectionString(),
                            events,
                            notificationsModel,
                            topics,
                            support);
                }
                else if(consumer.isReferenced())
                    try(final OsgiLoggingContext logger = getLoggingContext()){
                        logger.info(String.format("Managed resource connector %s (connection string %s) doesn't support notifications. Context: %s",
                                consumer.resourceConfiguration.getConnectionType(),
                                consumer.resourceConfiguration.getConnectionString(),
                                LogicalOperation.current()));
                    }
                else logConnectorNotExposed(consumer.resourceConfiguration.getConnectionType(), consumer.resourceName);
            }
        });
        //starts listening for events received through EventAdmin
        if (notificationsModel.size() > 0)
            notificationsModel.startListening(getBundleContextByObject(this), topics);
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
        else if(!notificationsModel.isEmpty()) {
            notificationsModel.stopListening();
            for (final ManagedResourceConnectorConsumer consumer : connectors.values())
                if (consumer.isNotificationsSupported()) {
                    final NotificationSupport support = consumer.getWeakNotificationSupport();
                    for (final String listID : notificationsModel.keySet())
                        try {
                            support.disableNotifications(listID);
                        }
                        catch (final NotificationSupportException e) {
                            try (final OsgiLoggingContext context = getLoggingContext()) {
                                context.log(Level.WARNING, String.format("Failed to disable notifications at %s topic", listID), e.getCause());
                            }
                        }
                }
            notificationsModel.clear();
        }
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
    protected final void clearModel(final AbstractAttributesModel<?> attributesModel) {
        if (attributesModel == null) throw new IllegalArgumentException("attributesModel is null.");
        for (final ManagedResourceConnectorConsumer consumer : connectors.values())
            if (consumer.isAttributesSupported()) {
                final AttributeSupport attributeProvider = consumer.getWeakAttributeSupport();
                for (final String attributeID : attributesModel.keySet())
                    attributeProvider.disconnectAttribute(attributeID);
            }
        attributesModel.clear();
    }

    /**
     * Starts the adapter.
     * <p>
     *     This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @param parameters Adapter startup parameters.
     * @throws java.lang.Exception Unable to start adapter.
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
     * @see #populateModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)
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
        try(final OsgiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Adapter %s is started. Context: %s",
                    adapterInstanceName,
                    LogicalOperation.current()));
        }
    }

    private void adapterStopped(){
        final ResourceAdapterEventListener listener = this.listener.get();
        if(listener != null)
            listener.adapterStopped(new ResourceAdapterEvent(this));
        try(final OsgiLoggingContext logger = getLoggingContext()){
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
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
     * @see #clearModel(com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)
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

    private <TAttributeView> void enlargeModel(final String resourceName,
                                               final Map<String, AttributeConfiguration> newAttributes,
                                               final AbstractAttributesModel<TAttributeView> model,
                                               final AttributeSupport attributeSupport) throws AttributeSupportException{
        for (final Map.Entry<String, AttributeConfiguration> entry : newAttributes.entrySet()) {
            final String attributeID = model.makeAttributeID(resourceName,
                    entry.getKey());
            final TAttributeView view = model.createAttributeView(resourceName,
                    entry.getKey(),
                    new AttributeAccessor(attributeID, entry.getValue(), attributeSupport));
            if (view != null)
                model.put(attributeID, view);
            else attributeSupport.disconnectAttribute(attributeID);
        }
    }

    /**
     * Propagates the attributes of newly connected resource to the model.
     * <p>
     *     It is recommended to call this method inside of overridden {@link #resourceAdded(String)} method.
     * </p>
     * @param resourceName The name of newly connected resource.
     * @param model The model to enlarge.
     * @param <TAttributeView> Type of the attribute metadata.
     * @throws AttributeSupportException Internal resouce connector error.
     */
    protected final <TAttributeView> void enlargeModel(final String resourceName,
                                                      final AbstractAttributesModel<TAttributeView> model) throws AttributeSupportException {
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if (consumer != null) {
            final Map<String, AttributeConfiguration> attributes = consumer.resourceConfiguration.getElements(AttributeConfiguration.class);
            if (consumer.isAttributesSupported())
                enlargeModel(consumer.resourceName,
                        attributes != null ? attributes : Collections.<String, AttributeConfiguration>emptyMap(),
                        model,
                        consumer.getWeakAttributeSupport());
        }
    }

    private <TNotificationView> void enlargeModel(
            final String resourceName,
            final String connectorType,
            final String connectionString,
            final Map<String, EventConfiguration> events,
            final AbstractNotificationsModel<TNotificationView> model,
            final Set<String> topics,
            final NotificationSupport support) throws NotificationSupportException{
        for (final Map.Entry<String, EventConfiguration> entry : events.entrySet()) {
            final String listID = model.makeSubscriptionListID(resourceName, entry.getKey());
            final EventConfiguration eventConfig = entry.getValue();
            final NotificationMetadata metadata = support.enableNotifications(listID, eventConfig.getCategory(), eventConfig.getParameters());
            final TNotificationView view = metadata != null ?
                    model.createNotificationView(resourceName, entry.getKey(), metadata):
                    null;
            if (view != null) {
                model.put(listID, view);
                topics.add(NotificationUtils.getTopicName(connectorType,
                        metadata.getCategory(),
                        listID));
            } else try (final OsgiLoggingContext context = getLoggingContext()) {
                context.warning(String.format("Event %s cannot be enabled for %s resource.", eventConfig.getCategory(), connectionString));
            }
        }
    }

    protected final <TNotificationView> void enlargeModel(final String resourceName,
                                                          final AbstractNotificationsModel<TNotificationView> model) throws NotificationSupportException{
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if(consumer != null){
            final Map<String, EventConfiguration> events = consumer.resourceConfiguration.getElements(EventConfiguration.class);
            if(consumer.isNotificationsSupported()) {
                final Set<String> topics = new HashSet<>(model.getTopics());
                try {
                    model.stopListening();
                    enlargeModel(consumer.resourceName,
                            consumer.resourceConfiguration.getConnectionType(),
                            consumer.resourceConfiguration.getConnectionString(),
                            events,
                            model,
                            topics,
                            consumer.getWeakNotificationSupport());
                } finally {
                    model.startListening(consumer.context, topics);
                }
            }
        }
    }

    private void clearModel(final String resourceName,
                            final Set<String> disconnectedAttributes,
                            final AbstractAttributesModel<?> model,
                            final AttributeSupport attributeSupport) {
        if (attributeSupport != null)
            for (final String userDefinedName : disconnectedAttributes) {
                final String attributeID = model.makeAttributeID(resourceName, userDefinedName);
                if(model.containsKey(attributeID)) {
                    model.remove(attributeID);
                    if (!attributeSupport.disconnectAttribute(attributeID))
                        try (final OsgiLoggingContext logger = getLoggingContext()) {
                            logger.info(String.format("Unable to disconnect attribute %s of resource %s. Context: %s",
                                    attributeID,
                                    resourceName,
                                    LogicalOperation.current()));
                        }
                }
            }
    }

    /**
     * Disconnects attributes from model.
     * @param resourceName The name of the managed resource which attributes should be disconnected.
     * @param model The model to update. Cannot be {@literal null}.
     * @see #resourceRemoved(String)
     */
    protected final void clearModel(final String resourceName, final AbstractAttributesModel<?> model){
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if(consumer != null) {
            final Map<String, AttributeConfiguration> disconnectedAttrs = consumer.resourceConfiguration.getElements(AttributeConfiguration.class);
            clearModel(consumer.resourceName, disconnectedAttrs != null ? disconnectedAttrs.keySet() : Collections.<String>emptySet(), model, consumer.getWeakAttributeSupport());
        }
    }

    private void clearModel(final String resourceName,
                            final String connectorType,
                            final Map<String, EventConfiguration> disconnectedEvents,
                            final AbstractNotificationsModel<?> model,
                            final NotificationSupport notificationSupport) {
        if (notificationSupport != null) {
            final Set<String> topics = new HashSet<>(model.getTopics());
            model.stopListening();
            for (final String userDefinedName : disconnectedEvents.keySet()) {
                final String listID = model.makeSubscriptionListID(resourceName, userDefinedName);
                final EventConfiguration eventConf = disconnectedEvents.get(userDefinedName);
                //disable receiving notifications
                topics.remove(NotificationUtils.getTopicName(connectorType, eventConf.getCategory(), listID));
                if(model.containsKey(listID)) {
                    try {
                        //remove event from the model
                        model.remove(listID);
                        //disable notification in the connector
                        notificationSupport.disableNotifications(listID);
                    } catch (NotificationSupportException e) {
                        try (final OsgiLoggingContext logger = getLoggingContext()) {
                            logger.info(String.format("Unable to disable event subscription %s of resource %s. Context: %s",
                                    listID,
                                    resourceName,
                                    LogicalOperation.current()));
                        }
                    }
                }
            }
            model.startListening(getBundleContextByObject(this), topics);
        }
    }

    /**
     * Disables events in the model.
     * @param resourceName The name of the managed resource which events should be disabled.
     * @param model The model to update. Cannot be {@literal null}.
     * @see #resourceRemoved(String)
     */
    protected final void clearModel(final String resourceName, final AbstractNotificationsModel<?> model){
        final ManagedResourceConnectorConsumer consumer = connectors.get(resourceName);
        if(consumer != null){
            final Map<String, EventConfiguration> disconnectedEvents = consumer.resourceConfiguration.getElements(EventConfiguration.class);
            clearModel(consumer.resourceName,
                    consumer.resourceConfiguration.getConnectionType(),
                    disconnectedEvents != null ? disconnectedEvents : Collections.<String, EventConfiguration>emptyMap(),
                    model,
                    consumer.getWeakNotificationSupport());
        }
    }

    /**
     * Invokes when resource connector is in stopping state or resource configuration was removed.
     * <p>
     *     This method will be called automatically by SNAMP infrastructure.
     *     In the default implementation this method throws internal exception
     *     derived from {@link java.lang.UnsupportedOperationException} indicating
     *     that the adapter should be restarted.
     *     It is recommended to use {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)}
     *     and/or {@link #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)} to
     *     update your underlying models.
     * </p>
     * @param resourceName The name of the resource to be removed.
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
     * @see #clearModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractNotificationsModel)
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
     * @see #enlargeModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
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
            try (final OsgiLoggingContext logger = getLoggingContext()) {
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
                        try (final OsgiLoggingContext logger = getLoggingContext()) {
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

    private OsgiLoggingContext getLoggingContext(){
        return OsgiLoggingContext.get(getLogger(), getBundleContextByObject(this));
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
        try (final OsgiLoggingContext context = getLoggingContext()) {
            context.log(logLevel, String.format("Failed to start resource adapter %s.", adapterInstanceName), e);
        }
    }

    /**
     * Reports an error when stopping adapter.
     * @param logLevel Logging level.
     * @param e The failure reason.
     */
    protected void failedToStopAdapter(final Level logLevel, final Exception e){
        try(final OsgiLoggingContext context = getLoggingContext()) {
            context.log(logLevel, String.format("Failed to stop resource adapter %s.", adapterInstanceName), e);
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
