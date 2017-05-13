package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.notifications.AbstractNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.Nonnull;
import javax.management.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final Executor listenerInvoker;

    NotificationComposition(final String resourceName,
                            final NotificationSupportProvider provider,
                            final ExecutorService threadPool){
        super(resourceName, CompositeNotification.class, false);
        this.provider = Objects.requireNonNull(provider);
        this.subscription = HashMultimap.create();
        listenerInvoker = threadPool;
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final Optional<CompositeNotification> compositeNotification = getNotificationInfo(notification.getType());
        if (compositeNotification.isPresent())
            fire(NotificationDescriptor.getName(compositeNotification.get()), notification.getMessage(), notification.getSequenceNumber(), notification.getTimeStamp(), notification.getUserData());
    }

    /**
     * Gets an executor used to execute event listeners.
     *
     * @return Executor service.
     */
    @Nonnull
    @Override
    protected Executor getListenerExecutor() {
        return listenerInvoker;
    }

    @Override
    protected CompositeNotification connectNotifications(final String notifType, final NotificationDescriptor metadata) throws MBeanException, ReflectionException, AbsentCompositeConfigurationParameterException {
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(metadata);
        final NotificationSupport support = provider.getNotificationSupport(connectorType)
                .orElseThrow(() -> new MBeanException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support notifications", connectorType))));
        final MBeanNotificationInfo underlyingNotif = support.enableNotifications(notifType, metadata)
                .orElseThrow(() -> new ReflectionException(new IllegalStateException(String.format("Connector '%s' could not enable notification '%s'", connectorType, notifType))));
        //update state of subscription
        if (subscription.get(connectorType).isEmpty()) {
            support.addNotificationListener(this, null, null);
        }
        subscription.put(connectorType, notifType);
        return new CompositeNotification(connectorType, underlyingNotif);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    protected void disconnectNotifications(final CompositeNotification metadata) {
        final Optional<NotificationSupport> support = provider.getNotificationSupport(metadata.getConnectorType());
        support.ifPresent(notificationSupport -> {
            for (final String notifType : metadata.getNotifTypes())
                notificationSupport.disableNotifications(notifType);
        });
        //update state of subscription
        for (final String notifType : metadata.getNotifTypes())
            subscription.remove(metadata.getConnectorType(), notifType);
        if (support.isPresent() && subscription.get(metadata.getConnectorType()).isEmpty())
            try {
                support.get().removeNotificationListener(this);
            } catch (final ListenerNotFoundException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to unsubscribe normally from notifications provided by connector '%s'. Subscription state: %s", metadata.getConnectorType(), subscription));
            }
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
