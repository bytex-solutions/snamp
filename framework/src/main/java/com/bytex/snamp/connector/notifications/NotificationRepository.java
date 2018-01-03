package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.FeatureRepository;
import com.bytex.snamp.connector.metrics.NotificationMetrics;
import com.bytex.snamp.connector.metrics.NotificationMetricsRecorder;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents repository of notifications.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public class NotificationRepository<F extends MBeanNotificationInfo> extends FeatureRepository<F> implements NotificationBroadcaster {
    private static final long serialVersionUID = -1714704908592534147L;
    private transient final NotificationListenerList listeners;
    private transient final NotificationMetricsRecorder recorder;
    /**
     * Represents metrics associated with notifications.
     */
    public final NotificationMetrics metrics;

    public NotificationRepository(){
        listeners = new NotificationListenerList();
        metrics = recorder = new NotificationMetricsRecorder();
    }

    protected final <I> Iterable<Notification> generateNotifications(final String category,
                                                                     final I input,
                                                                     final BiFunction<? super I, ? super F, Stream<Notification>> factory){
        final Collection<Notification> notifs = new LinkedList<>();
        try (final SafeCloseable ignored = readLock.acquireLock()) {
            getResource().values().stream()
                    .filter(holder -> NotificationDescriptor.hasName(holder, category))
                    .flatMap(holder -> factory.apply(input, holder))
                    .filter(Objects::nonNull)
                    .forEach(notifs::add);
        }
        return notifs;
    }

    private <I> void emit(final String category,
                              final I input,
                              final BiFunction<? super I, ? super F, Stream<Notification>> factory,
                              final BiConsumer<? super NotificationListenerList, ? super Notification> fireEngine) {
        //fire listeners
        for (final Iterator<Notification> iterator = generateNotifications(category, input, factory).iterator(); iterator.hasNext(); ) {
            final Notification n = iterator.next();
            fireEngine.accept(listeners, n);
            iterator.remove();  //help GC
            recorder.update();
        }
    }

    public final <I> void emitStream(final String category, final I input, @Nonnull final BiFunction<? super I, ? super F, Stream<Notification>> factory) {
        emit(category, input, factory, NotificationListenerList::fire);
    }

    public final <I> void emitSingle(final String category, final I input, @Nonnull final BiFunction<? super I, ? super F, ? extends Notification> factory) {
        emitStream(category, input, (i, m) -> Stream.of(factory.apply(i, m)));
    }

    public final void emitStream(final String category, @Nonnull final Function<? super F, Stream<Notification>> factory) {
        emitStream(category, null, (i, m) -> factory.apply(m));
    }

    public final void emitSingle(final String category, @Nonnull final Function<? super F, ? extends Notification> factory) {
        emitStream(category, null, (i, m) -> Stream.of(factory.apply(m)));
    }

    public final <I> void emitStream(final String category, final I input, @Nonnull final BiFunction<? super I, ? super F, Stream<Notification>> factory, @Nonnull final ExecutorService executor) {
        emit(category, input, factory, (listeners, n) -> listeners.fireAsync(n, executor));
    }

    public final void emitStream(final String category, @Nonnull final Function<? super F, Stream<Notification>> factory, @Nonnull final ExecutorService executor) {
        emitStream(category, null, (i, m) -> factory.apply(m), executor);
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
     * @throws ListenerNotFoundException The listener is not
     *                                   registered with the MBean.
     * @see #addNotificationListener
     * @see NotificationEmitter#removeNotificationListener
     */
    @Override
    public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        listeners.removeNotificationListener(listener);
    }

    public final void removeNotificationListeners(){
        listeners.clear();
    }

    /**
     * <p>Returns an array indicating, for each notification this
     * MBean may send, the name of the Java class of the notification
     * and the notification type.</p>
     * <p>
     * <p>It is not illegal for the MBean to send notifications not
     * described in this array.  However, some clients of the MBean
     * server may depend on the array being complete for their correct
     * functioning.</p>
     *
     * @return the array of possible notifications.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return read(features -> features.values().toArray(emptyArray(MBeanNotificationInfo[].class)));
    }

    public static Optional<? extends MBeanNotificationInfo> findNotification(final String type, final MBeanInfo info) {
        return findFeature(info, MBeanInfo::getNotifications, notif -> ArrayUtils.contains(notif.getNotifTypes(), type));
    }
}
