package com.bytex.snamp.instrumentation.measurements.jmx;

import com.bytex.snamp.instrumentation.measurements.Health;

/**
 * Represents health check wrapped into JMX notification.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public final class HealthNotification extends MeasurementNotification<Health> {
    public static final String TYPE = "com.bytex.snamp.measurement.health";
    private static final long serialVersionUID = 6031081350393997539L;
    private final Health healthCheck;

    HealthNotification(final Object source, final Health health) {
        super(TYPE, source, getMessage(health));
        healthCheck = health;
    }

    private static String getMessage(final Health health) {
        final String description = health.getDescription();
        return description == null || description.isEmpty() ? health.getStatus().toString() : description;
    }

    @Override
    public Health getMeasurement() {
        return healthCheck;
    }
}
