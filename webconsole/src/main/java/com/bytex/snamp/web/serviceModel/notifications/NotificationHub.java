package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.Convert;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.connector.AbstractManagedResourceTracker;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import org.osgi.framework.BundleContext;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Optional;

import static com.bytex.snamp.gateway.modeling.NotificationAccessor.extractFromNotification;

/**
 * Represents notification hub used to listen notifications from all managed resources.
 */
final class NotificationHub extends AbstractManagedResourceTracker implements javax.management.NotificationListener {
    private final WeakEventListener<NotificationListener, NotificationEvent> destination;

    NotificationHub(final BundleContext context,
                    final NotificationListener destination) {
        super(context);
        this.destination = WeakEventListener.create(destination, NotificationListener::handleNotification);
    }

    @Override
    protected void addResource(final ManagedResourceConnectorClient client) {
        final String resourceName = client.getManagedResourceName();
        client.queryObject(NotificationSupport.class)
                .ifPresent(support -> support.addNotificationListener(this, null, resourceName));
    }

    @Override
    protected void removeResource(final ManagedResourceConnectorClient client) throws ListenerNotFoundException {
        final Optional<NotificationSupport> support = client.queryObject(NotificationSupport.class);
        if (support.isPresent())
            support.get().removeNotificationListener(this);
    }


    private void handleNotification(final String resourceName, final MBeanNotificationInfo metadata, final Notification notification) {
        destination.invoke(new NotificationEvent(resourceName, metadata, notification));
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final String resourceName = Convert.toType(handback, String.class).orElseThrow(AssertionError::new);
        extractFromNotification(notification, NotificationSupport.class)
                .flatMap(support -> support.getNotificationInfo(notification.getType()))
                .ifPresent(metadata -> handleNotification(resourceName, metadata, notification));
    }

    @Override
    public void close() {
        destination.clear();
        super.close();
    }
}
