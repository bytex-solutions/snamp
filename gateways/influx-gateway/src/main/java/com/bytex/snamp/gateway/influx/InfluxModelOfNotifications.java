package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;

import javax.management.MBeanNotificationInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class InfluxModelOfNotifications extends ModelOfNotifications<NotificationPoint> {
    private Reporter reporter;
    private final AttributeSet<AttributePoint> attributes;

    InfluxModelOfNotifications(final AttributeSet<AttributePoint> attributes){
        this.attributes = Objects.requireNonNull(attributes);
    }

    void setReporter(final Reporter value){
        reporter = Objects.requireNonNull(value);
    }

    @Override
    protected NotificationPoint createAccessor(final String resourceName, final MBeanNotificationInfo metadata) throws Exception {
        return new NotificationPoint(metadata) {

            @Override
            String getResourceName() {
                return resourceName;
            }

            @Override
            Reporter getReporter() {
                return reporter;
            }

            @Override
            AttributeSet<AttributePoint> getAttributes() {
                return attributes;
            }
        };
    }

    @Override
    protected void cleared() {
        reporter = null;
    }
}
