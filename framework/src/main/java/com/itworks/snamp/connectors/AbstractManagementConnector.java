package com.itworks.snamp.connectors;

import com.itworks.snamp.*;
import com.itworks.snamp.core.AbstractFrameworkService;
import com.itworks.snamp.internal.*;
import com.itworks.snamp.connectors.util.NotificationListenerInvoker;

import static com.itworks.snamp.AbstractConcurrentResourceAccess.*;
import static com.itworks.snamp.connectors.NotificationSupport.NotificationListener;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.*;
import java.util.logging.Logger;

/**
 * Represents an abstract class for building custom management connectors.
 * @param <TConnectionOptions> The management connection initialization options.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public abstract class AbstractManagementConnector<TConnectionOptions> extends AbstractFrameworkService implements ManagementConnector<TConnectionOptions> {

    /**
     * Represents default implementation of the attribute descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class GenericAttributeMetadata<T extends ManagementEntityType> implements AttributeMetadata {
        /**
         * Represents the name of the attribute configuration parameter that assigns the display
         * name for the attribute.
         * <p>
         *     If you want to store display names for each language then you should use the following notation:
         *     displayName.en-US,
         *     display.ru-RU
         * </p>
         */
        public static final String DISPLAY_NAME_PARAM = "displayName";

        /**
         * Represents the name of the attribute configuration parameters that assigns the
         * description for the attribute.
         * <p>
         *     If you want to store display names for each language then you should use the following notation:
         *     description.en-US,
         *     description.ru-RU
         * </p>
         */
        public static final String DESCRIPTION_PARAM = "description";

        private final String attributeName;
        private T attributeType;

        /**
         * Initializes a new attribute metadata.
         * @param attributeName The name of the attribute. Cannot be {@literal null}.
         * @throws IllegalArgumentException attributeName or namespace is {@literal null}.
         */
        public GenericAttributeMetadata(final String attributeName){
            if(attributeName == null) throw new IllegalArgumentException("attributeName is null.");
            this.attributeName = attributeName;
        }

        private String readLocalizedParam(String paramName, final Locale locale, final String defaultValue){
            if(locale == null)
                return containsKey(paramName) ? get(paramName) : defaultValue;
            else{
                paramName = String.format("%s.%s", paramName, locale.toLanguageTag());
                return containsKey(paramName) ? get(paramName) : defaultValue;
            }
        }

        /**
         * Returns the localized description of this attribute.
         * <p>
         *     In the default implementation, this method reads description from {@link #DESCRIPTION_PARAM}
         *     attribute configuration parameter.
         * </p>
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this attribute.
         * @see #DESCRIPTION_PARAM
         */
        @Override
        public String getDescription(final Locale locale) {
            return readLocalizedParam(DESCRIPTION_PARAM, locale, "");
        }

        /**
         * Returns the localized name of this attribute.
         * <p>
         *     In the default implementation, this method reads description from {@link #DISPLAY_NAME_PARAM}
         *     attribute configuration parameter.
         * </p>
         * @param locale The locale of the display name. If it is {@literal null} then returns display name
         *               in the default locale.
         * @return The localized name of this attribute.
         * @see #DISPLAY_NAME_PARAM
         */
        @Override
        public String getDisplayName(final Locale locale) {
            return readLocalizedParam(DISPLAY_NAME_PARAM, locale, attributeName);
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
        public final T getType() {
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
        @SuppressWarnings("NullableProblems")
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
        public final String getName() {
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
        private static final String UNKNOWN_LIST_ID = "unknown";
        /**
         * Represents subscribed version of the notification listener. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        protected static final class SubscribedNotificationListener implements NotificationListener{
            /**
             * Represents user data associated with the notification listener.
             */
            public final Object userData;

            /**
             * Represents underlying notification listener.
             */
            public final NotificationListener listener;

            /**
             * Initializes a new subscribed version of the notification listener.
             * @param listener A listener to wrap. Cannot be {@literal null}.
             * @param userData User data associated with the notification listener.
             * @throws IllegalArgumentException listener is {@literal null}.
             */
            public SubscribedNotificationListener(final NotificationListener listener, final Object userData){
                if(listener == null) throw new IllegalArgumentException("listener is null.");
                this.userData = userData;
                this.listener = listener;
            }

            /**
             * Initializes a new subscribed version of the notification listener.
             * @param listener A listener to wrap. Cannot be {@literal null}.
             * @throws IllegalArgumentException listener is {@literal null}.
             */
            @SuppressWarnings("UnusedDeclaration")
            public SubscribedNotificationListener(final NotificationListener listener){
                this(listener, null);
            }

            /**
             * Handles the specified notification.
             *
             * @param listId An identifier of the subscription list.
             * @param n The notification to handle.
             * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
             */
            @Override
            public final boolean handle(final String listId, final NotificationSupport.Notification n) {
                return listener.handle(listId, n);
            }
        }

        private final String eventCategory;
        private final Map<String, SubscribedNotificationListener> listeners;
        private final ReadWriteLock coordinator;
        private String subscriptionList;

        /**
         * Initializes a new event metadata.
         * @param category The category of the event.
         */
        protected GenericNotificationMetadata(final String category){
            this.eventCategory = category;
            this.listeners = new HashMap<>(10);
            this.coordinator = new ReentrantReadWriteLock();
            this.subscriptionList = UNKNOWN_LIST_ID;
        }

        private void setSubscriptionList(final String value){
            this.subscriptionList = value != null && value.length() > 0 ? value : UNKNOWN_LIST_ID;
        }

        /**
         * Gets subscription list identifier.
         * @return A subscription list identifier.
         */
        protected final String getSubscriptionList(){
            return subscriptionList;
        }

        /**
         * Returns the localized description of this management entity.
         * <p>
         *     In the default implementation, this method returns an empty string.
         * </p>
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this management entity.
         */
        @Override
        public final String getDescription(final Locale locale) {
            return "";
        }

        /**
         * Fires the notification listeners.
         * @param n The notification to pass into listeners.
         * @param invoker Notification listener invoker.
         * @see NotificationListenerInvoker
         * @see com.itworks.snamp.connectors.util.NotificationListenerInvokerFactory
         */
        protected final void fire(final NotificationSupport.Notification n, final NotificationListenerInvoker invoker){
            final Lock readLock = coordinator.readLock();
            readLock.lock();
            try{
                invoker.invoke(subscriptionList, n, listeners.values());
            }
            finally {
                readLock.unlock();
            }
        }

        final boolean hasListener(final String listenerId){
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
         * Removes all listeners.
         */
        public final void removeListeners(){
            final Lock writeLock = coordinator.readLock();
            writeLock.lock();
            try{
                listeners.clear();
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
         final SubscribedNotificationListener getListener(final String listenerId){
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
         * @param listenerId Unique identifier of the listener.
         * @param listener The notification listener.
         * @param userData The user data associated with the listener.
         * @return A new unique identifier of the added listener.
         */
        public final boolean addListener(final String listenerId, final NotificationListener listener, final Object userData){
            if(listenerId == null || listenerId.isEmpty()) return false;
            final Lock writeLock = coordinator.writeLock();
            writeLock.lock();
            try{
                if(listeners.containsKey(listenerId))
                    return false;
                else {
                    listeners.put(listenerId, new SubscribedNotificationListener(listener, userData));
                    return true;
                }
            }
            finally {
                writeLock.unlock();
            }
        }

        /**
         * Removes the listener from this event.
         * @param listenerId An identifier of the listener obtained with {@link #addListener(String, NotificationListener, Object)}
         *                   method.
         * @return {@literal true} if the listener with the specified ID was registered; otherwise, {@literal false}.
         */
        public final boolean removeListener(final String listenerId){
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
        @SuppressWarnings("NullableProblems")
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

    /**
     * Represents a base class that allows to enable notification support for the management connector.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractNotificationSupport implements NotificationSupport{
        private final ConcurrentResourceAccess<KeyedObjects<String, GenericNotificationMetadata>> notifications;

        /**
         * Initializes a new notification manager.
         */
        protected AbstractNotificationSupport(){
            this.notifications = new ConcurrentResourceAccess<KeyedObjects<String, GenericNotificationMetadata>>(new AbstractKeyedObjects<String, GenericNotificationMetadata>(10) {
                @Override
                public final String getKey(final GenericNotificationMetadata item) {
                    return item.getSubscriptionList();
                }
            });
        }

        /**
         * Returns a read-only collection of enabled notifications (subscription list identifiers).
         *
         * @return A read-only collection of enabled notifications (subscription list identifiers).
         */
        @Override
        public final Collection<String> getEnabledNotifications() {
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Collection<String>>() {
                @Override
                public Collection<String> invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    return notifications.keySet();
                }
            });
        }

        /**
         * Returns all notifications associated with the specified category.
         * @param category The category of the event.
         * @param metadataType The type of requested notification metadata.
         * @param <T> The type of requested notification metadata.
         * @return A map of registered notifications (values) and subscription lists (keys).
         */
        @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
        protected final <T extends GenericNotificationMetadata> Map<String, T> getEnabledNotifications(final String category, final Class<T> metadataType){
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Map<String, T>>() {
                @Override
                public final Map<String, T> invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    final Map<String, T> result = new HashMap<>(notifications.size());
                    for(final Map.Entry<String, GenericNotificationMetadata> metadata: notifications.entrySet())
                        if(Objects.equals(metadata.getValue().getCategory(), category) && metadataType.isInstance(metadata.getValue()))
                            result.put(metadata.getKey(), metadataType.cast(metadata.getValue()));
                    return result;
                }
            });
        }

        /**
         * Returns a collection of active categories.
         * @return A collection of active categories.
         */
        @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
        protected final Set<String> getCategories(){
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Set<String>>() {
                @Override
                public Set<String> invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    final Set<String> categories = new HashSet<>(10);
                    for(final GenericNotificationMetadata eventData: notifications.values())
                        categories.add(eventData.getCategory());
                    return categories;
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
        protected abstract GenericNotificationMetadata enableNotifications(final String category, final Map<String, String> options);

        /**
         * Enables event listening for the specified category of events.
         * @param listId An identifier of the subscription list.
         * @param category A name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         */
        @Override
        public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) {
            return notifications.write(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, NotificationMetadata>() {
                @Override
                public final NotificationMetadata invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    if(notifications.containsKey(category)) return notifications.get(category);
                    final GenericNotificationMetadata metadata = enableNotifications(category, options);
                    if(metadata != null) {
                        metadata.setSubscriptionList(listId);
                        notifications.put(metadata);
                    }
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
        protected void disableNotifications(final GenericNotificationMetadata notificationType){
            notificationType.removeListeners();
        }

        /**
         * Disables event listening for the specified category of events.
         *
         * @param listId An identifier of the subscription list.
         * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
         */
        @Override
        public final boolean disableNotifications(final String listId) {
            return notifications.write(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Boolean>() {
                @Override
                public final Boolean invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    if (notifications.containsKey(listId)) {
                        disableNotifications(notifications.remove(listId));
                        return true;
                    } else return false;
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
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, NotificationMetadata>() {
                @Override
                public final NotificationMetadata invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    return notifications.get(listId);
                }
            });
        }

        /**
         * Adds a new listener for the specified notification.
         * @param listener The event listener.
         * @return Any custom data associated with the subscription.
         */
        protected abstract Object subscribe(final NotificationListener listener);

        /**
         * Attaches the notification listener.
         *
         * @param listenerId An identifier of the notification listener.
         * @param listener The notification listener.
         * @return An identifier of the notification listener generated by this connector.
         */
        @Override
        public final boolean subscribe(final String listenerId, final NotificationListener listener) {
            final Object userData = subscribe(listener);
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Boolean>() {
                @Override
                public final Boolean invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    for(final GenericNotificationMetadata metadata: notifications.values())
                        metadata.addListener(listenerId, listener, userData);
                    return notifications.size() > 0;
                }
            });
        }

        /**
         * Cancels the notification listening.
         * @param listener The notification listener to remove.
         * @param data The custom data associated with subscription that returned from {@link #subscribe(NotificationListener)}
         *             method.
         */
        protected abstract void unsubscribe(final NotificationListener listener, final Object data);

        /**
         * Removes the notification listener.
         * @param listenerId An identifier of the notification listener previously passed
         *                   to {@link #subscribe(String, NotificationListener)} method.
         * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
         */
        public final boolean unsubscribe(final String listenerId){
            return notifications.read(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Boolean>() {
                @Override
                public final Boolean invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    for(final GenericNotificationMetadata metadata: notifications.values()){
                        if(metadata.hasListener(listenerId)){
                            final GenericNotificationMetadata.SubscribedNotificationListener pair = metadata.getListener(listenerId);
                            if(pair == null) return false;
                            unsubscribe(pair.listener, pair.userData);
                            return metadata.removeListener(listenerId);
                        }
                    }
                    return false;
                }
            });
        }

        /**
         * Removes all listeners from this notification manager.
         * <p>
         *     It is recommended to call this method in the implementation of {@link AutoCloseable#close()}
         *     method in your management connector.
         * </p>
         */
        public final void clear(){
            notifications.write(new ConsistentAction<KeyedObjects<String, GenericNotificationMetadata>, Void>() {
                @Override
                public final Void invoke(final KeyedObjects<String, GenericNotificationMetadata> notifications) {
                    notifications.clear();
                    return null;
                }
            });
        }
    }

    private final TConnectionOptions connectionOptions;
    private final ConcurrentResourceAccess<Map<String, GenericAttributeMetadata<?>>> attributes;
    private final IllegalStateFlag closed = new IllegalStateFlag() {
        @Override
        public final IllegalStateException create() {
            return new IllegalStateException("Management connector is closed.");
        }
    };

    /**
     * Initializes a new management connector.
     * @param connectionOptions Management connector initialization options.
     * @param logger A logger for this management connector.
     */
    protected AbstractManagementConnector(final TConnectionOptions connectionOptions, final Logger logger){
        super(logger);
        if(connectionOptions == null) throw new IllegalArgumentException("connectionOptions is null.");
        else this.connectionOptions = connectionOptions;
        this.attributes = new ConcurrentResourceAccess<Map<String, GenericAttributeMetadata<?>>>(new HashMap<String, GenericAttributeMetadata<?>>(10));
    }

    /**
     * Returns connection options used by this management connector.
     *
     * @return The connection options used by this management connector.
     */
    @Override
    public final TConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    /**
     * Returns a count of connected managementAttributes.
     * @return The count of connected managementAttributes.
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
    protected abstract GenericAttributeMetadata<?> connectAttribute(final String attributeName, final Map<String, String> options);

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
                if((attr = connectAttribute(attributeName, options)) != null)
                    attributes.put(id, attr);
                return attr;
            }
        });
    }

    /**
     * Returns the value of the attribute.
     * @param attribute The metadata of the attribute to get.
     * @param readTimeout The attribute value invoke operation timeout.
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
     * Reads a set of managementAttributes.
     * @param output The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
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
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
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
                return attributes.containsKey(id) && setAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime(), value);
            }
        });
    }

    /**
     * Writes a set of managementAttributes inside of the transaction.
     * @param values The dictionary of managementAttributes keys and its values.
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
     * @param attributeInfo An attribute metadata.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @SuppressWarnings("UnusedParameters")
    protected boolean disconnectAttribute(final String id, final GenericAttributeMetadata<?> attributeInfo){
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
                if (attributes.containsKey(id) && disconnectAttribute(id, attributes.get(id))) {
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
     * Returns a read-only collection of registered managementAttributes.
     *
     * @return A read-only collection of registered managementAttributes.
     */
    @Override
    public final Collection<String> getConnectedAttributes() {
        return attributes.read(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Collection<String>>() {
            @Override
            public final Collection<String> invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                return Collections.unmodifiableCollection(attributes.keySet());
            }
        });
    }

    /**
     * Releases all resources associated with this connector.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        //remove all registered managementAttributes
        attributes.write(new ConsistentAction<Map<String, GenericAttributeMetadata<?>>, Void>() {
            @Override
            public final Void invoke(final Map<String, GenericAttributeMetadata<?>> attributes) {
                attributes.clear();
                return null;
            }
        });
        //change state of the connector
        closed.set();
    }

    /**
     * Returns logger name based on the management connector name.
     * @param connectorName The name of the connector.
     * @return The logger name.
     */
    public static String getLoggerName(final String connectorName){
        return String.format("itworks.snamp.connectors.%s", connectorName);
    }

    /**
     * Returns a logger associated with the specified management connector.
     * @param connectorName The name of the connector.
     * @return An instance of the logger.
     */
    public static Logger getLogger(final String connectorName){
        return Logger.getLogger(getLoggerName(connectorName));
    }
}
