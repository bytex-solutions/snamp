package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.UnicastNotificationRouter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * Bridge between notifications and XMPP protocol.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPNotificationAccessor extends UnicastNotificationRouter {
    private final String resourceName;

    XMPPNotificationAccessor(final MBeanNotificationInfo metadata,
                             final NotificationListener listener,
                             final String resourceName) {
        super(metadata, listener);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }
}
