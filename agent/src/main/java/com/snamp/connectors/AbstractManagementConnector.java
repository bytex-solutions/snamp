package com.snamp.connectors;

import com.snamp.*;

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
public abstract class AbstractManagementConnector implements ManagementConnector {

    /**
     * Represents default implementation of the attribute descriptor.
     * @param <T> Type of the attribute type descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class GenericAttributeMetadata<T extends AttributeTypeInfo> implements AttributeMetadata {
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
         * this map is read-only.
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
         * this map is read-only.
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
         * this map is read-only.
         * @param options A map of attribute discovery options.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void putAll(Map<? extends String, ? extends String> options) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is read-only.
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
         * By default, returns {@literal true}.
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
        private final String eventCategory;
        private final AtomicLong counter;
        private final Map<Long, NotificationListener<? extends Notification>> listeners;
        private final ReadWriteLock coordinator;

        /**
         * Initializes a new event metadata.
         * @param category The category of the event.
         */
        protected GenericNotificationMetadata(final String category){
            this.eventCategory = category;
            this.listeners = new HashMap<>(10);
            this.counter = new AtomicLong(0);
            this.coordinator = new ReentrantReadWriteLock();
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
         * Adds a new listener for this event.
         * @param listener The notification listener.
         * @return A new unique identifier of the added listener.
         */
        public final Long addListener(final NotificationListener<? extends Notification> listener){
            final Long listenerId = counter.getAndIncrement();
            final Lock writeLock = coordinator.writeLock();
            try{
                listeners.put(listenerId, listener);
            }
            finally {
                writeLock.unlock();
            }
            return listenerId;
        }

        /**
         * Removes the listener from this event.
         * @param listenerId An identifier of the listener obtained with {@link #addListener(NotificationListener)}
         *                   method.
         * @return {@literal true} if the listener with the specified ID was registered; otherwise, {@literal false}.
         */
        public final boolean removeListener(final Long listenerId){
            final Lock writeLock = coordinator.writeLock();
            try{
                return listeners.remove(listenerId) != null;
            }
            finally {
                writeLock.unlock();
            }
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is read-only.
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
         * this map is read-only.
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
         * because this map is read-only.
         * @param options The map of event options to add.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void putAll(final Map<? extends String, ? extends String> options) {
            throw new UnsupportedOperationException();
        }

        /**
         * Always throws {@link UnsupportedOperationException} exception because
         * this map is read-only.
         * @throws UnsupportedOperationException This operation is not supported.
         */
        @Override
        public final void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private final ReadWriteLock coordinator; //transaction coordinator
    private final Map<String, GenericAttributeMetadata<?>> attributes;
    private final Map<String, GenericNotificationMetadata> notifications;

    /**
     * Initializes a new management connector.
     */
    protected AbstractManagementConnector(){
        this.attributes = new HashMap<>();
        this.coordinator = new ReentrantReadWriteLock();
        this.notifications = new HashMap<>();
    }

    /**
     * Returns a count of connected attributes.
     * @return The count of connected attributes.
     */
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    protected final int attributesCount(){
        return attributes.size();
    }

    /**
     *  Throws an exception if the connector is not initialized.
     */
    protected abstract void verifyInitialization();

    /**
     * Connects to the specified attribute.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    protected abstract GenericAttributeMetadata<?> connectAttributeCore(final String attributeName, final Map<String, String> options);

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options Attribute discovery options.
     * @return The description of the attribute.
     */
    @Override
    public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) {
        verifyInitialization();
        final Lock writeLock =  coordinator.writeLock();
        writeLock.lock();
        try {
            //return existed attribute without exception to increase flexibility of the API
            if(attributes.containsKey(id)) return attributes.get(id);
            final GenericAttributeMetadata<?> attr;
            if((attr = connectAttributeCore(attributeName, options)) != null)
                attributes.put(id, attr);
            return attr;
        }
        finally {
            writeLock.unlock();
        }
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
     * @param id  A key string that is used to read attribute from this connector.
     * @param readTimeout The attribute value read operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException{
        final CountdownTimer timer = new CountdownTimer(readTimeout);
        final Lock readLock = coordinator.readLock();
        timer.start();
        if(readTimeout == TimeSpan.INFINITE) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.duration, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
           return defaultValue;
        }
        timer.stop();
        //read lock is acquired, forces the custom reading operation
        try{
            return getAttributeValue(attributes.get(id), timer.getElapsedTime(), defaultValue);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Reads a set of attributes.
     * @param output The dictionary with set of attribute keys to read and associated default values.
     * @param readTimeout The attribute value read operation timeout.
     * @return The set of attributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be read in the specified duration.
     */
    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        final Lock readLock = coordinator.readLock();
        final CountdownTimer timer = new CountdownTimer(readTimeout);
        timer.start();
        if(readTimeout == TimeSpan.INFINITE) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.duration, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return new HashSet<>();
        }
        timer.stop();
        //accumulator for really existed attribute IDs
        final Set<String> result = new HashSet<>();
        try{
            final Object missing = new Object(); //this object represents default value for understanding
            //whether the attribute value is unavailable
            for(final String id: output.keySet()){
                timer.start();
                final Object value = getAttributeValue(attributes.get(id), timer.getElapsedTime(), missing);
                if(value != missing) { //attribute value is available
                    result.add(id);
                    output.put(id, value);
                }
                timer.stop();
            }
        }
        finally {
            readLock.unlock();
        }
        return result;
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
        final Lock writeLock = coordinator.writeLock();
        final CountdownTimer timer = new CountdownTimer(writeTimeout);
        timer.start();
        if(writeTimeout == TimeSpan.INFINITE) writeLock.lock();
        else try {
            if(!writeLock.tryLock(writeTimeout.duration, writeTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return false;
        }
        timer.stop();
        try{
            return attributes.containsKey(id) ? setAttributeValue(attributes.get(id), timer.getElapsedTime(), value) : false;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Writes a set of attributes inside of the transaction.
     * @param values The dictionary of attributes keys and its values.
     * @param writeTimeout
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws TimeoutException
     */
    @Override
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException {
        final Lock writeLock = coordinator.writeLock();
        final CountdownTimer timer = new CountdownTimer(writeTimeout);
        timer.start();
        if(writeTimeout == TimeSpan.INFINITE) writeLock.lock();
        else try {
            if(!writeLock.tryLock(writeTimeout.duration, writeTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return false;
        }
        timer.stop();
        boolean result = true;
        try{
            final Object missing = new Object(); //this object represents default value for understanding
            //whether the attribute value is unavailable
            for(final Map.Entry<String, Object> entry: values.entrySet()){
                timer.start();
                result &= setAttributeValue(attributes.get(entry.getKey()), timer.getElapsedTime(), entry.getValue());
                timer.stop();
            }
        }
        finally {
            writeLock.unlock();
        }
        return result;
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
        final Lock writeLock = coordinator.writeLock();
        writeLock.lock();
        try{
            if(attributes.containsKey(id) && disconnectAttributeCore(id)){
                attributes.remove(id);
                return true;
            }
            else return false;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    @Override
    public final AttributeMetadata getAttributeInfo(final String id) {
        final Lock readLock = coordinator.readLock();
        readLock.lock();
        try {
            return attributes.get(id);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Returns an iterator through attribute identifiers.
     * @return An iterator through attribute identifiers.
     */
    @Override
    public final Iterator<String> iterator() {
        return attributes.keySet().iterator();
    }

    /**
     * Enables event listening for the specified category of events.
     *
     * @param category The name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    protected GenericNotificationMetadata enableNotificationsCore(final String category, final Map<String, String> options){
        return null;
    }

    /**
     * Enables event listening for the specified category of events.
     *
     * @param category The name of the category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    @Override
    public final NotificationMetadata enableNotifications(final String category, final Map<String, String> options) {
        final Lock writeLock = coordinator.writeLock();
        try{
            if(notifications.containsKey(category)) return notifications.get(category);
            final GenericNotificationMetadata metadata = enableNotificationsCore(category, options);
            if(metadata != null) notifications.put(category, metadata);
            return metadata;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Disable all notifications associated with the specified event.
     * @param notificationType The event descriptor.
     */
    protected void disableNotificationsCore(final NotificationMetadata notificationType){
    }

    /**
     * Disables event listening for the specified category of events.
     *
     * @param category The name of the event category.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    @Override
    public final boolean disableNotifications(final String category) {
        final Lock writeLock = coordinator.writeLock();
        writeLock.lock();
        try{
            if(notifications.containsKey(category)){
                disableNotificationsCore(notifications.remove(category));
                return true;
            }
            else return false;
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets the notification metadata by its category.
     *
     * @param category The category of the notification.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     *         for the specified category is not enabled by {@link #enableNotifications(String, java.util.Map)} method.
     */
    @Override
    public final NotificationMetadata getNotificationInfo(final String category) {
        final Lock readLock = coordinator.readLock();
        try{
            return notifications.get(category);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Adds a new listener for the specified notification.
     * @param notificationType The event type.
     * @param listener The event listener.
     */
    protected void subscribeCore(final NotificationMetadata notificationType, final NotificationListener<? extends Notification> listener){

    }

    /**
     * Attaches the notification listener.
     *
     * @param category The category of the event to listen.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    @Override
    public final Long subscribe(final String category, final NotificationListener<? extends Notification> listener) {
        final Lock readLock = coordinator.readLock();
        try{
            final GenericNotificationMetadata metadata = notifications.get(category);
            if(metadata == null) return null;
            subscribeCore(metadata, listener);
            return metadata.addListener(listener) ^ category.hashCode();
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Cancels the notification listening.
     * @param metadata The event type.
     * @param listener The notification listener to remove.
     */
    protected void unsubscribeCore(final NotificationMetadata metadata, final NotificationListener listener){

    }

    /**
     * Removes the notification listener.
     * @param listenerId An identifier of the notification listener previously returned
     *                   by {@link #subscribe(String, NotificationListener)} method.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final boolean unsubscribe(final long listenerId){
        final Lock readLock = coordinator.readLock();
        readLock.lock();
        try{
            for(final String category: notifications.keySet()){
                final int categoryHash = category.hashCode();
                if((categoryHash ^ listenerId) == categoryHash)
                    return notifications.get(category).removeListener(listenerId);
            }

        }
        finally {
            readLock.unlock();
        }
        return false;
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
}
