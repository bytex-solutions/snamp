package com.bytex.snamp.connectors.notifications;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.bytex.snamp.connectors.AbstractFeatureModeler;
import com.bytex.snamp.core.LogicalOperation;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.internal.annotations.MethodStub;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a base class that allows to enable notification support for the management connector.
 * @param <M> Notification metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractNotificationSupport<M extends MBeanNotificationInfo> extends AbstractFeatureModeler<M> implements NotificationSupport {
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
            return getMetadata().getNotifTypes()[0];
        }

        private boolean equals(final String category, final CompositeData options){
            return super.identity.equals(computeIdentity(category, options));
        }

        private static BigInteger computeIdentity(final String category,
                                                  final CompositeData options) {
            BigInteger result = toBigInteger(category);
            for (final String propertyName : options.getCompositeType().keySet())
                result = result.xor(toBigInteger(propertyName))
                        .xor(new BigInteger(Integer.toString(options.get(propertyName).hashCode())));
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
            for(final String listID: metadata.getNotifTypes())
                add(createNotification(listID, message, userData));
        }
    }

    private final KeyedObjects<String, NotificationHolder<M>> notifications;
    private final NotificationListenerList listeners;
    private final AtomicLong sequenceCounter;

    /**
     * Initializes a new notification manager.
     * @param resourceName The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata;
     */
    protected AbstractNotificationSupport(final String resourceName,
                                          final Class<M> notifMetadataType) {
        super(resourceName,
                notifMetadataType,
                ANSResource.class,
                ANSResource.RESOURCE_EVENT_LISTENERS);
        notifications = createNotifications();
        listeners = new NotificationListenerList();
        sequenceCounter = new AtomicLong(0L);
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
     * Gets subscription model.
     * @return The subscription model.
     */
    @Override
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

    private Notification createNotification(final String listID,
                                                   final String message,
                                                   final Object userData){
        return new NotificationBuilder()
                .setTimeStamp()
                .setSequenceNumber(sequenceCounter.getAndIncrement())
                .setType(listID)
                .setSource(this)
                .setMessage(message)
                .setUserData(userData)
                .get();
    }

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
        final Collection<Notification> notifs;
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            notifs = Lists.newArrayListWithExpectedSize(notifications.size());
            for (final NotificationHolder<M> holder : notifications.values())
                if (Objects.equals(NotificationDescriptor.getNotificationCategory(holder.getMetadata()), category))
                    for (final String listId : holder.getMetadata().getNotifTypes()) {
                        final Notification n = createNotification(listId,
                                message,
                                userData);
                        notifs.add(n);
                    }
        }
        //fire listeners
        fireListeners(notifs);
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
     * <p/>
     * category can be used for enabling notifications for the same category
     * but with different options.
     * <p/>
     * listId parameter
     * is used as a value of {@link Notification#getType()}.
     *
     * @param listId   An identifier of the subscription list.
     * @param category The name of the event category to listen.
     * @param options  Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     */
    public final M enableNotifications(final String listId, final String category, final CompositeData options) {
        NotificationHolder<M> holder;
        try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
            holder = notifications.get(listId);
            if(holder != null) {
                if (holder.equals(category, options))
                    return holder.getMetadata();
                else {
                    //remove notification
                    notificationRemoved(holder.getMetadata());
                    holder = notifications.remove(listId);
                    //and register again
                    disableNotifications(holder.getMetadata());
                    final M metadata = enableNotifications(listId, new NotificationDescriptor(category, getSubscriptionModel(), options));
                    if (metadata != null) {
                        notifications.put(holder = new NotificationHolder<>(metadata, category, options));
                        notificationAdded(holder.getMetadata());
                    }
                }
            }
            else {
                final M metadata = enableNotifications(listId, new NotificationDescriptor(category, getSubscriptionModel(), options));
                if(metadata != null) {
                    notifications.put(holder = new NotificationHolder<>(metadata, category, options));
                    notificationAdded(holder.getMetadata());
                }
                else holder = null;
            }
        }
        catch (final Exception e) {
            failedToEnableNotifications(listId, category, e);
            holder = null;
        }
        return holder != null ? holder.getMetadata() : null;
    }

    /**
     * Disables event listening for the specified subscription list.
     *
     * @param listId The identifier of the subscription list.
     * @return Metadata of deleted notification.
     */
    @Override
    public final M remove(final String listId) {
        final NotificationHolder<M> holder;
        try (final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)) {
            holder = notifications.get(listId);
            if (holder != null) {
                notificationRemoved(holder.getMetadata());
                notifications.remove(listId);
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
    public final M getNotificationInfo(final String listID) {
        try (final LockScope ignored = beginRead(ANSResource.NOTIFICATIONS)) {
            final NotificationHolder<M> holder = notifications.get(listID);
            return holder != null ? holder.getMetadata() : null;
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
        logger.log(logLevel, String.format("Failed to enable notifications '%s' for %s subscription list. Context: %s",
                category, listID, LogicalOperation.current()), e);
    }

    /**
     * Reports an error when enabling notifications.
     * @param listID Subscription list identifier.
     * @param category An event category.
     * @param e Internal connector error.
     * @see #failedToEnableNotifications(Logger, Level, String, String, Exception)
     */
    protected abstract void failedToEnableNotifications(final String listID,
                                                        final String category,
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
    public final boolean isRegistered(final String notifType) {
        try(final LockScope ignored = beginWrite(ANSResource.NOTIFICATIONS)){
            return notifications.containsKey(notifType);
        }
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
}
