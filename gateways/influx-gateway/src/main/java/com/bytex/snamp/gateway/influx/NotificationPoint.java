package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.measurements.TimeMeasurement;
import com.bytex.snamp.instrumentation.measurements.ValueMeasurement;
import com.bytex.snamp.instrumentation.measurements.jmx.MeasurementNotification;
import org.influxdb.dto.Point;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class NotificationPoint extends NotificationAccessor {
    private final ClusterMember clusterMember;

    NotificationPoint(final MBeanNotificationInfo metadata, final ClusterMember clusterMember) {
        super(metadata);
        this.clusterMember = clusterMember;
    }

    abstract String getResourceName();

    abstract Reporter getReporter();

    abstract AttributeSet<AttributePoint> getAttributes();

    private Map<String, String> extractTags() {
        return Helpers.extractTags(getBundleContextOfObject(this), getResourceName());
    }

    private Void handleNotification(final AttributeChangeNotification notification) throws JMException, InterruptedException {
        final Map<String, String> tags = extractTags();
        final Reporter reporter = getReporter();
        if (reporter != null)
            getAttributes().processAttribute(getResourceName(), notification.getAttributeName(), accessor -> reporter.report(accessor.toPoint(tags)));
        return null;
    }

    private void handleNotification(final MeasurementNotification<?> notification) {
        final String measurementName = notification.getMeasurement().getName();
        if (isNullOrEmpty(measurementName))
            return;
        final Map<String, Object> fields;
        final Map<String, String> tags = extractTags();
        final Measurement measurement = notification.getMeasurement();
        if (measurement instanceof ValueMeasurement)
            fields = Helpers.toScalar(((ValueMeasurement) measurement).getRawValue());
        else if (measurement instanceof Span) {
            fields = new HashMap<>();
            final Span span = (Span) measurement;
            tags.put("correlationID", span.getCorrelationID().toString());
            fields.put("correlationID", span.getCorrelationID().toString());
            fields.put("spanID", span.getSpanID().toString());
            fields.put("parentSpanID", span.getParentSpanID().toString());
            fields.put("duration", span.getDuration(TimeUnit.NANOSECONDS));
        } else if (measurement instanceof TimeMeasurement)
            fields = Helpers.toScalar(((TimeMeasurement) measurement).getDuration(TimeUnit.NANOSECONDS));
        else
            return;
        final Point p = Point.measurement(measurementName)
                .fields(fields)
                .time(notification.getTimeStamp(), TimeUnit.MILLISECONDS)
                .tag(tags)
                .build();
        final Reporter reporter = getReporter();
        if (reporter != null)
            reporter.report(p);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        //only active cluster node is responsible for reporting
        if(clusterMember.isActive()) {
            if(notification instanceof NotificationContainer)
                handleNotification(((NotificationContainer) notification).get(), handback);
            if (notification instanceof AttributeChangeNotification)
                callUnchecked(() -> handleNotification((AttributeChangeNotification) notification));
            else if (notification instanceof MeasurementNotification<?>)
                handleNotification((MeasurementNotification<?>) notification);
        }
    }
}
