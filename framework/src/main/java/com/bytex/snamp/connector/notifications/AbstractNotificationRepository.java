package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.NotificationMetric;
import com.bytex.snamp.connector.metrics.NotificationMetricRecorder;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableSet;

import javax.management.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents a base class that allows to enable notification support for the management connector.
 * @param <M> Notification metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public abstract class AbstractNotificationRepository<M extends MBeanNotificationInfo> extends AbstractFeatureRepository<M> implements NotificationSupport, SafeCloseable {
    /**
     * Represents batch notification sender.
     * This class cannot be inherited or instantiated directly from your code.
     */
    protected final class NotificationCollector{
        private final Collection<Notification> notifications;

        private NotificationCollector(){
            this.notifications = new LinkedList<>();
        }

        /**
         * Enqueues a notification to sent in future.
         * @param metadata Notification metadata.
         * @param message Notification message.
         * @param sequenceNumberGenerator Sequence number generator. Cannot be {@literal null}.
         * @param userData Advanced data to be associated with the notification.
         */
        public void enqueue(final MBeanNotificationInfo metadata,
                               final String message,
                            final LongSupplier sequenceNumberGenerator,
                               final Object userData){
            enqueue(metadata,
                    message,
                    sequenceNumberGenerator.getAsLong(),
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
        public void enqueue(final MBeanNotificationInfo metadata,
                               final String message,
                               final long sequenceNumber,
                               final long timeStamp,
                               final Object userData){
            for(final String category: metadata.getNotifTypes())
                notifications.add(new NotificationBuilder()
                        .setType(category)
                        .setTimeStamp(timeStamp)
                        .setSequenceNumber(sequenceNumber)
                        .setSource(AbstractNotificationRepository.this)
                        .setMessage(message)
                        .setUserData(userData)
                        .get());
        }
    }

    private final KeyedObjects<String, M> notifications;
    private final NotificationListenerList listeners;
    private final NotificationMetricRecorder metrics;
    private final boolean expandable;

    /**
     * Initializes a new notification manager.
     * @param resourceName The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata.
     * @param expandable {@literal true}, if repository can be populated automatically; otherwise, {@literal false}.
     */
    protected AbstractNotificationRepository(final String resourceName,
                                             final Class<M> notifMetadataType,
                                             final boolean expandable) {
        super(resourceName, notifMetadataType);
        notifications = AbstractKeyedObjects.create(metadata -> ArrayUtils.getFirst(metadata.getNotifTypes()));
        listeners = new NotificationListenerList();
        metrics = new NotificationMetricRecorder();
        this.expandable = expandable;
    }

    /**
     * Gets metrics associated with activity of the features in this repository.
     *
     * @return Metrics associated with activity in this repository.
     */
    @Override
    public final NotificationMetric getMetrics() {
        return metrics;
    }

    /**
     * Gets the invoker used to executed notification listeners.
     * @return The notification listener invoker.
     */
    protected abstract NotificationListenerInvoker getListenerInvoker();

    protected final void fire(final BiConsumer<? super M, ? super NotificationCollector> sender){
        final NotificationCollector collector = new NotificationCollector();
        readLock.accept(SingleResourceGroup.INSTANCE, notifications.values(), collector, (notifications, collector1) -> notifications.forEach(n -> sender.accept(n, collector1)));
        fireListeners(collector.notifications);
        collector.notifications.clear();    //help GC
    }

    /**
     * Invokes all listeners associated with the specified notification category.
     * @param category An event category.
     * @param message The human-readable message associated with the notification.
     * @param sequenceNumberProvider Sequence number generator. Cannot be {@literal null}.
     * @param userData Advanced object associated with the notification.
     * @return {@literal true}, if one of the registered notifications is raised; otherwise, {@literal false}.
     */
    protected final boolean fire(final String category,
                              final String message,
                              final LongSupplier sequenceNumberProvider,
                              final Object userData) {
        return fire(category, message, sequenceNumberProvider.getAsLong(), System.currentTimeMillis(), userData);
    }

    protected final boolean fire(final String category,
                                 final Function<? super M, ? extends Notification> notificationFactory){
        if(isSuspended())
            return false;
        final Collection<Notification> notifs = readLock.apply(SingleResourceGroup.INSTANCE, notifications, n -> n.values().stream()
                .filter(holder -> Objects.equals(NotificationDescriptor.getName(holder), category))
                .map(holder -> {
                    final Notification notification = notificationFactory.apply(holder);
                    notification.setSource(this);
                    return notification;
                })
                .collect(Collectors.toList()));
        final boolean hasNotifications = !notifs.isEmpty();
        //fire listeners
        fireListeners(notifs);
        notifs.clear();     //help GC
        return hasNotifications;
    }

    protected final boolean fire(final String category,
                              final String message,
                              final long sequenceNumber,
                              final long timeStamp,
                              final Object userData) {
        return fire(category, holder -> new NotificationBuilder()
                .setType(ArrayUtils.getFirst(holder.getNotifTypes()))
                .setTimeStamp(timeStamp)
                .setSequenceNumber(sequenceNumber)
                .setMessage(message)
                .setUserData(userData)
                .get());
    }

    /**
     * Determines whether all listeners in this repository should be suspended.
     * @return {@literal true} to suspend all notifications; otherwise, {@literal false}.
     */
    protected boolean isSuspended(){
        return false;
    }

    @MethodStub
    protected void interceptFire(final Collection<? extends Notification> notifications){

    }

    final void fireListenersNoIntercept(final Notification n){
        listeners.handleNotification(getListenerInvoker(), n, null);
        metrics.update();
    }

    private void fireListeners(final Collection<? extends Notification> notifications) {
        notifications.forEach(this::fireListenersNoIntercept);
        interceptFire(notifications);
    }

    private void notificationAdded(final M metadata){
        fireResourceEvent(new NotificationAddedEvent(this, getResourceName(), metadata));
    }

    private void notificationRemoved(final M metadata){
        fireResourceEvent(new NotificationRemovingEvent(this, getResourceName(), metadata));
    }

    protected abstract M connectNotifications(final String notifType,
                                            final NotificationDescriptor metadata) throws Exception;

    private static boolean equals(final MBeanNotificationInfo info, final String category, final Descriptor descriptor){
        return ArrayUtils.containsAny(info.getNotifTypes(), category) && descriptor.equals(info.getDescriptor());
    }

    private M connectAndAdd(final String category, final NotificationDescriptor descriptor) throws Exception{
        final M metadata = connectNotifications(category, descriptor);
        if (metadata != null) {
            notifications.put(metadata);
            notificationAdded(metadata);
        }
        return metadata;
    }

    private M enableNotificationsImpl(final String category, final NotificationDescriptor descriptor) throws Exception {
        M holder = notifications.get(category);
        if (holder != null) {
            if (equals(holder, category, descriptor))
                return holder;
            else {
                //remove notification
                notificationRemoved(holder);
                holder = notifications.remove(category);
                disconnectNotifications(holder);
                //and register again
                holder = connectAndAdd(category, descriptor);
            }
        } else
            holder = connectAndAdd(category, descriptor);
        return holder;
    }

    /**
     * Enables event listening for the specified category of events.
     *
     * @param category The name of the event category to listen.
     * @param descriptor  Event discovery options.
     * @return Metadata of created notification.
     */
    @Override
    public final M enableNotifications(final String category, final NotificationDescriptor descriptor) {
        try {
            return writeLock.call(SingleResourceGroup.INSTANCE, () -> enableNotificationsImpl(category, descriptor), null);
        } catch (final Exception e) {
            failedToEnableNotifications(category, e);
            return null;
        }
    }

    private M removeImpl(final String category) {
        final M holder = notifications.get(category);
        if (holder != null)
            notificationRemoved(holder);
        return notifications.remove(category);
    }

    /**
     * Disables event listening for the specified subscription list.
     *
     * @param category The identifier of the subscription list.
     * @return Metadata of deleted notification.
     */
    @Override
    public final M remove(final String category) {
        final M metadata = writeLock.apply(SingleResourceGroup.INSTANCE, this, category, AbstractNotificationRepository::removeImpl);
        if (metadata != null)
            disconnectNotifications(metadata);
        return metadata;
    }

    /**
     * Disables notifications of the specified category.
     * @param category Category of notifications to disable.
     * @return An instance of disabled notification category; or {@literal null}, if notification with the specified category doesn't exist.
     * @since 2.0
     */
    @Override
    public final M disableNotifications(final String category) {
        return remove(category);
    }

    /**
     * Disables all notifications except specified in the collection.
     *
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
    @Override
    public final void retainNotifications(final Set<String> events) {
        retainAll(events);
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        return readLock.apply(SingleResourceGroup.INSTANCE, notifications, notifs -> ImmutableSet.copyOf(notifs.keySet()));
    }

    /**
     * Determines whether all notifications disabled.
     * @return {@literal true}, if all notifications disabled; otherwise, {@literal false}.
     */
    protected final boolean hasNoNotifications() {
        return readLock.supplyBool(SingleResourceGroup.INSTANCE, notifications::isEmpty);
    }

    @MethodStub
    protected void disconnectNotifications(final M metadata){
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
        return readLock.apply(SingleResourceGroup.INSTANCE, notifications.values(), this::toArray);
    }

    @Override
    public final M getNotificationInfo(final String category) {
        return readLock.apply(SingleResourceGroup.INSTANCE, notifications, category, Map::get);
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

    private void removeAllImpl(final KeyedObjects<String, M> notifications){
        notifications.values().forEach(metadata -> {
            notificationRemoved(metadata);
            disconnectNotifications(metadata);
        });
        notifications.clear();
    }

    /**
     * Populate this repository with notifications.
     *
     * @return A collection of registered notifications; or empty collection if nothing tot populate.
     */
    @Override
    public Collection<? extends M> expandNotifications() {
        return Collections.emptyList();
    }

    /**
     * Determines whether this repository can be populated with notifications using call of {@link #expandNotifications()}.
     *
     * @return {@literal true}, if this repository can be populated; otherwise, {@literal false}.
     * @since 2.0
     */
    @Override
    public final boolean canExpandNotifications() {
        return expandable;
    }

    /**
     * Disables all notifications registered in this manager.
     * @param removeNotificationListeners {@literal true} to remove all notification listeners.
     * @param removeResourceEventListeners {@literal true} to remove all notification model listeners.
     */
    public final void removeAll(final boolean removeNotificationListeners,
                                final boolean removeResourceEventListeners) {
        writeLock.accept(SingleResourceGroup.INSTANCE, this, notifications, AbstractNotificationRepository::removeAllImpl);
        if (removeNotificationListeners)
            listeners.clear();
        if (removeResourceEventListeners)
            super.removeAllResourceEventListeners();
    }

    @Override
    public final M get(final String notifType) {
        return getNotificationInfo(notifType);
    }

    @Override
    public final int size() {
        return readLock.supplyInt(SingleResourceGroup.INSTANCE, notifications::size);
    }

    @Override
    public final Iterator<M> iterator() {
        return readLock.apply(SingleResourceGroup.INSTANCE, notifications.values(), Collection::iterator);
    }

    @Override
    public final void forEach(final Consumer<? super M> action) {
        readLock.accept(SingleResourceGroup.INSTANCE, notifications.values(), action, Iterable::forEach);
    }

    protected final void failedToExpand(final Logger logger, final Level level, final Exception e){
        logger.log(level, String.format("Unable to expand events for resource %s", getResourceName()), e);
    }

    /**
     * Removes all notifications from this repository.
     */
    @Override
    public void close() {
        removeAll(true, true);
    }

}
