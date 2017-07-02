package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.measurements.TimeMeasurement;
import com.bytex.snamp.instrumentation.measurements.ValueMeasurement;
import com.bytex.snamp.instrumentation.measurements.jmx.MeasurementNotification;
import com.google.common.collect.ImmutableMap;
import org.influxdb.dto.Point;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    abstract boolean report(final Point p);

    abstract AttributeSet<AttributePoint> getAttributes();

    private Logger getLogger() {
        return LoggerProvider.getLoggerForObject(this);
    }

    private Map<String, String> extractTags() {
        return Helpers.extractTags(getBundleContextOfObject(this), getResourceName());
    }

    private void handleNotification(final AttributeChangeNotification notification) {
        final Map<String, String> tags = extractTags();
        try {
            getAttributes().processAttribute(getResourceName(), notification.getAttributeName(), accessor -> report(accessor.toPoint(tags)));
        } catch (final JMException | InterruptedException e) {
            getLogger().log(Level.SEVERE, String.format("Unable to read attribute %s from resource %s", notification.getAttributeName(), getResourceName()), e);
        }
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
            if (!span.getCorrelationID().isEmpty()) {
                final String correlationID = span.getCorrelationID().toString();
                tags.put("correlationID", correlationID);
                fields.put("correlationID", correlationID);
            }
            fields.put("spanID", span.getSpanID().toString());
            if (!span.getParentSpanID().isEmpty())
                fields.put("parentSpanID", span.getParentSpanID().toString());
            if (!isNullOrEmpty(span.getModuleName()))
                tags.put("moduleName", span.getModuleName());
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
        report(p);
    }

    private void handleNotification(final Notification notification) {
        final Map<String, String> tags = extractTags();
        final Map<String, Object> fields = ImmutableMap.of(
                "sequenceNumber", notification.getSequenceNumber(),
                "message", notification.getMessage()
        );
        final Point p = Point.measurement(notification.getType())
                .time(notification.getTimeStamp(), TimeUnit.MILLISECONDS)
                .fields(fields)
                .tag(tags)
                .build();
        report(p);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        //only active cluster node is responsible for reporting
        if (clusterMember.isActive()) {
            if (notification instanceof NotificationContainer)
                handleNotification(((NotificationContainer) notification).get(), handback);
            else if (notification instanceof AttributeChangeNotification)
                handleNotification((AttributeChangeNotification) notification);
            else if (notification instanceof MeasurementNotification<?>)
                handleNotification((MeasurementNotification<?>) notification);
            else
                handleNotification(notification);
        }
    }
}
