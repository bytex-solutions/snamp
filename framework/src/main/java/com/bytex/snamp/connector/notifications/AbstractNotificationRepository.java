package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.concurrent.LockDecorator;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.metrics.NotificationMetrics;
import com.bytex.snamp.connector.metrics.NotificationMetricsRecorder;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a base class that allows to enable notification support for the management connector.
 * @param <M> Notification metadata.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 * @deprecated Use {@link NotificationRepository} instead.
 */
@Deprecated
public abstract class AbstractNotificationRepository<M extends MBeanNotificationInfo> extends AbstractFeatureRepository<M> implements NotificationBroadcaster {
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
    private final NotificationMetricsRecorder metrics;
    private final LazyReference<ExecutorService> defaultExecutor;
    private final LockDecorator readLock, writeLock;

    {
        notifications = AbstractKeyedObjects.create(AbstractNotificationRepository::extractNotificationType);
        listeners = new NotificationListenerList();
        metrics = new NotificationMetricsRecorder();
        defaultExecutor = LazyReference.strong();
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = LockDecorator.readLock(rwLock);
        writeLock = LockDecorator.writeLock(rwLock);
    }

    /**
     * Initializes a new notification manager.
     * @param resourceName The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata.
     */
    protected AbstractNotificationRepository(final String resourceName,
                                             final Class<M> notifMetadataType) {
        super(resourceName, notifMetadataType);
    }

    /**
     * Initializes a new notification manager.
     * @param source Owner of this
     * @param notifMetadataType Type of the notification metadata.
     */
    protected AbstractNotificationRepository(final AbstractManagedResourceConnector source,
                                             final Class<M> notifMetadataType){
        super(source, notifMetadataType);
    }

    private static String extractNotificationType(final MBeanNotificationInfo metadata) {
        return ArrayUtils.getFirst(metadata.getNotifTypes()).orElseThrow(AssertionError::new);
    }

    /**
     * Gets metrics associated with activity of the features in this repository.
     *
     * @return Metrics associated with activity in this repository.
     */
    @Override
    public final NotificationMetrics getMetrics() {
        return metrics;
    }

    /**
     * Gets an executor used to execute event listeners.
     * @return Executor service.
     */
    @Nonnull
    protected ExecutorService getListenerExecutor(){
        return defaultExecutor.get(MoreExecutors::newDirectExecutorService);
    }

    protected final void fire(final BiConsumer<? super M, ? super NotificationCollector> sender){
        final NotificationCollector collector = new NotificationCollector();
        readLock.accept(notifications.values(), collector, (notifications, collector1) -> notifications.forEach(n -> sender.accept(n, collector1)));
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
                                 final Function<? super M, ? extends Notification> notificationFactory) {
        if (isSuspended())
            return false;
        final Collection<Notification> notifs = new LinkedList<>();
        try (final SafeCloseable ignored = readLock.acquireLock()) {
            notifications.values().stream()
                    .filter(holder -> Objects.equals(NotificationDescriptor.getName(holder), category))
                    .map(notificationFactory)
                    .filter(Objects::nonNull)
                    .forEach(notifs::add);
        }
        final boolean hasNotifications = !notifs.isEmpty();
        //fire listeners
        fireListeners(notifs);
        notifs.clear();     //help GC
        return hasNotifications;
    }

    private static Optional<String> getNotificationType(final MBeanNotificationInfo metadata){
        return ArrayUtils.getFirst(metadata.getNotifTypes());
    }

    protected final boolean fire(final String category,
                              final String message,
                              final long sequenceNumber,
                              final long timeStamp,
                              final Object userData) {
        return fire(category, holder -> getNotificationType(holder).map(newNotifType -> new NotificationBuilder()
                .setType(newNotifType)
                .setTimeStamp(timeStamp)
                .setSequenceNumber(sequenceNumber)
                .setMessage(message)
                .setUserData(userData)
                .setSource(getSource())
                .get()).orElse(null));
    }

    protected static Notification wrapNotification(final MBeanNotificationInfo metadata,
                                                          final Notification prototype) {
        return getNotificationType(metadata)
                .map(newNotifType -> NotificationContainer.create(newNotifType, prototype))
                .orElse(null);
    }

    protected final boolean fire(final Notification notification, final boolean adjustNotificationType) {
        final String category;
        if (adjustNotificationType) {
            final Optional<M> metadata = getNotificationInfo(notification.getType());
            if (metadata.isPresent())
                category = NotificationDescriptor.getName(metadata.get());
            else
                return false;
        } else
            category = notification.getType();
        return fire(category, holder -> wrapNotification(holder, notification));
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
        n.setSource(getSource());
        listeners.fireAsync(n, getListenerExecutor());
        metrics.update();
    }

    private void fireListeners(final Collection<? extends Notification> notifications) {
        notifications.forEach(this::fireListenersNoIntercept);
        interceptFire(notifications);
    }

    protected abstract M connectNotifications(final String notifType,
                                            final NotificationDescriptor metadata) throws Exception;

    private static boolean equals(final MBeanNotificationInfo info, final String category, final Descriptor descriptor){
        return ArrayUtils.contains(info.getNotifTypes(), category) && descriptor.equals(info.getDescriptor());
    }

