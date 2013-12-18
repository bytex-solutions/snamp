package com.snamp.connectors;

import com.snamp.*;
import static com.snamp.ConcurrentResourceAccess.ConsistentAction;
import static com.snamp.ConcurrentResourceAccess.Action;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

/**
 * Represents an abstract class for building custom management connectors.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
@SuppressWarnings("try")
public abstract class AbstractManagementConnector implements ManagementConnector {

    /**
     * Represents default implementation of the attribute descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class GenericAttributeMetadata<T extends ManagementEntityType> implements AttributeMetadata {
        private final String attributeName;
        private final String namespace;
        private T attributeType;

        /**
         * Initializes a new attribute metadata.
         * @param attributeName The name of the attribute. Cannot be {@literal null}.
         * @param namespace The namespace of the attribute. Cannot be {@literal null}.
         * @throws IllegalArgumentException attributeName or namespace is {@literal null}.
         */
        public GenericAttributeMetadata(final String attributeName, final String namespace){
            if(attributeName == null) throw new IllegalArgumentException("attributeName is null.");
            else if(namespace == null) throw new IllegalArgumentException("namespace is null.");
            this.attributeName = attributeName;
            this.namespace = namespace;
        }

        /**
         * Detects the attribute type (this method will be called by infrastructure once).
         * @return Detected attribute type.
         */
        protected abstract T detectAttributeType();

        /**
         * Returns the type of the attribute value.
         *
         * @return The type of the attribute value.
         */
        @Override
        public final T getAttributeType() {
            if(attributeType == null) attributeType = detectAttributeType();
            return attributeType;
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @param option The name of the option to put.
         * @param value The value of the option to put.
         * @return The previously option value.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final String put(final String option, final String value) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @param option The name of the option to remove.
         * @return The value of the remove option.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final String remove(final Object option) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @param options A map of attribute discovery options.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void putAll(Map<? extends String, ? extends String> options) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the attribute name.
         * @return The attribute name.
         */
        @Override
        public final String getAttributeName() {
            return attributeName;
        }

        /**
         * By default, returns {@literal true}, but you can override this method
         * in the derived class.
         * @return {@literal true}
         */
        @Override
        public boolean canRead() {
            return true;
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return true;
        }

        /**
         * Determines whether the value of the attribute can be cached after first reading
         * and supplied as real attribute value before first write, return {@literal false} by default.
         *
         * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
         */
        @Override
        public boolean cacheable() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    /**
     * Represents default implementation of the notification metadata.
     * <p>
     *     This class holds the list of notification listeners.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class GenericNotificationMetadata implements Map<String, String>, NotificationMetadata{
        private static final AtomicLong globalCounter = new AtomicLong(0L);
        private final String eventCategory;
        private final AtomicLong counter;
        private final Map<Long, Pair<NotificationListener, Object>> listeners;
        private final ReadWriteLock coordinator;

        /**
         * Initializes a new event metadata.
         * @param category The category of the event.
         */
        protected GenericNotificationMetadata(final String category){
            this(category, true);
        }

        /**
         * Initializes a new event metadata.
         * @param category The category of the event.
         * @param useGlobalIdGen {@literal true} to generate an unique listener identifier through
         *                                      all instance of the notification metadata inside
         *                                      of this process; {@literal false} to generate an unique
         *                                      identifier scoped to this instance only.
         */
        protected GenericNotificationMetadata(final String category, final boolean useGlobalIdGen){
            this.eventCategory = category;
            this.listeners = new HashMap<>(10);
            this.coordinator = new ReentrantReadWriteLock();
            this.counter = useGlobalIdGen ? globalCounter : new AtomicLong(0L);
        }

        final boolean hasListener(final Long listenerId){
            final Lock readLock = coordinator.readLock();
            readLock.lock();
            try{
                return listeners.containsKey(listenerId);
            }
            finally {
                readLock.unlock();
            }
        }

        /**
         * Gets the category of the notification.
         *
         * @return The category of the notification.
         */
        @Override
        public final String getCategory() {
            return eventCategory;
        }

        /**
         * Returns all subscribed listeners.
         * @return A collection of subscribed listeners.
         */
        public final Collection<Pair<NotificationListener, Object>> getListeners(){
            final Lock readLock = coordinator.readLock();
            readLock.lock();
            try{
                return new ArrayList<>(listeners.values());
            }
            finally {
                readLock.unlock();
            }
        }

        /**
         * Removes all listeners.
         * @return A collection of removed listeners.
         */
        public final Collection<Pair<NotificationListener, Object>> removeListeners(){
            final Lock writeLock = coordinator.readLock();
            writeLock.lock();
            try{
                final Collection<Pair<NotificationListener, Object>> result = new ArrayList<>(listeners.values());
                listeners.clear();
                return result;
            }
            finally {
                writeLock.unlock();
            }
        }

        /**
         * Gets the listener for the specified listener identifier.
         * @param listenerId An identifier of the listener.
         * @return An instance of the notification listener.
         */
        public final Pair<NotificationListener, Object> getListener(final Long listenerId){
            final Lock readLock = coordinator.readLock();
            readLock.lock();
            try{
                return listeners.containsKey(listenerId) ? listeners.get(listenerId) : null;
            }
            finally {
                readLock.unlock();
            }
        }

        /**
         * Adds a new listener for this event.
         * @param listener The notification listener.
         * @param userData The user data associated with the listener.
         * @return A new unique identifier of the added listener.
         */
        public final Long addListener(final NotificationListener listener, final Object userData){
            final Long listenerId = counter.getAndIncrement();
            final Lock writeLock = coordinator.writeLock();
            writeLock.lock();
            try{
                listeners.put(listenerId, new Pair<>(listener, userData));
            }
            finally {
                writeLock.unlock();
            }
            return listenerId;
        }

        /**
         * Removes the listener from this event.
         * @param listenerId An identifier of the listener obtained with {@link #addListener(NotificationListener, Object)}
         *                   method.
         * @return {@literal true} if the listener with the specified ID was registered; otherwise, {@literal false}.
         */
        public final boolean removeListener(final Long listenerId){
            final Lock writeLock = coordinator.writeLock();
            writeLock.lock();
            try{
                return listeners.remove(listenerId) != null;
            }
            finally {
                writeLock.unlock();
            }
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @param option The name of the event option.
         * @param value The value of the event option.
         * @return The previous value of the event option.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final String put(final String option, final String value) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @param option The name of the event option to remove.
         * @return The value of the removed option.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final String remove(final Object option) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception
         * because this map is invoke-only.
         * @param options The map of event options to add.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void putAll(final Map<? extends String, ? extends String> options) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is invoke-only.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private final ConcurrentResourceAccess<Map<String, GenericAttributeMetadata<?>>> attributes;
    private final ConcurrentResourceAccess<Map<String, GenericNotificationMetadata>> notifications;
    private final IllegalStateFlag closed = new IllegalStateFlag() {
        @Override
        public final IllegalStateException newInstance() {
            return new IllegalStateException("Management connector is closed.");
        }
    };

    /**
     * Initializes a new management connector.
     */
    protected AbstractManagementConnector(){
        this.attributes = new ConcurrentResourceAccess<Map<String, GenericAttributeMetadata<?>>>(new HashMap<String, GenericAttributeMetadata<?>>(10));
        this.notifications = new ConcurrentResourceAccess<Map<String, GenericNotificationMetadata>>(new HashMap<String, GenericNotificationMetadata>(10));
    }

    /**
     * Returns all notifications associated with the specified category.
     * @param category The category of the event.
     * @param metadataType The type of requested notification metadata.
     * @param <T> The type of requested notification metadata.
     * @return A map of registered notifications and subscription lists.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final <T extends GenericNotificationMetadata> Map<String, T> getEnabledNotifications(final String category, final Class<T> metadataType){
        return notifications.read(new ConsistentAction<Map<String, GenericNotificationMetadata>, Map<String, T>>() {
            @Override
            public final Map<String, T> invoke(final Map<String, GenericNotificationMetadata> notifications) {
                final Map<String, T> result = new HashMap<>(10);
                for(final Map.Entry<String, GenericNotificationMetadata> metadata: notifications.entrySet())
                    if(Objects.equals(metadata.getValue().getCategory(), category) && metadataType.isInstance(metadata.getValue()))
                        result.put(metadata.getKey(), metadataType.cast(metadata.getValue()));
                return result;
            }
        });
    }

    /**
     * Returns a count of connected attributes.
     * @return The count of connected attributes.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final int attributesCount(){
        return attributes.read(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Integer>() {
            @Override
            public final Integer invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                return attributes.size();
            }
        });
    }

    /**
     *  Throws an {@link IllegalStateException} if the connector is not initialized.
     *  <p>
     *      You should call the base implementation from the overridden method.
     *  </p>
     *  @throws IllegalStateException Connector is not initialized.
     */
    protected void verifyInitialization() throws IllegalStateException{
        closed.verify();
    }

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    protected abstract GenericAttributeMetadata<?> connectAttributeCore(final String attributeName, final Map<String, String> options);

    /**
     * Connects to the specified attribute.
     * Connects to the specified attribute.
     * @param id A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) {
        verifyInitialization();
        return attributes.write(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, AttributeMetadata>() {
            @Override
            public final AttributeMetadata invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                //return existed attribute without exception to increase flexibility of the API
                if(attributes.containsKey(id)) return attributes.get(id);
                final GenericAttributeMetadata<?> attr;
                if((attr = connectAttributeCore(attributeName, options)) != null)
                    attributes.put(id, attr);
                return attr;
            }
        });
    }

    /**
     * Returns the value of the attribute.
     * @param attribute The metadata of the attribute to get.
     * @param readTimeout
     * @param defaultValue The default value of the attribute if reading fails.
     * @return The value of the attribute.
     * @throws TimeoutException
     */
    protected abstract Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to invoke attribute from this connector.
     * @param readTimeout The attribute value invoke operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException{
        verifyInitialization();
        final CountdownTimer timer = CountdownTimer.start(readTimeout);
        return attributes.read(new Action<Map<String, GenericAttributeMetadata<?>>, Object, TimeoutException>() {
            @Override
            public final Object invoke(final Map<String, GenericAttributeMetadata<?>> attributes) throws TimeoutException {
                return getAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime(), defaultValue);
            }
        });
    }

    /**
     * Reads a set of attributes.
     * @param output The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of attributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        final CountdownTimer timer = CountdownTimer.start(readTimeout);
        return attributes.read(new Action<Map<String, GenericAttributeMetadata<?>>, Set<String>, TimeoutException>() {
            @Override
            public final Set<String> invoke(final Map<String, GenericAttributeMetadata<?>> attributes) throws TimeoutException {
                //accumulator for really existed attribute IDs
                final Set<String> result = new HashSet<>(attributes.size());
                final Object missing = new Object(); //this object represents default value for understanding
                //whether the attribute value is unavailable
                timer.stop();
                for(final String id: output.keySet()){
                    timer.start();
                    final Object value = getAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime(), missing);
                    if(value != missing) { //attribute value is available
                        result.add(id);
                        output.put(id, value);
                    }
                }
                return result;
            }
        });
    }

    /**
     * Sends the attribute value to the remote agent.
     * @param attribute The metadata of the attribute to set.
     * @param writeTimeout
     * @param value
     * @return {@literal true} if attribute value is overridden successfully; otherwise, {@literal false}.
     */
    protected abstract boolean setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value);

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws TimeoutException The attribute value cannot be write in the specified duration.
     */
    @Override
    public final boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException {
        verifyInitialization();
        final CountdownTimer timer = CountdownTimer.start(writeTimeout);
        return attributes.write(new Action<Map<String, GenericAttributeMetadata<?>>, Boolean, TimeoutException>() {
            @Override
            public final Boolean invoke(final Map<String, GenericAttributeMetadata<?>> attributes) throws TimeoutException {
                return attributes.containsKey(id) ? setAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime(), value) : false;
            }
        });
    }

    /**
     * Writes a set of attributes inside of the transaction.
     * @param values The dictionary of attributes keys and its values.
     * @param writeTimeout Batch write timeout.
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws TimeoutException
     */
    @Override
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException {
        final CountdownTimer timer = CountdownTimer.start(writeTimeout);
        return attributes.write(new Action<Map<String, GenericAttributeMetadata<?>>, Boolean, TimeoutException>() {
            @Override
            public final Boolean invoke(final Map<String, GenericAttributeMetadata<?>> attributes) throws TimeoutException {
                boolean result = true;
                final Object missing = new Object(); //this object represents default value for understanding
                //whether the attribute value is unavailable
                timer.stop();
                for(final Map.Entry<String, Object> entry: values.entrySet()){
                    timer.start();
                    result &= setAttributeValue(attributes.get(entry.getKey()), timer.stopAndGetElapsedTime(), entry.getValue());
                }
                return result;
            }
        });
    }

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    protected boolean disconnectAttributeCore(final String id){
        return true;
    }

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public final boolean disconnectAttribute(final String id) {
        verifyInitialization();
        return attributes.write(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Boolean>() {
            @Override
            public final Boolean invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                if (attributes.containsKey(id) && disconnectAttributeCore(id)) {
                    attributes.remove(id);
                    return true;
                } else return false;
            }
        });
    }

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    @Override
    public final AttributeMetadata getAttributeInfo(final String id) {
        verifyInitialization();
        return attributes.read(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, AttributeMetadata>() {
            @Override
            public final AttributeMetadata invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                return attributes.get(id);
            }
        });
    }

    /**
     * Returns a read-only collection of registered attributes.
     *
     * @return A read-only collection of registered attributes.
     */
    @Override
    public final Collection<String> getRegisteredAttributes() {
        return attributes.read(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Collection<String>>() {
            @Override
            public final Collection<String> invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                return Collections.unmodifiableCollection(attributes.keySet());
            }
        });
    }

    /**
     * Enables event listening for the specified category of events.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param category The name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    protected GenericNotificationMetadata enableNotificationsCore(final String category, final Map<String, String> options){
        return null;
    }

    /**
     * Enables event listening for the specified category of events.
     * @param listId An identifier of the subscription list.
     * @param category A name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    @Override
    public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) {
        verifyInitialization();
        return notifications.write(new ConsistentAction<Map<String, GenericNotificationMetadata>, NotificationMetadata>() {
            @Override
            public final NotificationMetadata invoke(final Map<String, GenericNotificationMetadata> notifications) {
                if(notifications.containsKey(category)) return notifications.get(category);
                final GenericNotificationMetadata metadata = enableNotificationsCore(category, options);
                if(metadata != null) notifications.put(listId, metadata);
                return metadata;
            }
        });
    }

    /**
     * Disable all notifications associated with the specified event.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param notificationType The event descriptor.
     */
    protected void disableNotificationsCore(final NotificationMetadata notificationType){
    }

    /**
     * Disables event listening for the specified category of events.
     *
     * @param listId An identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    @Override
    public final boolean disableNotifications(final String listId) {
        verifyInitialization();
        return notifications.write(new ConsistentAction<Map<String, GenericNotificationMetadata>, Boolean>() {
            @Override
            public final Boolean invoke(final Map<String, GenericNotificationMetadata> notifications) {
                if(notifications.containsKey(listId)){
                    disableNotificationsCore(notifications.remove(listId));
                    return true;
                }
                else return false;
            }
        });
    }

    /**
     * Gets the notification metadata by its category.
     *
     * @param listId An identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     *         for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    @Override
    public final NotificationMetadata getNotificationInfo(final String listId) {
        return notifications.read(new ConsistentAction<Map<String, GenericNotificationMetadata>, NotificationMetadata>() {
            @Override
            public final NotificationMetadata invoke(final Map<String, GenericNotificationMetadata> notifications) {
                return notifications.get(listId);
            }
        });
    }

    /**
     * Adds a new listener for the specified notification.
     * @param notificationType The event type.
     * @param listener The event listener.
     * @return Any custom data associated with the subscription.
     */
    protected Object subscribeCore(final NotificationMetadata notificationType, final NotificationListener listener){
        return null;
    }

    /**
     * Attaches the notification listener.
     *
     * @param listId An identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final Long subscribe(final String listId, final NotificationListener listener) {
        verifyInitialization();
        return notifications.read(new ConsistentAction<Map<String, GenericNotificationMetadata>, Long>() {
            @Override
            public final Long invoke(final Map<String, GenericNotificationMetadata> notifications) {
                final GenericNotificationMetadata metadata = notifications.get(listId);
                if(metadata == null) return null;
                final Object userData = subscribeCore(metadata, listener);
                return metadata.addListener(listener, userData);
            }
        });
    }

    /**
     * Cancels the notification listening.
     * @param metadata The event type.
     * @param listener The notification listener to remove.
     * @param data The custom data associated with subscription that returned from {@link #subscribeCore(NotificationMetadata, NotificationListener)}
     *             method.
     */
    protected void unsubscribeCore(final NotificationMetadata metadata, final NotificationListener listener, final Object data){

    }

    /**
     * Removes the notification listener.
     * @param listenerId An identifier of the notification listener previously returned
     *                   by {@link #subscribe(String, NotificationListener)} method.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final boolean unsubscribe(final Long listenerId){
        verifyInitialization();
        return notifications.read(new ConsistentAction<Map<String, GenericNotificationMetadata>, Boolean>() {
            @Override
            public final Boolean invoke(final Map<String, GenericNotificationMetadata> notifications) {
                for(final GenericNotificationMetadata metadata: notifications.values()){
                    if(metadata.hasListener(listenerId)){
                        final Pair<NotificationListener, Object> pair = metadata.getListener(listenerId);
                        if(pair == null) return false;
                        unsubscribeCore(metadata, pair.first, pair.second);
                        return metadata.removeListener(listenerId);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Removes the notification listener.
     *
     * @param listenerId An identifier previously returned by {@link #subscribe(String, com.snamp.connectors.NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean unsubscribe(final Object listenerId)  {
        return listenerId instanceof Long && unsubscribe((Long)listenerId);
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        //remove all registered attributes
        attributes.write(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Void>() {
            @Override
            public final Void invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                attributes.clear();
                return null;
            }
        });
        //remove all registered notification listeners
        notifications.write(new ConsistentAction<Map<String, GenericNotificationMetadata>, Void>() {
            @Override
            public final Void invoke(final Map<String, GenericNotificationMetadata> notifications) {
                notifications.clear();
                return null;
            }
        });
        //change state of the connector
        closed.set();
    }
}
