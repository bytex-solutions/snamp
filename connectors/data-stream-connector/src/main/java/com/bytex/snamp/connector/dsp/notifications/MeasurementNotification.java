package com.bytex.snamp.connector.dsp.notifications;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import javax.management.Notification;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents notification with measurement of some value.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public abstract class MeasurementNotification<M extends Measurement> extends Notification {
    private static final long serialVersionUID = 3373809208171833571L;

    MeasurementNotification(final String type, final Object source, final String message) {
        super(type, source, 0L, message);
    }

    public final String getComponentName(){
        return getMeasurement().getComponentName();
    }

    public final String getInstanceName(){
        return getMeasurement().getInstanceName();
    }

    @Override
    public Object getUserData() {
        return firstNonNull(super.getUserData(), getMeasurement().getAnnotations());
    }

    @Override
    public final String getMessage(){
        return getMeasurement().getMessage(super.getMessage());
    }

    @Override
    public final long getTimeStamp() {
        return getMeasurement().getTimeStamp();
    }

    @Override
    public final void setTimeStamp(long timeStamp) {
        getMeasurement().setTimeStamp(timeStamp);
    }

    public abstract M getMeasurement();
}
