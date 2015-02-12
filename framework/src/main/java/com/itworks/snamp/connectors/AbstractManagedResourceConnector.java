package com.itworks.snamp.connectors;

import com.google.common.collect.Lists;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.Descriptive;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.AbstractFrameworkService;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.IllegalStateFlag;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
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
public abstract class AbstractManagedResourceConnector<TConnectionOptions> extends AbstractFrameworkService implements ManagedResourceConnector<TConnectionOptions>, Descriptive {

    /**
     * Provides a base support of management attributes.
     * @param <M> Type of the attribute metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractAttributeSupport<M extends MBeanAttributeInfo> extends ThreadSafeObject implements AttributeSupport {
        private final KeyedObjects<String, M> attributes;
        private final Class<M> metadataType;

        /**
         * Initializes a new support of management attributes.
         * @param attributeMetadataType The type of the attribute metadata.
         */
        protected AbstractAttributeSupport(final Class<M> attributeMetadataType) {
            attributes = createAttributes();
            metadataType = Objects.requireNonNull(attributeMetadataType);
        }

        private static <M extends MBeanAttributeInfo> AbstractKeyedObjects<String, M> createAttributes(){
            return new AbstractKeyedObjects<String, M>(10) {
                @Override
                public String getKey(final MBeanAttributeInfo metadata) {
                    return metadata.getName();
                }
            };
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
         * Gets an array of connected attributes.
         *
         * @return An array of connected attributes.
         */
        @Override
        public final M[] getAttributeInfo() {
            beginRead();
            try {
                return ArrayUtils.toArray(attributes.values(), metadataType);
            } finally {
                endRead();
            }
        }

        /**
         * Gets a set of attributes in sequential manner.
         * @param attributes A set of attributes to read. Cannot be {@literal null}.
         * @return output A list of obtained attributes.
         */
        protected final AttributeList getAttributesSequential(final String[] attributes) {
            final List<Attribute> result = Lists.newArrayListWithExpectedSize(attributes.length);
            for(final String attributeID: attributes)
                try {
                    result.add(new Attribute(attributeID, getAttribute(attributeID)));
                } catch (final JMException e) {
                    failedToGetAttribute(attributeID, e);
                }
            return new AttributeList(result);
        }

        /**
         * Gets a set of attributes in parallel manner.
         * @param executor The executor used to schedule attribute reader. Cannot be {@literal null}.
         * @param attributes A set of attributes to read. Cannot be {@literal null}.
         * @param timeout Synchronization timeout. May be {@link com.itworks.snamp.TimeSpan#INFINITE}.
         * @return  A list of obtained attributes.
         * @throws InterruptedException Operation is interrupted.
         * @throws TimeoutException Unable to read attributes in the specified time duration.
         */
        protected final AttributeList getAttributesParallel(final ExecutorService executor,
                                                            final String[] attributes,
                                                            final TimeSpan timeout) throws InterruptedException, TimeoutException {
            final List<Attribute> result = Collections.
                    synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.length));
            final CountDownLatch synchronizer = new CountDownLatch(attributes.length);
            for (final String attributeID : attributes)
                executor.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws JMException {
                        try {
                            return result.add(new Attribute(attributeID, getAttribute(attributeID)));
                        }
                        catch (final JMException e){
                            failedToGetAttribute(attributeID, e);
                            return null;
                        }
                        finally {
                            synchronizer.countDown();
                        }
                    }
                });
            if (timeout == null)
                synchronizer.await();
            else if (!synchronizer.await(timeout.duration, timeout.unit))
                throw new TimeoutException();
            return new AttributeList(result);
        }

        /**
         * Get the values of several attributes of the managed resource.
         *
         * @param attributes A list of the attributes to be retrieved.
         * @return The list of attributes retrieved.
         * @see #getAttributesSequential(String[])
         * @see #getAttributesParallel(java.util.concurrent.ExecutorService, String[], com.itworks.snamp.TimeSpan)
         */
        @Override
        public AttributeList getAttributes(final String[] attributes) {
            return getAttributesSequential(attributes);
        }

        /**
         * Sets the values of several attributes of the managed resource in sequential manner.
         *
         * @param attributes A list of attributes: The identification of the
         *                   attributes to be set and  the values they are to be set to.
         * @return The list of attributes that were set, with their new values.
         */
        protected final AttributeList setAttributesSequential(final AttributeList attributes) {
            final List<Attribute> result = Lists.newArrayListWithExpectedSize(attributes.size());
            for(final Attribute attr: attributes.asList()) {
                try {
                    setAttribute(attr);
                    result.add(attr);
                }
                catch (final JMException e){
                    failedToSetAttribute(attr.getName(), attr.getValue(), e);
                }
            }
            return new AttributeList(result);
        }

        /**
         * Sets the values of several attributes of the managed resource in sequential manner.
         *
         * @param executor The executor used to schedule attribute writer. Cannot be {@literal null}.
         * @param attributes A list of attributes: The identification of the
         *                   attributes to be set and  the values they are to be set to.
         * @param timeout Synchronization timeout. May be {@link com.itworks.snamp.TimeSpan#INFINITE}.
         * @return The list of attributes that were set, with their new values.
         * @throws java.lang.InterruptedException Operation is interrupted.
         * @throws java.util.concurrent.TimeoutException Unable to set attributes in the specified time duration.
         */
        protected final AttributeList setAttributesParallel(final ExecutorService executor,
                                                            final AttributeList attributes,
                                                            final TimeSpan timeout) throws TimeoutException, InterruptedException {
            if(attributes.isEmpty()) return attributes;
            final List<Attribute> result =
                    Collections.synchronizedList(Lists.<Attribute>newArrayListWithExpectedSize(attributes.size()));
            final CountDownLatch synchronizer = new CountDownLatch(attributes.size());
            for (final Attribute attr : attributes.asList())
                executor.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            setAttribute(attr);
                            return result.add(new Attribute(attr.getName(), attr.getValue()));
                        }
                        catch (final JMException e){
                            failedToSetAttribute(attr.getName(), attr.getValue(), e);
                            return null;
                        }
                        finally {
                            synchronizer.countDown();
                        }
                    }
                });
            if(timeout == null)
                synchronizer.await();
            else if(!synchronizer.await(timeout.duration, timeout.unit))
                throw new TimeoutException();
            return new AttributeList(result);
        }

        /**
         * Sets the values of several attributes of the managed resource.
         *
         * @param attributes A list of attributes: The identification of the
         *                   attributes to be set and  the values they are to be set to.
         * @return The list of attributes that were set, with their new values.
         * @see #setAttributesSequential(javax.management.AttributeList)
         * @see #setAttributesParallel(java.util.concurrent.ExecutorService, javax.management.AttributeList, com.itworks.snamp.TimeSpan)
         */
        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            return setAttributesSequential(attributes);
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeID The id of the attribute.
         * @param descriptor Attribute descriptor.
         * @return The description of the attribute.
         * @throws java.lang.Exception Internal connector error.
         */
        protected abstract M connectAttribute(final String attributeID,
                                                               final AttributeDescriptor descriptor) throws Exception;

        /**
         * Connects to the specified attribute.
         *
         * @param id               A key string that is used to invoke attribute from this connector.
         * @param attributeName    The name of the attribute.
         * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
         * @param options          The attribute discovery options.
         * @return The description of the attribute.
         * @throws javax.management.AttributeNotFoundException The managed resource doesn't provide the attribute with the specified name.
         * @throws javax.management.JMException                Internal connector error.
         */
        @Override
        public final M connectAttribute(final String id,
                                                         final String attributeName,
                                                         final TimeSpan readWriteTimeout,
                                                         final CompositeData options) throws JMException {
            beginWrite();
            try {
                //return existed attribute without exception to increase flexibility of the API
                if (attributes.containsKey(id)) return attributes.get(id);
                final M attr;
                if ((attr = connectAttribute(id, new AttributeDescriptor(attributeName, readWriteTimeout, options))) != null)
                    attributes.put(attr);
                return attr;
            }
            catch (final Exception e){
                failedToConnectAttribute(id, attributeName, e);
                throw new MBeanException(e);
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
         * Obtains the value of a specific attribute of the managed resource.
         * @param metadata The metadata of the attribute.
         * @return The value of the attribute retrieved.
         * @throws Exception Internal connector error.
         */
        protected abstract Object getAttribute(final M metadata) throws Exception;

        /**
         * Obtains the value of a specific attribute of the managed resource.
         *
         * @param attributeID The name of the attribute to be retrieved
         * @return The value of the attribute retrieved.
         * @throws javax.management.AttributeNotFoundException
         * @throws javax.management.MBeanException             Wraps a {@link java.lang.Exception} thrown by the MBean's getter.
         * @throws javax.management.ReflectionException Wraps any exception associated with Java Reflection.
         * @see #setAttribute
         */
        @Override
        public final Object getAttribute(final String attributeID) throws AttributeNotFoundException, MBeanException, ReflectionException {
            beginRead();
            try{
                if(attributes.containsKey(attributeID))
                    return getAttribute(attributes.get(attributeID));
                else throw JMExceptionUtils.attributeNotFound(attributeID);
            }
            catch (final AttributeNotFoundException e){
                throw e;
            }
            catch (final MBeanException | ReflectionException e){
                failedToGetAttribute(attributeID, e);
                throw e;
            }
            catch (final Exception e){
                failedToGetAttribute(attributeID, e);
                throw new MBeanException(e);
            }
            finally {
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
         * Set the value of a specific attribute of the managed resource.
         * @param attribute The attribute of to set.
         * @param value The value of the attribute.
         * @throws Exception Internal connector error.
         * @throws javax.management.InvalidAttributeValueException Incompatible attribute type.
         */
        protected abstract void setAttribute(final M attribute,
                                             final Object value) throws Exception;

        /**
         * Set the value of a specific attribute of the managed resource.
         *
         * @param attribute The identification of the attribute to
         *                  be set and  the value it is to be set to.
         * @throws javax.management.AttributeNotFoundException
         * @throws javax.management.InvalidAttributeValueException
         * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
         * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
         * @see #getAttribute
         */
        @Override
        public final void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            beginRead();
            try{
                if(attributes.containsKey(attribute.getName()))
                    setAttribute(attributes.get(attribute.getName()), attribute.getValue());
                else throw JMExceptionUtils.attributeNotFound(attribute.getName());
            }
            catch (final AttributeNotFoundException e){
                throw e;
            }
            catch (final InvalidAttributeValueException | MBeanException | ReflectionException e){
                failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
                throw e;
            }
            catch (final Exception e){
                failedToSetAttribute(attribute.getName(), attribute.getValue(), e);
                throw new MBeanException(e);
            }
            finally {
                beginRead();
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
         * Removes the attribute from the connector.
         *
         * @param id            The unique identifier of the attribute.
         * @param attributeInfo An attribute metadata.
         * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
         */
        @SuppressWarnings("UnusedParameters")
        protected boolean disconnectAttribute(final String id, final M attributeInfo) {
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
                return attributes.containsKey(id) && disconnectAttribute(id, attributes.remove(id));
            } finally {
                endWrite();
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

    private static final class NotificationListenerHolder implements NotificationListener {
        private final NotificationListener listener;
        private final Object handback;
        private final NotificationFilter filter;

        private NotificationListenerHolder(final NotificationListener listener,
                                           final NotificationFilter filter,
                                           final Object handback) {
            this.listener = Objects.requireNonNull(listener);
            this.handback = handback;
            this.filter = filter;
        }

        private boolean isWrapped(final NotificationListener listener){
            return Objects.equals(listener, this.listener);
        }

        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            if (filter == null || filter.isNotificationEnabled(notification))
                listener.handleNotification(notification, handback != null ? handback : this.handback);
        }
    }

    /**
     * Represents a base class that allows to enable notification support for the management connector.
     * @param <M> Notification metadata.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class AbstractNotificationSupport<M extends MBeanNotificationInfo> extends ThreadSafeObject implements NotificationSupport{
        private static enum ANSResource{
            NOTIFICATIONS,
            LISTENERS
        }

        private final KeyedObjects<String, M> notifications;
        private final List<NotificationListenerHolder> listeners;
        private final AtomicLong sequenceCounter;
        private final Class<M> metadataType;

        /**
         * Initializes a new notification manager.
         * @param notifMetadataType Type of the notification metadata;
         */
        protected AbstractNotificationSupport(final Class<M> notifMetadataType) {
            super(ANSResource.class);
            notifications = createNotifications();
            listeners = new ArrayList<>(10);
            sequenceCounter = new AtomicLong(0L);
            metadataType = Objects.requireNonNull(notifMetadataType);
        }

        private static <M extends MBeanNotificationInfo> AbstractKeyedObjects<String, M> createNotifications(){
            return new AbstractKeyedObjects<String, M>(10) {
                @Override
                public String getKey(final MBeanNotificationInfo item) {
                    return item.getNotifTypes()[0];
                }
            };
        }

        /**
         * Gets subscription model.
         * @return The subscription model.
         */
        public final NotificationSubscriptionModel getSubscriptionModel(){
            final NotificationListenerInvoker invoker = getListenerInvoker();
            if(invoker instanceof NotificationListenerSequentialInvoker)
                return NotificationSubscriptionModel.MULTICAST_SEQUENTIAL;
            else if(invoker instanceof NotificationListenerParallelInvoker)
                return NotificationSubscriptionModel.MULTICAST_PARALLEL;
            else return NotificationSubscriptionModel.MULTICAST;
        }

        /**
         * Gets the invoker used to executed notification listeners.
         * @return The notification listener invoker.
         */
        protected abstract NotificationListenerInvoker getListenerInvoker();

        /**
         * Invokes all listeners associated with the specified notification category.
         * @param category An event category.
         * @param message The human-readable message associated with the notification.
         * @param userData Advanced object associated with the notification.
         */
        protected final void fire(final String category,
                                  final String message,
                                  final Object userData){
            final Collection<Notification> notifs;
            beginRead(ANSResource.NOTIFICATIONS);
            try{
                notifs = Lists.newArrayListWithExpectedSize(notifications.size());
                for(final M metadata: notifications.values())
                    if(Objects.equals(NotificationDescriptor.getNotificationCategory(metadata), category))
                        for(final String listId: metadata.getNotifTypes()){
                            final Notification n = new Notification(listId,
                                    this,
                                    sequenceCounter.getAndIncrement(),
                                    message);
                            n.setTimeStamp(System.currentTimeMillis());
                            n.setUserData(userData);
                            notifs.add(n);
                        }
            }
            finally {
                beginRead(ANSResource.NOTIFICATIONS);
            }
            //fire listeners
            beginRead(ANSResource.LISTENERS);
            try{
                for(final Notification n: notifs)
                    getListenerInvoker().invoke(n, null, listeners);
            }
            finally {
                endRead(ANSResource.LISTENERS);
            }
        }

        protected abstract M enableNotifications(final String notifType,
                                                final NotificationDescriptor metadata) throws Exception;

        /**
         * Enables event listening for the specified category of events.
         * <p/>
         * category can be used for enabling notifications for the same category
         * but with different options.
         * <p/>
         * listId parameter
         * is used as a value of {@link javax.management.Notification#getType()}.
         *
         * @param listId   An identifier of the subscription list.
         * @param category The name of the event category to listen.
         * @param options  Event discovery options.
         * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
         * @throws javax.management.JMException Internal connector error.
         */
        @Override
        public final M enableNotifications(final String listId, final String category, final CompositeData options) throws JMException {
            beginWrite(ANSResource.NOTIFICATIONS);
            try{
                if(notifications.containsKey(listId))
                    return notifications.get(listId);
                final M result;
                notifications.put(result = enableNotifications(listId,
                        new NotificationDescriptor(category, getSubscriptionModel(), options)));
                return result;
            }
            catch (final Exception e){
                failedToEnableNotifications(listId, category, e);
                throw new MBeanException(e);
            }
            finally {
                endWrite(ANSResource.NOTIFICATIONS);
            }
        }

        protected abstract boolean disableNotifications(final M metadata);

        /**
         * Disables event listening for the specified category of events.
         * <p>
         * This method removes all listeners associated with the specified subscription list.
         * </p>
         *
         * @param listId The identifier of the subscription list.
         * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
         */
        @Override
        public final boolean disableNotifications(final String listId) {
            beginWrite(ANSResource.NOTIFICATIONS);
            try{
                return notifications.containsKey(listId) &&
                        disableNotifications(notifications.get(listId));
            }
            finally {
                endWrite(ANSResource.NOTIFICATIONS);
            }
        }

        /**
         * Adds a listener to this MBean.
         *
         * @param listener The listener object which will handle the
         *                 notifications emitted by the broadcaster.
         * @param filter   The filter object. If filter is null, no
         *                 filtering will be performed before handling notifications.
         * @param handback An opaque object to be sent back to the
         *                 listener when a notification is emitted. This object cannot be
         *                 used by the Notification broadcaster object. It should be
         *                 resent unchanged with the notification to the listener.
         * @throws IllegalArgumentException Listener parameter is null.
         * @see #removeNotificationListener
         */
        @Override
        public final void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) throws IllegalArgumentException {
            beginWrite(ANSResource.LISTENERS);
            try{
                listeners.add(new NotificationListenerHolder(listener, filter, handback));
            }
            finally {
                endWrite(ANSResource.LISTENERS);
            }
        }

        /**
         * Removes a listener from this MBean.  If the listener
         * has been registered with different handback objects or
         * notification filters, all entries corresponding to the listener
         * will be removed.
         *
         * @param listener A listener that was previously added to this
         *                 MBean.
         * @throws javax.management.ListenerNotFoundException The listener is not
         *                                                    registered with the MBean.
         * @see #addNotificationListener
         * @see javax.management.NotificationEmitter#removeNotificationListener
         */
        @Override
        public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
            beginWrite(ANSResource.LISTENERS);
            try {
                final Iterator<NotificationListenerHolder> iter = listeners.iterator();
                boolean removed = false;
                while (iter.hasNext()){
                    final NotificationListenerHolder holder = iter.next();
                    if(holder.isWrapped(listener)) {
                        iter.remove();
                        removed = true;
                    }
                }
                if(!removed)
                    throw JMExceptionUtils.listenerNotFound(listener);
            }
            finally {
                endWrite(ANSResource.LISTENERS);
            }
        }

        /**
         * <p>Returns an array indicating, for each notification this
         * MBean may send, the name of the Java class of the notification
         * and the notification type.</p>
         * <p/>
         * <p>It is not illegal for the MBean to send notifications not
         * described in this array.  However, some clients of the MBean
         * server may depend on the array being complete for their correct
         * functioning.</p>
         *
         * @return the array of possible notifications.
         */
        @Override
        public final M[] getNotificationInfo() {
            beginRead(ANSResource.NOTIFICATIONS);
            try{
                return ArrayUtils.toArray(notifications.values(), metadataType);
            }
            finally {
                endRead(ANSResource.NOTIFICATIONS);
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
         * Removes all listeners from this notification manager.
         * <p>
         *     It is recommended to call this method in the implementation of {@link AutoCloseable#close()}
         *     method in your management connector.
         * </p>
         */
        public final void clear() {
            beginWrite(ANSResource.NOTIFICATIONS);
            beginWrite(ANSResource.LISTENERS);
            try {
                notifications.clear();
                listeners.clear();
            } finally {
                endWrite(ANSResource.LISTENERS);
                endWrite(ANSResource.NOTIFICATIONS);
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
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attribute The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        throw JMExceptionUtils.attributeNotFound(attribute);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw JMExceptionUtils.attributeNotFound(attribute.getName());
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        return new AttributeList();
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return new AttributeList();
    }

    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params     An array containing the parameters to be set when the action is
     *                   invoked.
     * @param signature  An array containing the signature of the action. The class objects will
     *                   be loaded through the same class loader as the one used for loading the
     *                   MBean on which the action is invoked.
     * @return The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     * @throws javax.management.MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @throws javax.management.ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        throw new MBeanException(new UnsupportedOperationException("Operation invocation is not supported."));
    }

    private String getClassName(){
        return getClass().getName();
    }

    /**
     * Returns the localized description of this connector.
     *
     * @param locale The locale of the description. If it is {@literal null} then returns description
     *               in the default locale.
     * @return The localized description of this connector.
     */
    @Override
    public String getDescription(final Locale locale) {
        return getClassName();
    }

    private MBeanAttributeInfo[] getAttributes(){
        final AttributeSupport attributes = queryObject(AttributeSupport.class);
        return attributes != null ? attributes.getAttributeInfo() : new MBeanAttributeInfo[0];
    }

    private MBeanNotificationInfo[] getNotifications(){
        final NotificationSupport notifs = queryObject(NotificationSupport.class);
        return notifs != null ? notifs.getNotificationInfo() : new MBeanNotificationInfo[0];
    }

    private MBeanOperationInfo[] getOperations(){
        return new MBeanOperationInfo[0];
    }

    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions
     * exposed by this Dynamic MBean to be retrieved.
     */
    @Override
    public final MBeanInfo getMBeanInfo() {
        return new MBeanInfo(getClassName(),
                getDescription(Locale.getDefault()),
                getAttributes(),
                new MBeanConstructorInfo[0],
                getOperations(),
                getNotifications());
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
