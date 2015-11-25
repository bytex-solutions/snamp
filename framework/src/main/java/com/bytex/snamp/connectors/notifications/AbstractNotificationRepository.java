package com.bytex.snamp.connectors.notifications;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connectors.AbstractFeatureRepository;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a base class that allows to enable notification support for the management connector.
 * @param <M> Notification metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractNotificationRepository<M extends MBeanNotificationInfo> extends AbstractFeatureRepository<M> implements NotificationSupport, SafeCloseable {
    private enum ANSResource{
        NOTIFICATIONS,
        RESOURCE_EVENT_LISTENERS
    }

    private static final class NotificationHolder<M extends MBeanNotificationInfo> extends FeatureHolder<M>{
        private NotificationHolder(final M metadata,
                                   final String category,
                                   final CompositeData options) {
            super(metadata, computeIdentity(category, options));
        }

        private String getNotifType(){
            return ArrayUtils.getFirst(getMetadata().getNotifTypes());
        }

        private boolean equals(final String category, final CompositeData options){
            return super.identity.equals(computeIdentity(category, options));
        }

        private static BigInteger computeIdentity(final String category,
                                                  final CompositeData options) {
            BigInteger result = toBigInteger(category);
            for (final String propertyName : options.getCompositeType().keySet())
                result = result
                        .xor(toBigInteger(propertyName))
                        .xor(BigInteger.valueOf(options.get(propertyName).hashCode()));
            return result;
        }
    }

    /**
     * Represents batch notification sender.
     */
    protected abstract class NotificationCollector extends LinkedList<Notification>{
        private static final long serialVersionUID = -7783660559033012643L;

        /**
         * Processes enabled notification.
         * @param metadata Notification metadata.
         * @see #enqueue(MBeanNotificationInfo, String, Object)
         */
        protected abstract void process(final M metadata);

        /**
         * Enqueues a notification to sent in future.
         * @param metadata Notification metadata.
         * @param message Notification message.
         * @param userData Advanced data to be associated with the notification.
         */
        protected final void enqueue(final MBeanNotificationInfo metadata,
                                     final String message,
                                     final Object userData){
            enqueue(metadata,
                    message,
                    generateSequenceNumber(),
                    System.currentTimeMillis(),
                    userData);
        }

        /**
         * Enqueues a notification to sent in future.
         * @param metadata Notification metadata.
         * @param message Notification message.
         * @param sequenceNumber Unique ID of notification.
         * @param timeStamp Time stamp of notification.
         * @param userData Advanced data to be associated with the notification.
         */
        protected final void enqueue(final MBeanNotificationInfo metadata,
                                     final String message,
                                     final long sequenceNumber,
                                     final long timeStamp,
                                     final Object userData){
            for(final String category: metadata.getNotifTypes())
                add(new NotificationBuilder()
                        .setTimeStamp(timeStamp)
                        .setSequenceNumber(sequenceNumber)
                        .setType(category)
                        .setSource(AbstractNotificationRepository.this)
                        .setMessage(message)
                        .setUserData(userData)
                        .get());
        }
    }

    private final KeyedObjects<String, NotificationHolder<M>> notifications;
    private final NotificationListenerList listeners;
    private final LongCounter sequenceNumberGenerator;
    private volatile boolean suspended = false;

    /**
     * Initializes a new notification manager.
     * @param resourceName The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata;
     */
    protected AbstractNotificationRepository(final String resourceName,
                                             final Class<M> notifMetadataType) {
        this(resourceName, notifMetadataType, DistributedServices.getProcessLocalCounterGenerator("notifications-".concat(resourceName)));
    }

    /**
     * Initializes a new notification manager.
     * @param resourceName The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata.
     * @param sequenceNumberGenerator Generator for sequence numbers. Cannot be {@literal null}.
     */
    protected AbstractNotificationRepository(final String resourceName,
                                             final Class<M> notifMetadataType,
                                             final LongCounter sequenceNumberGenerator) {
        super(resourceName,
                notifMetadataType,
                ANSResource.class,
                ANSResource.RESOURCE_EVENT_LISTENERS);
        notifications = createNotifications();
        listeners = new NotificationListenerList();
        this.sequenceNumberGenerator = Objects.requireNonNull(sequenceNumberGenerator);
    }

    /**
     * Generates a new unique sequence number for the notification.
     * @return A new unique sequence number.
     */
    protected final long generateSequenceNumber(){
        return sequenceNumberGenerator.increment();
    }

    private static <M extends MBeanNotificationInfo> AbstractKeyedObjects<String, NotificationHolder<M>> createNotifications(){
        return new AbstractKeyedObjects<String, NotificationHolder<M>>(10) {
            private static final long serialVersionUID = 6753355822109787406L;

            @Override
            public String getKey(final NotificationHolder<M> holder) {
                return holder.getNotifType();
            }
        };
    }

    /**
     * Gets the invoker used to executed notification listeners.
     * @return The notification listener invoker.
     */
    protected abstract NotificationListenerInvoker getListenerInvoker();

    /**
     * Collects notifications in batch manner.
     * @param sender An object used to collect notifications
     */
    protected final void fire(final NotificationCollector sender){
        //collect notifications
        try(final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)){
            for(final NotificationHolder<M> holder: notifications.values())
                sender.process(holder.getMetadata());
        }
        //send notifications
        fireListeners(sender);
    }

    /**
     * Invokes all listeners associated with the specified notification category.
     * @param category An event category.
     * @param message The human-readable message associated with the notification.
     * @param userData Advanced object associated with the notification.
     */
    protected final void fire(final String category,
                              final String message,
                              final Object userData) {
        fire(category, message, generateSequenceNumber(), System.currentTimeMillis(), userData);
    }

    protected final void fire(final String category,
                              final String message,
                              final long sequenceNumber,
                              final long timeStamp,
                              final Object userData) {
        if (isSuspended()) return; //check if events are suspended

        final Collection<Notification> notifs;
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            notifs = Lists.newArrayListWithExpectedSize(notifications.size());
            for (final NotificationHolder<M> holder : notifications.values())
                if (Objects.equals(NotificationDescriptor.getName(holder.getMetadata()), category)) {
                    final Notification n = new NotificationBuilder()
                            .setTimeStamp(timeStamp)
                            .setSequenceNumber(sequenceNumber)
                            .setType(holder.getNotifType())
                            .setSource(this)
                            .setMessage(message)
                            .setUserData(userData)
                            .get();
                    notifs.add(n);
                }
        }
        //fire listeners
        fireListeners(notifs);
        afterFire();
    }

    /**
     * Aspect called after invocation of {@link #fire(NotificationCollector)},
     * {@link #fire(String, String, long, long, Object)} or
     * {@link #fire(String, String, Object)}.
     */
    @MethodStub
    protected void afterFire(){

    }

    private void fireListeners(final Iterable<? extends Notification> notifications){
        for (final Notification n : notifications)
            listeners.handleNotification(getListenerInvoker(), n, null);
    }

    private void notificationAdded(final M metadata){
        fireResourceEvent(new NotificationAddedEvent(this, getResourceName(), metadata));
    }

    private void notificationRemoved(final M metadata){
        fireResourceEvent(new NotificationRemovingEvent(this, getResourceName(), metadata));
    }

    protected abstract M enableNotifications(final String notifType,
                                            final NotificationDescriptor metadata) throws Exception;

    /**
     * Enables event listening for the specified category of events.
     *
     * @param category The name of the event category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    public final M enableNotifications(final String category, final CompositeData options) {
        NotificationHolder<M> holder;
        try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
            holder = notifications.get(category);
            if(holder != null) {
                if (holder.equals(category, options))
                    return holder.getMetadata();
                else {
                    //remove notification
                    notificationRemoved(holder.getMetadata());
                    holder = notifications.remove(category);
                    //and register again
                    disableNotifications(holder.getMetadata());
                    final M metadata = enableNotifications(category, new NotificationDescriptor(options));
                    if (metadata != null) {
                        notifications.put(holder = new NotificationHolder<>(metadata, category, options));
                        notificationAdded(holder.getMetadata());
                    }
                }
            }
            else {
                final M metadata = enableNotifications(category, new NotificationDescriptor(options));
                if(metadata != null) {
                    notifications.put(holder = new NotificationHolder<>(metadata, category, options));
                    notificationAdded(holder.getMetadata());
                }
                else holder = null;
            }
        }
        catch (final Exception e) {
            failedToEnableNotifications(category, e);
            holder = null;
        }
        return holder != null ? holder.getMetadata() : null;
    }

    /**
     * Disables event listening for the specified subscription list.
     *
     * @param category The identifier of the subscription list.
     * @return Metadata of deleted notification.
     */
    @Override
    public final M remove(final String category) {
        final NotificationHolder<M> holder;
        try (final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
            holder = notifications.get(category);
            if (holder != null) {
                notificationRemoved(holder.getMetadata());
                notifications.remove(category);
            }
        }
        if (holder != null) {
            disableNotifications(holder.getMetadata());
            return holder.getMetadata();
        } else return null;
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            return ImmutableSet.copyOf(notifications.keySet());
        }
    }

    /**
     * Determines whether all notifications disabled.
     * @return {@literal true}, if all notifications disabled; otherwise, {@literal false}.
     */
    protected final boolean hasNoNotifications() {
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            return notifications.isEmpty();
        }
    }

    @MethodStub
    protected void disableNotifications(final M metadata){
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
        listeners.addNotificationListener(listener, filter, handback);
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
        listeners.removeNotificationListener(listener);
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
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            return toArray(notifications.values());
        }
    }

    @Override
    public final M getNotificationInfo(final String category) {
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            final NotificationHolder<M> holder = notifications.get(category);
            return holder != null ? holder.getMetadata() : null;
        }
    }

    /**
     * Reports an error when enabling notifications.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param category An event category.
     * @param e Internal connector error.
     */
    protected static void failedToEnableNotifications(final Logger logger,
                                                      final Level logLevel,
                                                      final String category,
                                                      final Exception e){
        logger.log(logLevel, String.format("Failed to enable notifications '%s'", category), e);
    }

    /**
     * Reports an error when enabling notifications.
     * @param category An event category.
     * @param e Internal connector error.
     * @see #failedToEnableNotifications(Logger, Level, String, Exception)
     */
    protected abstract void failedToEnableNotifications(final String category,
                                                        final Exception e);

    /**
     * Disables all notifications registered in this manager.
     * @param removeNotificationListeners {@literal true} to remove all notification listeners.
     * @param removeResourceEventListeners {@literal true} to remove all notification model listeners.
     */
    public final void removeAll(final boolean removeNotificationListeners,
                                final boolean removeResourceEventListeners){
        try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)){
            for(final NotificationHolder<M> holder: notifications.values()) {
                notificationRemoved(holder.getMetadata());
                disableNotifications(holder.getMetadata());
            }
            notifications.clear();
        }
        if(removeNotificationListeners)
            listeners.clear();
        if(removeResourceEventListeners)
            super.removeAllResourceEventListeners();
    }

    @Override
    public final M get(final String notifType) {
        return getNotificationInfo(notifType);
    }

    @Override
    public final int size() {
        try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)){
            return notifications.size();
        }
    }

    @Override
    public final Iterator<M> iterator() {
        return iterator(notifications.values());
    }

    protected final void failedToExpand(final Logger logger, final Level level, final Exception e){
        logger.log(level, String.format("Unable to expand events for resource %s", getResourceName()), e);
    }

    /**
     * Determines whether raising of registered events is suspended.
     *
     * @return {@literal true}, if events are suspended; otherwise {@literal false}.
     */
    @Override
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Suspends or activate raising of events.
     *
     * @param value {@literal true} to suspend events; {@literal false}, to activate events.
     */
    @Override
    public void setSuspended(final boolean value) {
        suspended = value;
    }

    /**
     * Removes all notifications from this repository.
     */
    @Override
    public void close() {
        removeAll(true, true);
    }

}
