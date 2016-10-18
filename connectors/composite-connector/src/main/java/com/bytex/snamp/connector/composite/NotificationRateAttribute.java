package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RateRecorder;
import com.bytex.snamp.jmx.MetricsConverter;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import static com.bytex.snamp.core.DistributedServices.isActiveNode;

/**
 * Represents attribute computing rate of the input notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class NotificationRateAttribute extends MetricAttribute<RateRecorder> implements NotificationListener {
    static final Class<RateRecorder> STATE_TYPE = RateRecorder.class;
    private static final long serialVersionUID = -6525078880291154173L;
    private static final CompositeType TYPE = MetricsConverter.RATE_TYPE;
    private static final String DESCRIPTION = "Computes rate of the notification";

    private final String notificationType;

    NotificationRateAttribute(final String name,
                              final AttributeDescriptor descriptor,
                              final Box<RateRecorder> stateStorage) {
        super(name, TYPE, DESCRIPTION, descriptor, stateStorage);
        notificationType = descriptor.getName(name);
    }

    @Override
    CompositeData getValue(final RateRecorder metric) {
        return MetricsConverter.fromRate(metric);
    }

    @Override
    RateRecorder createMetrics() {
        return new RateRecorder(getName());
    }

    private void updateMetric() {
        //metric can be updated only at active cluster node
        if (isActiveNode(getBundleContext()))
            metricStorage.accumulateAndGet(m -> {
                if(m == null) m = createMetrics();
                m.mark();
                return m;
            });
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if(this.notificationType.equals(notification.getType()))
            updateMetric();
    }
}
