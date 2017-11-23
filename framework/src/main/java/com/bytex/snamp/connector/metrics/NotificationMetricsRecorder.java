package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents default implementation of interface {@link NotificationMetrics}.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@ThreadSafe
public class NotificationMetricsRecorder extends AbstractMetric implements NotificationMetrics {
    public static final String DEFAULT_NAME = "notifications";
    private static final long serialVersionUID = 6355507158499182709L;
    private final RateRecorder notificationsRate;

    public NotificationMetricsRecorder(final String name){
        super(name);
        notificationsRate = new RateRecorder(name);
    }

    public NotificationMetricsRecorder(){
        this(DEFAULT_NAME);
    }

    protected NotificationMetricsRecorder(final NotificationMetricsRecorder source){
        super(source);
        notificationsRate = source.notificationsRate.clone();
    }

    @Override
    public NotificationMetricsRecorder clone() {
        return new NotificationMetricsRecorder(this);
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
        notificationsRate.reset();
    }
}
