package com.itworks.snamp.adapters.http;

import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.NotificationRouter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HttpNotificationAccessor extends NotificationRouter implements HttpAccessor {
    static final String NOTIFICATION_ACCESS_PATH = "/notifications/{" + RESOURCE_URL_PARAM + "}";
    private final String resourceName;

    HttpNotificationAccessor(final String resourceName,
                             final MBeanNotificationInfo metadata,
                             final NotificationListener destination) {
        super(metadata, destination);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    @Override
    public String getPath(final String servletContext,
                          final String resourceName) {
        return servletContext +
                NOTIFICATION_ACCESS_PATH.replace("{" + RESOURCE_URL_PARAM + "}", resourceName);
    }
}
