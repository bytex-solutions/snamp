package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.web.serviceModel.AbstractManagedResourceTracker;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import java.util.Map;
import java.util.logging.Level;

import static com.bytex.snamp.gateway.modeling.NotificationAccessor.extractFromNotification;

/**
 * Represents notification hub used to listen notifications from all managed resources.
 */
final class NotificationHub extends AbstractManagedResourceTracker<NotificationListener> implements javax.management.NotificationListener {
    @Override
    protected void addResource(final ManagedResourceConnectorClient client) {
        final NotificationSupport notifications = client.queryObject(NotificationSupport.class);
        if (notifications != null)
            notifications.addNotificationListener(this, null, client.getManagedResourceName());
    }

    @Override
    protected void removeResource(final ManagedResourceConnectorClient client) {
        final NotificationSupport notifications = client.queryObject(NotificationSupport.class);
        if (notifications != null)
            try {
                notifications.removeNotificationListener(this);
            } catch (final ListenerNotFoundException e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
    }

    @Override
    protected void stop() {
    }

    @Override
    protected void start(final Map<String, NotificationListener> configuration) {

    }

    private void handleNotification(final NotificationEvent event){
        getConfiguration().values().forEach(listener -> listener.handleNotification(event));
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        assert handback instanceof String;  //handback is always resourceName
        final String resourceName = (String) handback;
        extractFromNotification(notification, NotificationSupport.class)
                .map(support -> support.getNotificationInfo(notification.getType()))
                .ifPresent(metadata -> handleNotification(new NotificationEvent(resourceName, metadata, notification)));
    }

    void startTracking(@Nonnull final NotificationListener destination) throws Exception {
        update(ImmutableMap.of("destination", destination));
    }
}
