package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connectors.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;

import javax.management.MBeanNotificationInfo;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * Represents collection of notifications metadata.
 * @param <M> Type of notifications in repository.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MDANotificationRepository<M extends MBeanNotificationInfo> extends AbstractNotificationRepository<M> implements SafeCloseable {
    private final NotificationListenerInvoker listenerInvoker;
    private final AccessTimer lastWriteAccess;

    protected MDANotificationRepository(final String resourceName,
                                       final Class<M> featureType,
                                       final AccessTimer accessTimer,
                                       final ExecutorService threadPool){
        super(resourceName, featureType);
        this.lastWriteAccess = Objects.requireNonNull(accessTimer);
        listenerInvoker = NotificationListenerInvokerFactory.createParallelInvoker(threadPool);
    }

    /**
     * Resets last access time.
     */
    @Override
    protected final void postFire() {
        lastWriteAccess.reset();
    }

    /**
     * Gets the invoker used to executed notification listeners.
     *
     * @return The notification listener invoker.
     */
    @Override
    protected final NotificationListenerInvoker getListenerInvoker() {
        return listenerInvoker;
    }

    protected abstract M enableNotifications(final String notifType,
                                             final OpenType<?> attachmentType,
                                             final NotificationDescriptor metadata) throws Exception;

    @Override
    protected final M enableNotifications(final String notifType, final NotificationDescriptor metadata) throws Exception {
        return enableNotifications(notifType, parseType(metadata), metadata);
    }

    protected abstract Logger getLogger();

    @Override
    protected final void failedToEnableNotifications(final String listID, final String category, final Exception e) {
        failedToEnableNotifications(getLogger(), Level.WARNING, listID, category, e);
    }

    /**
     * Releases all notifications from this repository.
     */
    @Override
    public void close() {
        removeAll(true, true);
    }
}
