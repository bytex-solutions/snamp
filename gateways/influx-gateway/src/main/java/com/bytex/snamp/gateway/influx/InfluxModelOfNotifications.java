package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.modeling.ModelOfNotifications;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class InfluxModelOfNotifications extends ModelOfNotifications<NotificationPoint> {
    @Override
    protected NotificationPoint createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception {
        return null;
    }
}
