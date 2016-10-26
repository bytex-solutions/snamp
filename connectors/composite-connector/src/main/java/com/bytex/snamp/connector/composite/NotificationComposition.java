package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.core.LongCounter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.core.DistributedServices.getDistributedCounter;

/**
 * Represents composition of notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationComposition extends AbstractNotificationRepository<CompositeNotification> implements NotificationListener{
    private final NotificationSupportProvider provider;
    /*
        State of current subscription.
        Key is a connector type
        Values are the set of subscribed notifications
        This state used to attach and remove listeners
     */
    private final Multimap<String, String> subscription;
    private final Logger logger;
    private final NotificationListenerInvoker listenerInvoker;

    NotificationComposition(final String resourceName,
                            final NotificationSupportProvider provider,
                            final ExecutorService threadPool,
                            final Logger logger){
        super(resourceName, CompositeNotification.class, false);
        this.provider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
        this.subscription = HashMultimap.create();
        listenerInvoker = NotificationListenerInvokerFactory.createParallelInvoker(threadPool);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final CompositeNotification compositeNotification = getNotificationInfo(notification.getType());
        if (compositeNotification != null)
            fire(NotificationDescriptor.getName(compositeNotification), notification.getMessage(), notification.getSequenceNumber(), notification.getTimeStamp(), notification.getUserData());
    }

    @Override
    protected NotificationListenerInvoker getListenerInvoker() {
        return listenerInvoker;
    }


    @Override
    protected CompositeNotification connectNotifications(final String notifType, final NotificationDescriptor metadata) throws MBeanException, ReflectionException, AbsentCompositeConfigurationParameterException {
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(metadata);
        final NotificationSupport support = provider.getNotificationSupport(connectorType);
        if (support == null)
            throw new MBeanException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support notifications", connectorType)));
        final MBeanNotificationInfo underlyingNotif = support.enableNotifications(notifType, metadata);
        if (underlyingNotif == null)
            throw new ReflectionException(new IllegalStateException(String.format("Connector '%s' could not enable notification '%s'", connectorType, notifType)));
        //update state of subscription
        if (subscription.get(connectorType).isEmpty()) {
            support.addNotificationListener(this, null, null);
        }
        subscription.put(connectorType, notifType);
        return new CompositeNotification(connectorType, underlyingNotif);
    }

    @Override
    protected void disconnectNotifications(final CompositeNotification metadata) {
        final NotificationSupport support = provider.getNotificationSupport(metadata.getConnectorType());
        if (support != null)
            for (final String notifType : metadata.getNotifTypes())
                support.disableNotifications(notifType);
        //update state of subscription
        for (final String notifType : metadata.getNotifTypes())
            subscription.remove(metadata.getConnectorType(), notifType);
        if (support != null && subscription.get(metadata.getConnectorType()).isEmpty())
            try {
                support.removeNotificationListener(this);
            } catch (final ListenerNotFoundException e) {
                logger.log(Level.SEVERE, String.format("Unable to unsubscribe normally from notifications provided by connector '%s'. Subscription state: %s", metadata.getConnectorType(), subscription));
            }
    }

    @Override
    protected void failedToEnableNotifications(final String category, final Exception e) {
        failedToEnableNotifications(logger, Level.WARNING, category, e);
    }

    /**
     * Removes all notifications from this repository.
     */
    @Override
    public void close() {
        subscription.clear();
        super.close();
    }
}
