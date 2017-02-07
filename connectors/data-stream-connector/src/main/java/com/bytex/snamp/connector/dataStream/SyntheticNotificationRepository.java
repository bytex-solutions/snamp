package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationListenerInvoker;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.instrumentation.measurements.jmx.TimeMeasurementNotification;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.bytex.snamp.internal.Utils.parallelForEach;

/**
 * Represents repository of notifications metadata.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public class SyntheticNotificationRepository extends AbstractNotificationRepository<SyntheticNotificationInfo> {
    private static final class MessageDrivenNotificationListenerInvoker extends WriteOnceRef<ExecutorService> implements NotificationListenerInvoker{

        @Override
        public void invoke(final Notification n, final Object handback, final Iterable<? extends NotificationListener> listeners) {
            final Consumer<? super NotificationListener> listenerConsumer = listener -> listener.handleNotification(n, handback);
            final ExecutorService threadPool = get();
            if(threadPool == null)
                listeners.forEach(listenerConsumer);
            else
                parallelForEach(listeners, listenerConsumer, threadPool);
        }
    }

    private final MessageDrivenNotificationListenerInvoker threadPool;

    public SyntheticNotificationRepository(final String resourceName) {
        super(resourceName, SyntheticNotificationInfo.class, false);
        threadPool = new MessageDrivenNotificationListenerInvoker();
    }

    final void init(final ExecutorService threadPool) {
        this.threadPool.set(threadPool);
    }

    private static Notification prepareNotification(final SyntheticNotificationInfo metadata, final Notification notification) {
        if (metadata.isNotificationEnabled(notification)) {
            final String newNotifType = ArrayUtils.getFirst(metadata.getNotifTypes());
            return newNotifType.equals(notification.getType()) ?
                    notification :
                    new NotificationContainer(newNotifType, notification);
        } else
            return null;
    }

    public void handleNotification(final Notification notification) {
        fire(notification.getType(), holder -> prepareNotification(holder, notification));
    }

    /**
     * Gets the invoker used to executed notification listeners.
     *
     * @return The notification listener invoker.
     */
    @Override
    protected final NotificationListenerInvoker getListenerInvoker() {
        return threadPool;
    }

    @Override
    protected SyntheticNotificationInfo connectNotifications(final String notifType, final NotificationDescriptor metadata) throws Exception {
        switch (metadata.getName(notifType)){
            case AttributeChangeNotification.ATTRIBUTE_CHANGE:
                return new SyntheticNotificationInfo(notifType, AttributeChangeNotification.class, "Occurs when one of registered attribute will be changed", metadata);
            case TimeMeasurementNotification.TYPE:
                return new SyntheticNotificationInfo(notifType, TimeMeasurementNotification.class, "Occurs when time measurement will be supplied", metadata);
            case SpanNotification.TYPE:
                return new SyntheticNotificationInfo(notifType, SpanNotification.class, "Occurs when span will be occurred", metadata);
            case ValueMeasurementNotification.TYPE:
                return new SyntheticNotificationInfo(notifType, ValueMeasurementNotification.class, "Occurs when instant measurement will be supplied", metadata);
            default:
                return new SyntheticNotificationInfo(notifType, metadata);
        }
    }
}
