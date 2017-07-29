package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.Box;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import org.influxdb.dto.Point;

import javax.management.MBeanNotificationInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class InfluxModelOfNotifications extends ModelOfNotifications<NotificationPoint> {
    private final Box<Reporter> reporter;
    private final AttributeSet<AttributePoint> attributes;
    private final ClusterMember clusterMember;

    InfluxModelOfNotifications(final AttributeSet<AttributePoint> attributes,
                               final ClusterMember clusterMember){
        this.attributes = Objects.requireNonNull(attributes);
        this.clusterMember = Objects.requireNonNull(clusterMember);
        this.reporter = Box.of(null);
    }

    void setReporter(final Reporter value){
        reporter.set(Objects.requireNonNull(value));
    }

    @Override
    protected NotificationPoint createAccessor(final String resourceName, final MBeanNotificationInfo metadata) {
        return new NotificationPoint(metadata, clusterMember) {

            @Override
            String getResourceName() {
                return resourceName;
            }

            @Override
            boolean report(final Point p) {
                return reporter.ifPresent(reporter -> reporter.report(p));
            }

            @Override
            AttributeSet<AttributePoint> getAttributes() {
                return attributes;
            }
        };
    }

    @Override
    protected void cleared() {
        reporter.reset();
    }
}
