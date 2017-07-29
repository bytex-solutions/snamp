package com.bytex.snamp.gateway.http;

import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class HttpNotificationAccessor extends NotificationRouter implements HttpAccessor {
    HttpNotificationAccessor(final String resourceName,
                             final MBeanNotificationInfo metadata,
                             final NotificationListener destination) {
        super(resourceName, metadata, destination);
    }

    @Override
    public String getPath(final String servletContext,
                          final String resourceName) {
        return servletContext + '/' + resourceName;
    }
}
