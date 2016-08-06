package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connectors.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;

import javax.management.openmbean.OpenType;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * Represents collection of notifications metadata.
 * @param <M> Type of notifications in repository.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class MDANotificationRepository<M extends MDANotificationInfo> extends AbstractNotificationRepository<M> implements SafeCloseable {
    private final NotificationListenerInvoker listenerInvoker;
    private AccessTimer lastWriteAccess;

    protected MDANotificationRepository(final String resourceName,
                                       final Class<M> featureType,
                                       final ExecutorService threadPool){
        this(resourceName, featureType, threadPool, DistributedServices.getProcessLocalCounterGenerator("notifications-".concat(resourceName)));
    }

    protected MDANotificationRepository(final String resourceName,
                                        final Class<M> featureType,
                                        final ExecutorService threadPool,
                                        final LongCounter sequenceNumberGenerator){
        super(resourceName, featureType, sequenceNumberGenerator);
        listenerInvoker = NotificationListenerInvokerFactory.createParallelInvoker(threadPool);
    }

    final void init(final AccessTimer accessTimer){
        this.lastWriteAccess = accessTimer;
    }

    /**
     * Resets last access time.
     */
    @Override
    protected final void interceptFire() {
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

    protected abstract M createNotificationMetadata(final String notifType,
                                                    final NotificationDescriptor metadata) throws Exception;

    @Override
    protected final M enableNotifications(final String notifType, final NotificationDescriptor metadata) throws Exception {
        final OpenType<?> attachmentType = parseType(metadata);
        final M result = createNotificationMetadata(notifType, metadata.setUserDataType(attachmentType));
        result.init(attachmentType);
        return result;
    }

    protected abstract Logger getLogger();

    @Override
    protected final void failedToEnableNotifications(final String category, final Exception e) {
        failedToEnableNotifications(getLogger(), Level.WARNING, category, e);
    }

    /**
     * Releases all notifications from this repository.
     */
    @Override
    public void close() {
        removeAll(true, true);
    }
}
