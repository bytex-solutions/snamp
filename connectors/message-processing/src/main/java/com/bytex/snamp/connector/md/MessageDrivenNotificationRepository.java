package com.bytex.snamp.connector.md;

import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationListenerInvoker;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.parallelForEach;

/**
 * Represents repository of notifications metadata.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public class MessageDrivenNotificationRepository extends AbstractNotificationRepository<MBeanNotificationInfo> {
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
    private final WriteOnceRef<Logger> logger;
    private final LongCounter sequenceNumberProvider;

    public MessageDrivenNotificationRepository(final String resourceName) {
        super(resourceName, MBeanNotificationInfo.class, false);
        threadPool = new MessageDrivenNotificationListenerInvoker();
        logger = new WriteOnceRef<>();
        sequenceNumberProvider = DistributedServices.getDistributedCounter(getBundleContextOfObject(this), "SequenceGenerator-".concat(resourceName));
    }

    final void init(final ExecutorService threadPool, final Logger logger) {
        this.threadPool.set(threadPool);
        this.logger.set(logger);
    }

    public void handleNotification(final Notification notification) {
        notification.setSequenceNumber(sequenceNumberProvider.getAsLong());
        fire(notification.getType(), holder -> notification);
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
    protected MBeanNotificationInfo connectNotifications(final String notifType, final NotificationDescriptor metadata) throws Exception {
        return null;
    }

    @Override
    protected final void failedToEnableNotifications(final String category, final Exception e) {
        failedToEnableNotifications(logger.get(), Level.SEVERE, category, e);
    }
}
