package com.itworks.snamp.connectors;

import com.google.common.collect.Maps;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.AbstractFrameworkService;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.CountdownTimer;
import com.itworks.snamp.internal.IllegalStateFlag;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.annotations.ThreadSafe;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an abstract class for building custom management connectors.
 * <p>
 *     This class provides a base support for the following management mechanisms:
 *     <ul>
 *         <li>{@link com.itworks.snamp.connectors.AbstractManagedResourceConnector.AbstractAttributeSupport} for resource management using attributes.</li>
 *         <li>{@link com.itworks.snamp.connectors.AbstractManagedResourceConnector.AbstractNotificationSupport} to receive management notifications from the managed resource.</li>
 *     </ul>
 * </p>
 * @param <TConnectionOptions> The management connection initialization options.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractManagedResourceConnector<TConnectionOptions> extends AbstractFrameworkService implements ManagedResourceConnector<TConnectionOptions> {

    /**
     * Represents default implementation of the attribute descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class GenericAttributeMetadata<T extends ManagedEntityType> implements AttributeMetadata {
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
        private volatile T attributeType;

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
            // use a temporary variable to reduce the number of reads of the
            // volatile field
            T result = attributeType;
            if (result == null)
                synchronized (this) {
                    result = attributeType;
                    if (result == null)
                        result = attributeType = detectAttributeType();
                }
            return result;
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
     * Provides a base support of management attributes.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractAttributeSupport extends ThreadSafeObject implements AttributeSupport {
        private final Map<String, GenericAttributeMetadata<?>> attributes;

        /**
         * Initializes a new support of management attributes.
         */
        protected AbstractAttributeSupport() {
            attributes = new HashMap<>(10);
        }

        /**
         * Returns a count of connected managementAttributes.
         *
         * @return The count of connected managementAttributes.
         */
        @ThreadSafe
        protected final int attributesCount() {
            beginRead();
            try {
                return attributes.size();
            } finally {
                endRead();
            }
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The name of the attribute.
         * @param options       Attribute discovery options.
         * @return The description of the attribute.
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract GenericAttributeMetadata<?> connectAttribute(final String attributeName, final Map<String, String> options) throws Exception;

        /**
         * Connects to the specified attribute.
         * Connects to the specified attribute.
         *
         * @param id            A key string that is used to invoke attribute from this connector.
         * @param attributeName The name of the attribute.
         * @param options       Attribute discovery options.
         * @return The description of the attribute.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        @Override
        @ThreadSafe
        public final AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) throws AttributeSupportException{
            beginWrite();
            try {
                //return existed attribute without exception to increase flexibility of the API
                if (attributes.containsKey(id)) return attributes.get(id);
                final GenericAttributeMetadata<?> attr;
                if ((attr = connectAttribute(attributeName, options)) != null)
                    attributes.put(id, attr);
                return attr;
            }
            catch (final Exception e){
                failedToConnectAttribute(id, attributeName, e);
                throw new AttributeSupportException(e);
            }
            finally {
                endWrite();
            }
        }

        /**
         * Reports an error when connecting attribute.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param attributeID The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e Internal connector error.
         */
        protected static void failedToConnectAttribute(final Logger logger,
                                                       final Level logLevel,
                                                       final String attributeID,
                                                       final String attributeName,
                                                       final Exception e){
            logger.log(logLevel, String.format("Failed to connect attribute %s with ID %s. Context: %s",
                    attributeName, attributeID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when connecting attribute.
         * @param attributeID The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        protected abstract void failedToConnectAttribute(final String attributeID,
                                                         final String attributeName,
                                                         final Exception e);

        /**
         * Returns the value of the attribute.
         *
         * @param attribute    The metadata of the attribute to get.
         * @param readTimeout  The attribute value invoke operation timeout.
         * @return The value of the attribute.
         * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract Object getAttributeValue(final AttributeMetadata attribute, final TimeSpan readTimeout) throws Exception;

        /**
         * Returns the attribute value.
         *
         * @param id           A key string that is used to invoke attribute from this connector.
         * @param readTimeout  The attribute value invoke operation timeout.
         * @return The value of the attribute, or default value.
         * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
         * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The requested attribute doesn't exist.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        @Override
        @ThreadSafe
        public final Object getAttribute(final String id, final TimeSpan readTimeout) throws TimeoutException,
                UnknownAttributeException,
                AttributeSupportException {
            final CountdownTimer timer = CountdownTimer.start(readTimeout);
            beginRead();
            try {
                if (attributes.containsKey(id))
                    return getAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime());
                else throw new UnknownAttributeException(id);
            } catch (final UnknownAttributeException | TimeoutException e) {
                throw e;
            } catch (final Exception e) {
                failedToGetAttribute(id, e);
                throw new AttributeSupportException(e);
            } finally {
                endRead();
            }
        }

        /**
         * Reports an error when getting attribure.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param attributeID The attribute identifier.
         * @param e Internal connector error.
         */
        protected static void failedToGetAttribute(final Logger logger,
                                                   final Level logLevel,
                                                   final String attributeID,
                                                   final Exception e){
            logger.log(logLevel, String.format("Failed to get attribute %s. Context: %s",
                    attributeID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when getting attribute.
         * @param attributeID The attribute identifier.
         * @param e Internal connector error.
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        protected abstract void failedToGetAttribute(final String attributeID,
                                                     final Exception e);

        /**
         * Reads a set of managementAttributes.
         *
         * @param output      The dictionary with set of attribute keys to invoke and associated default values.
         * @param readTimeout The attribute value invoke operation timeout.
         * @return The set of managementAttributes ids really written to the dictionary.
         * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector exception.
         */
        @Override
        @ThreadSafe
        public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException, AttributeSupportException {
            final CountdownTimer timer = CountdownTimer.start(readTimeout);
            beginRead();
            String attributeID = null;
            try {
                //accumulator for really existed attribute IDs
                final Set<String> result = new HashSet<>(attributes.size());
                //whether the attribute value is unavailable
                timer.stop();
                for (final String id : output.keySet())
                    if (attributes.containsKey(attributeID = id)) {
                        timer.start();
                        final Object value = getAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime());
                        result.add(id);
                        output.put(id, value);

                    }
                return result;
            } catch (final TimeoutException e) {
                throw e;
            } catch (final Exception e) {
                failedToGetAttribute(attributeID, e);
                throw new AttributeSupportException(e);
            } finally {
                endRead();
            }
        }

        /**
         * Sends the attribute value to the remote agent.
         *
         * @param attribute    The metadata of the attribute to set.
         * @param writeTimeout The attribute value write operation timeout.
         * @param value        The value to write.
         * @throws java.util.concurrent.TimeoutException The attribute value cannot be written in the specified time constraint.
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract void setAttributeValue(final AttributeMetadata attribute, final TimeSpan writeTimeout, final Object value) throws Exception;

        /**
         * Writes the value of the specified attribute.
         *
         * @param id           An identifier of the attribute,
         * @param writeTimeout The attribute value write operation timeout.
         * @param value        The value to write.
         * @throws TimeoutException The attribute value cannot be written in the specified time constraint.
         * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The requested attribute doesn't exist.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        @Override
        @ThreadSafe
        public final void setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException, UnknownAttributeException, AttributeSupportException {
            final CountdownTimer timer = CountdownTimer.start(writeTimeout);
            beginWrite();
            try {
                if (attributes.containsKey(id))
                    setAttributeValue(attributes.get(id), timer.stopAndGetElapsedTime(), value);
                else throw new UnknownAttributeException(id);
            } catch (final UnknownAttributeException | TimeoutException e) {
                throw e;
            } catch (final Exception e) {
                failedToSetAttribute(id, value, e);
                throw new AttributeSupportException(e);
            } finally {
                endWrite();
            }
        }

        /**
         * Reports an error when updating attribute.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param attributeID The attribute identifier.
         * @param value The value of the attribute.
         * @param e Internal connector error.
         */
        protected static void failedToSetAttribute(final Logger logger,
                                                   final Level logLevel,
                                                   final String attributeID,
                                                   final Object value,
                                                   final Exception e){
            logger.log(logLevel, String.format("Failed to update attribute %s with %s value. Context: %s",
                    attributeID, value, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when updating attribute.
         * @param attributeID The attribute identifier.
         * @param value The value of the attribute.
         * @param e Internal connector error.
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
         */
        protected abstract void failedToSetAttribute(final String attributeID,
                                                     final Object value,
                                                     final Exception e);

        /**
         * Writes a set of managementAttributes inside of the transaction.
         *
         * @param values       The dictionary of managementAttributes keys and its values.
         * @param writeTimeout Batch write timeout.
         * @throws TimeoutException The attribute value cannot be written in the specified time constraint.
         * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
         */
        @Override
        @ThreadSafe
        public void setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException, AttributeSupportException {
            final CountdownTimer timer = CountdownTimer.start(writeTimeout);
            beginWrite();
            String attributeID = null;
            Object attributeValue = null;
            try {
                //whether the attribute value is unavailable
                timer.stop();
                for (final Map.Entry<String, Object> entry : values.entrySet()) {
                    timer.start();
                    setAttributeValue(attributes.get(attributeID = entry.getKey()), timer.stopAndGetElapsedTime(), attributeValue = entry.getValue());
                }
            } catch (final TimeoutException e) {
                throw e;
            } catch (final Exception e) {
                failedToSetAttribute(attributeID, attributeValue, e);
                throw new AttributeSupportException(e);
            } finally {
                endWrite();
            }
        }

        /**
         * Removes the attribute from the connector.
         *
         * @param id            The unique identifier of the attribute.
         * @param attributeInfo An attribute metadata.
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @SuppressWarnings("UnusedParameters")
        protected boolean disconnectAttribute(final String id, final GenericAttributeMetadata<?> attributeInfo) {
            return true;
        }

        /**
         * Removes the attribute from the connector.
         *
         * @param id The unique identifier of the attribute.
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @Override
        @ThreadSafe
        public final boolean disconnectAttribute(final String id) {
            beginWrite();
            try {
                if (attributes.containsKey(id) && disconnectAttribute(id, attributes.get(id))) {
                    attributes.remove(id);
                    return true;
                } else return false;
            } finally {
                endWrite();
            }
        }

        /**
         * Returns the information about the connected attribute.
         *
         * @param id An identifier of the attribute.
         * @return The attribute descriptor; or {@literal null} if attribute is not connected.
         */
        @Override
        @ThreadSafe
        public final AttributeMetadata getAttributeInfo(final String id) {
            beginRead();
            try {
                return attributes.get(id);
            } finally {
                endRead();
            }
        }

        /**
         * Returns a read-only collection of registered managementAttributes.
         *
         * @return A read-only collection of registered managementAttributes.
         */
        @Override
        @ThreadSafe
        public final Collection<String> getConnectedAttributes() {
            beginRead();
            try {
                return Collections.unmodifiableCollection(attributes.keySet());
            } finally {
                endRead();
            }
        }

        /**
         * Removes all attributes.
         */
        public void clear() {
            beginWrite();
            try {
                attributes.clear();
            } finally {
                endWrite();
            }
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
            public final boolean handle(final String listId, final Notification n) {
                return listener.handle(listId, n);
            }
        }

        private final String eventCategory;
        private final Map<String, SubscribedNotificationListener> listeners;
        private final ReadWriteLock coordinator;
        private String subscriptionList;
        private volatile ManagedEntityType attachmentType;

        /**
         * Initializes a new event metadata.
         * @param category The category of the event.
         */
        protected GenericNotificationMetadata(final String category){
            this.eventCategory = category;
            this.listeners = new HashMap<>(10);
            this.coordinator = new ReentrantReadWriteLock();
            this.subscriptionList = UNKNOWN_LIST_ID;
            attachmentType = null;
        }

        private void setSubscriptionList(final String value){
            this.subscriptionList = value != null && value.length() > 0 ? value : UNKNOWN_LIST_ID;
        }

        /**
         * Detects the attachment type.
         * <p>
         *     This method will be called automatically by SNAMP infrastructure
         *     and once for this instance of notification metadata.
         * @return The attachment type.
         */
        protected abstract ManagedEntityType detectAttachmentType();

        /**
         * Gets attachment descriptor that can be used to convert notification attachment
         * into well-known object.
         *
         * @return An attachment descriptor; or {@literal null} if attachments are not supported.
         * @see #detectAttachmentType()
         */
        @Override
        public final ManagedEntityType getAttachmentType() {
            ManagedEntityType result = attachmentType;
            if (result == null)
                synchronized (this) {
                    result = attachmentType;
                    if (result == null)
                        result = attachmentType = detectAttachmentType();
                }
            return result;
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
         * @see com.itworks.snamp.connectors.notifications.NotificationListenerInvokerFactory
         */
        protected final void fire(final Notification n, final NotificationListenerInvoker invoker){
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
    protected static abstract class AbstractNotificationSupport extends ThreadSafeObject implements NotificationSupport{
        private static enum ANSResource{
            DELAYED_NOTIFS,
            CONNECTED_NOTIFS
        }

        private final KeyedObjects<String, GenericNotificationMetadata> notifications;
        private final Map<String, NotificationListener> delayedNotifications;

        /**
         * Initializes a new notification manager.
         */
        protected AbstractNotificationSupport() {
            super(ANSResource.class);
            this.notifications = new AbstractKeyedObjects<String, GenericNotificationMetadata>(10) {
                @Override
                public final String getKey(final GenericNotificationMetadata item) {
                    return item.getSubscriptionList();
                }
            };
            this.delayedNotifications = new HashMap<>(3);
        }

        /**
         * Returns a read-only collection of enabled notifications (subscription list identifiers).
         *
         * @return A read-only collection of enabled notifications (subscription list identifiers).
         */
        @Override
        public final Collection<String> getEnabledNotifications() {
            beginRead(ANSResource.CONNECTED_NOTIFS);
            try {
                return Collections.unmodifiableSet(notifications.keySet());
            } finally {
                endRead(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Returns all notifications associated with the specified category.
         * @param category The category of the event.
         * @param metadataType The type of requested notification metadata.
         * @param <T> The type of requested notification metadata.
         * @return A map of registered notifications (values) and subscription lists (keys).
         */
        @ThreadSafe
        protected final <T extends GenericNotificationMetadata> Map<String, T> getEnabledNotifications(final String category, final Class<T> metadataType) {
            beginRead(ANSResource.CONNECTED_NOTIFS);
            try {
                final Map<String, T> result = Maps.newHashMapWithExpectedSize(notifications.size());
                for (final Map.Entry<String, GenericNotificationMetadata> metadata : notifications.entrySet())
                    if (Objects.equals(metadata.getValue().getCategory(), category) && metadataType.isInstance(metadata.getValue()))
                        result.put(metadata.getKey(), metadataType.cast(metadata.getValue()));
                return result;
            } finally {
                endRead(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Returns a collection of active categories.
         * @return A collection of active categories.
         */
        @ThreadSafe
        protected final Set<String> getCategories() {
            beginRead(ANSResource.CONNECTED_NOTIFS);
            try {
                final Set<String> categories = new HashSet<>(notifications.size());
                for (final GenericNotificationMetadata eventData : notifications.values())
                    categories.add(eventData.getCategory());
                return categories;
            } finally {
                endRead(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Enables event listening for the specified category of events.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param category The name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen;
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract GenericNotificationMetadata enableNotifications(final String category, final Map<String, String> options) throws Exception;

        /**
         * Enables event listening for the specified category of events.
         * @param listId An identifier of the subscription list.
         * @param category A name of the category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen.
         * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
         */
        @Override
        public final NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) throws NotificationSupportException {
            beginWrite(ANSResource.CONNECTED_NOTIFS);
            try {
                if (notifications.containsKey(category)) return notifications.get(category);
                final GenericNotificationMetadata metadata = enableNotifications(category, options);
                if (metadata != null) {
                    metadata.setSubscriptionList(listId);
                    notifications.put(metadata);
                    //add delayed listeners
                    beginWrite(ANSResource.DELAYED_NOTIFS);
                    try {
                        for (final String listenerId : delayedNotifications.keySet())
                            subscribe(listenerId, delayedNotifications.get(listenerId), false);
                    } finally {
                        endWrite(ANSResource.DELAYED_NOTIFS);
                    }
                }
                return metadata;
            } catch (final Exception e) {
                failedToEnableNotifications(listId, category, e);
                throw new NotificationSupportException(e);
            } finally {
                endWrite(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Reports an error when enabling notifications.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param listID Subscription list identifier.
         * @param category An event category.
         * @param e Internal connector error.
         */
        protected static void failedToEnableNotifications(final Logger logger,
                                                          final Level logLevel,
                                                          final String listID,
                                                          final String category,
                                                          final Exception e){
            logger.log(logLevel, String.format("Failed to enable notifications %s for %s subscription list. Context: %s",
                    category, listID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when enabling notifications.
         * @param listID Subscription list identifier.
         * @param category An event category.
         * @param e Internal connector error.
         * @see #failedToEnableNotifications(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        protected abstract void failedToEnableNotifications(final String listID,
                                                            final String category,
                                                            final Exception e);

        /**
         * Disable all notifications associated with the specified event.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param notificationType The event descriptor.
         * @throws java.lang.Exception Internal connector error.
         */
        protected void disableNotifications(final GenericNotificationMetadata notificationType) throws Exception{
            notificationType.removeListeners();
        }

        /**
         * Disables event listening for the specified category of events.
         *
         * @param listId An identifier of the subscription list.
         * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
         * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
         */
        @Override
        public final boolean disableNotifications(final String listId) throws NotificationSupportException{
            beginWrite(ANSResource.CONNECTED_NOTIFS);
            try {
                if (notifications.containsKey(listId)) {
                    disableNotifications(notifications.remove(listId));
                    return true;
                } else return false;
            }
            catch (final Exception e){
                failedToDisableNotifications(listId, e);
                throw new NotificationSupportException(e);
            }
            finally {
                endWrite(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Reports an error when disabling notifications.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param listID Subscription list identifier.
         * @param e Internal connector error.
         */
        protected static void failedToDisableNotifications(final Logger logger,
                                                           final Level logLevel,
                                                           final String listID,
                                                           final Exception e){
            logger.log(logLevel, String.format("Failed to disable notifications at %s topic. Context: %s",
                    listID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when disabling notifications.
         * @param listID Subscription list identifier.
         * @param e Internal connector error.
         * @see #failedToDisableNotifications(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        protected abstract void failedToDisableNotifications(final String listID, final Exception e);

        /**
         * Gets the notification metadata by its category.
         *
         * @param listId An identifier of the subscription list.
         * @return The metadata of the specified notification category; or {@literal null}, if notifications
         *         for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
         */
        @Override
        public final NotificationMetadata getNotificationInfo(final String listId) {
            beginRead(ANSResource.CONNECTED_NOTIFS);
            try {
                return notifications.get(listId);
            } finally {
                endRead(ANSResource.CONNECTED_NOTIFS);
            }
        }

        /**
         * Adds a new listener for the specified notification.
         * @param listener The event listener.
         * @return Any custom data associated with the subscription.
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract Object subscribe(final NotificationListener listener) throws Exception;

        /**
         * Attaches the notification listener.
         *
         * @param listenerId An identifier of the notification listener.
         * @param listener The notification listener.
         * @param delayed {@literal true} to add notification listener event if this
         *                               object has no enabled notifications.
         * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
         */

        @Override
        public final void subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) throws NotificationSupportException{
            if (listenerId == null || listenerId.isEmpty())
                throw new IllegalArgumentException("Invalid subscription list identifier");
            else if(listener == null)
                throw new IllegalArgumentException("listener is null.");
            else if (delayed) {
                beginWrite(ANSResource.DELAYED_NOTIFS);
                try {
                    delayedNotifications.put(listenerId, listener);
                } finally {
                    endWrite(ANSResource.DELAYED_NOTIFS);
                }
            } else {
                beginRead(ANSResource.CONNECTED_NOTIFS);
                try {
                    final Object userData = subscribe(listener);
                    for (final GenericNotificationMetadata metadata : notifications.values())
                        metadata.addListener(listenerId, listener, userData);
                }
                catch (final Exception e){
                    failedToSubscribe(listenerId, e);
                    throw new NotificationSupportException(e);
                }
                finally {
                    endRead(ANSResource.CONNECTED_NOTIFS);
                }
            }
        }


        /**
         * Reports an error when subscribing the listener.
         * @param logger The logger instance. Cannot be {@literal null}.
         * @param logLevel Logging level.
         * @param listenerID Subscription list identifier.
         * @param e Internal connector error.
         */
        protected static void failedToSubscribe(final Logger logger,
                                                final Level logLevel,
                                                final String listenerID,
                                                final Exception e){
            logger.log(logLevel, String.format("Failed to subscribe on %s topic. Context: %s",
                    listenerID, LogicalOperation.current()), e);
        }

        /**
         * Reports an error when subscribing the listener.
         * @param listenerID Subscription list identifier.
         * @param e Internal connector error.
         * @see #failedToSubscribe(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        protected abstract void failedToSubscribe(final String listenerID,
                                                  final Exception e);

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
         *                   to {@link #subscribe(String, com.itworks.snamp.connectors.notifications.NotificationListener, boolean)} method.
         * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
         */
        public final boolean unsubscribe(final String listenerId) {
            beginRead(ANSResource.CONNECTED_NOTIFS);
            try {
                for (final GenericNotificationMetadata metadata : notifications.values()) {
                    if (metadata.hasListener(listenerId)) {
                        final GenericNotificationMetadata.SubscribedNotificationListener pair = metadata.getListener(listenerId);
                        if (pair == null) continue;
                        unsubscribe(pair.listener, pair.userData);
                        if (metadata.removeListener(listenerId)) return true;
                    }
                }
            } finally {
                endRead(ANSResource.CONNECTED_NOTIFS);
            }
            beginWrite(ANSResource.DELAYED_NOTIFS);
            try {
                return delayedNotifications.remove(listenerId) != null;
            } finally {
                endWrite(ANSResource.DELAYED_NOTIFS);
            }
        }

        /**
         * Removes all listeners from this notification manager.
         * <p>
         *     It is recommended to call this method in the implementation of {@link AutoCloseable#close()}
         *     method in your management connector.
         * </p>
         */
        public final void clear() {
            beginWrite(ANSResource.CONNECTED_NOTIFS);
            beginWrite(ANSResource.DELAYED_NOTIFS);
            try {
                notifications.clear();
                delayedNotifications.clear();
            } finally {
                endWrite(ANSResource.DELAYED_NOTIFS);
                endWrite(ANSResource.CONNECTED_NOTIFS);
            }
        }
    }

    private final TConnectionOptions connectionOptions;
    private final IllegalStateFlag closed = new IllegalStateFlag() {
        @Override
        public final IllegalStateException create() {
            return new IllegalStateException("Management connector is closed.");
        }
    };

    /**
     * Initializes a new management connector.
     * @param connectionOptions Management connector initialization options.
     */
    protected AbstractManagedResourceConnector(final TConnectionOptions connectionOptions){
        if(connectionOptions == null) throw new IllegalArgumentException("connectionOptions is null.");
        else this.connectionOptions = connectionOptions;
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
     * Releases all resources associated with this connector.
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    @ThreadSafe(false)
    public void close() throws Exception {
        //change state of the connector
        closed.set();
    }

    /**
     * Returns logger name based on the management connector name.
     * @param connectorName The name of the connector.
     * @return The logger name.
     */
    public static String getLoggerName(final String connectorName){
        return String.format("com.itworks.snamp.connectors.%s", connectorName);
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