    private M connectAndAdd(final String category, final NotificationDescriptor descriptor) throws Exception{
        final M metadata = connectNotifications(category, descriptor);
        if (metadata != null) {
            notifications.put(metadata);
            featureAdded(metadata);
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
                removingFeature(holder);
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
    public final Optional<M> enableNotifications(final String category, final NotificationDescriptor descriptor) {
        M result;
        try {
            result = writeLock.call(() -> enableNotificationsImpl(category, descriptor), null);
        } catch (final Exception e) {
            getLogger().log(Level.WARNING, String.format("Failed to enable notifications '%s'", category), e);
            result = null;
        }
        return Optional.ofNullable(result);
    }

    private M removeImpl(final String category) {
        final M holder = notifications.get(category);
        if (holder != null)
            removingFeature(holder);
        return notifications.remove(category);
    }

    /**
     * Disables event listening for the specified subscription list.
     *
     * @param category The identifier of the subscription list.
     * @return Metadata of deleted notification.
     */
    @Override
    public final Optional<M> remove(final String category) {
        final M metadata = writeLock.apply(this, category, AbstractNotificationRepository<M>::removeImpl);
        if (metadata == null)
            return Optional.empty();
        else {
            disconnectNotifications(metadata);
            return Optional.of(metadata);
        }
    }

    /**
     * Disables notifications of the specified category.
     * @param category Category of notifications to disable.
     * @return An instance of disabled notification category; or {@link Optional#empty()}, if notification with the specified category doesn't exist.
     * @since 2.0
     */
    public final Optional<M> disableNotifications(final String category) {
        return remove(category);
    }

    /**
     * Disables all notifications except specified in the collection.
     *
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
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
        return readLock.apply(notifications, notifs -> ImmutableSet.copyOf(notifs.keySet()));
    }

    /**
     * Determines whether all notifications disabled.
     * @return {@literal true}, if all notifications disabled; otherwise, {@literal false}.
     */
    protected final boolean hasNoNotifications() {
        return readLock.supplyBool(notifications::isEmpty);
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
        return readLock.apply(this, notifications.values(), AbstractNotificationRepository<M>::toArray);
    }

    public final Optional<M> getNotificationInfo(final String category) {
        return Optional.ofNullable(readLock.apply(notifications, category, Map::get));
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    private void clearImpl() {
        notifications.values().forEach(metadata -> {
            removingFeature(metadata);
            disconnectNotifications(metadata);
        });
        notifications.clear();
    }

    /**
     * Removes all features from this repository.
     *
     * @since 2.0
     */
    @Override
    public final void clear() {
        writeLock.run(this::clearImpl);
    }

    @Override
    public final Optional<M> get(final String notifType) {
        return getNotificationInfo(notifType);
    }

    @Override
    public final int size() {
        return readLock.supplyInt(notifications::size);
    }

    @Override
    @Nonnull
    public final Iterator<M> iterator() {
        return readLock.apply(notifications.values(), Collection::iterator);
    }

    @Override
    public final void forEach(final Consumer<? super M> action) {
        readLock.accept(notifications.values(), action, Iterable::forEach);
    }

    protected final void failedToExpand(final Level level, final Exception e){
        getLogger().log(level, String.format("Unable to expand events for resource %s", getResourceName()), e);
    }

    /**
     * Removes all notifications from this repository.
     */
    @Override
    public void close() {
        defaultExecutor.remove();
        listeners.clear();
        metrics.reset();
        super.close();
    }

    protected final NotificationDescriptor createDescriptor(final Consumer<EventConfiguration> initializer) {
        return createDescriptor(EventConfiguration.class, initializer, NotificationDescriptor::new);
    }

    protected final NotificationDescriptor createDescriptor(){
        return createDescriptor(config -> {});
    }

    public Map<String, NotificationDescriptor> discoverNotifications(){
        return Collections.emptyMap();
    }

    /**
     * Converts this repository into {@link NotificationManager}.
     * @return An instance of manager.
     * @since 2.1
     */
    public NotificationManager createManager(){
        final class DefaultNotificationManager implements NotificationManager {
            @Override
            public void enableNotifications(final String category, final NotificationDescriptor descriptor) {
                AbstractNotificationRepository.this.enableNotifications(category, descriptor);
            }

            @Override
            public boolean disableNotifications(final String category) {
                return AbstractNotificationRepository.this.disableNotifications(category).isPresent();
            }

            @Override
            public void retainNotifications(final Set<String> events) {
                AbstractNotificationRepository.this.retainNotifications(events);
            }

            @Override
            public Map<String, NotificationDescriptor> discoverNotifications() {
                return AbstractNotificationRepository.this.discoverNotifications();
            }

            @Override
            public int hashCode() {
                return AbstractNotificationRepository.this.hashCode();
            }

            private boolean equalsOwner(final AbstractNotificationRepository<?> other){
                return AbstractNotificationRepository.this.equals(other);
            }

            private boolean equals(final DefaultNotificationManager other) {
                return other.equalsOwner(AbstractNotificationRepository.this);
            }

            @Override
            public boolean equals(final Object other) {
                return this == other || getClass().isInstance(other) && equals((DefaultNotificationManager) other);
            }

            @Override
            public String toString() {
                return AbstractNotificationRepository.this.toString();
            }
        }
        return new DefaultNotificationManager();
    }
}
