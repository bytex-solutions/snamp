package com.bytex.snamp.connector.metrics;

/**
 * Represents default implementation of interface {@link NotificationMetric}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class NotificationMetricRecorder extends AbstractMetric implements NotificationMetric {
    public static final String DEFAULT_NAME = "notifications";
    private final RateRecorder notificationsRate;

    public NotificationMetricRecorder(final String name){
        super(name);
        notificationsRate = new RateRecorder(name);
    }

    public NotificationMetricRecorder(){
        this(DEFAULT_NAME);
    }

    public void update(){
        notificationsRate.mark();
    }

    /**
     * Gets rate of all emitted notifications.
     *
     * @return Rate of all emitted notifications.
     */
    @Override
    public final Rate notifications() {
        return notificationsRate;
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        notificationsRate.mark();
    }
}
