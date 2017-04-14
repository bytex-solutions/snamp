package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.notifications.Severity;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.web.serviceModel.ManagedResourceTrackerSlim;

import javax.annotation.Nonnull;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents notification hub used to listen notifications from all managed resources.
 */
final class NotificationHub extends ManagedResourceTrackerSlim implements NotificationListener {
    private final WeakReference<com.bytex.snamp.gateway.NotificationListener> listener;

    NotificationHub(@Nonnull final com.bytex.snamp.gateway.NotificationListener listener){
        this.listener = new WeakReference<>(listener);
    }

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
        listener.clear();
    }

    @Override
    protected void start(final Map<String, String> configuration) throws Exception {

    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final com.bytex.snamp.gateway.NotificationListener listener = this.listener.get();
        if (listener != null) {
            assert handback instanceof String;  //handback is always resourceName
            assert notification.getSource() instanceof NotificationSupport;
            final String resourceName = (String) handback;
            final MBeanNotificationInfo metadata = ((NotificationSupport) notification.getSource()).getNotificationInfo(notification.getType());
            listener.handleNotification(new NotificationEvent(resourceName, metadata, notification));
        }
    }
}
