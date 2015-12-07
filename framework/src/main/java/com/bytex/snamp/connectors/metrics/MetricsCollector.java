package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Switch;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

/**
 * Represents default implementation of the {@link MetricsReader} service.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MetricsCollector extends AbstractAggregator implements MetricsReader {
    private final AttributeMetricsWriter attributes = new AttributeMetricsWriter();
    private final NotificationMetricsImpl notifications = new NotificationMetricsImpl();
    private final OperationMetricsImpl operations = new OperationMetricsImpl();

    public AttributeMetricsWriter getAttributeMetrics(){
        return attributes;
    }

    /**
     * Gets metrics for the specified resource feature.
     *
     * @param featureType Type of the feature.
     * @return A set of metrics.
     * @see AttributeMetrics
     */
    @Override
    public Metrics getMetrics(final Class<? extends MBeanFeatureInfo> featureType) {
        return new Switch<Class<? extends MBeanFeatureInfo>, Metrics>()
                .equals(MBeanAttributeInfo.class, attributes)
                .equals(MBeanNotificationInfo.class, notifications)
                .equals(MBeanOperationInfo.class, operations)
                .apply(featureType);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void resetAll() {
        attributes.reset();
        notifications.reset();
        operations.reset();
    }
}
