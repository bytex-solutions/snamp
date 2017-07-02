package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.Stateful;

import javax.annotation.Nonnull;
import javax.management.Notification;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Represents base class for notification builder.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractNotificationBuilder<N extends Notification> implements Supplier<N>, Stateful {
    private final AtomicLong sequenceNumber;
    private String message;
    private long timeStamp;
    private Object source;
    private Object userData;

    protected AbstractNotificationBuilder() {
        sequenceNumber = new AtomicLong(0L);
        message = "";
    }

    protected AbstractNotificationBuilder(final N notification){
        sequenceNumber = new AtomicLong(notification.getSequenceNumber());
        message = notification.getMessage();
        timeStamp = notification.getTimeStamp();
        source = notification.getSource();
        userData = notification.getUserData();
    }

    /**
     * Gets type of notification.
     * @return Type of notification.
     */
    public abstract String getType();

    public final AbstractNotificationBuilder<N> setSequenceNumber(final long value){
        sequenceNumber.set(value);
        return this;
    }

    protected final long getSequenceNumber(final boolean autoIncrement){
        return autoIncrement ? sequenceNumber.getAndIncrement() : sequenceNumber.get();
    }

    public final AbstractNotificationBuilder<N> setMessage(@Nonnull final String value){
        message = value;
        return this;
    }

    protected final String getMessage(){
        return message;
    }

    public final AbstractNotificationBuilder<N> setTimeStamp(final long value){
        timeStamp = value;
        return this;
    }

    protected final long getTimeStamp(){
        return timeStamp;
    }

    public final AbstractNotificationBuilder<N> setTimeStamp(final Date value){
        return setTimeStamp(value.getTime());
    }

    public final AbstractNotificationBuilder<N> setTimeStamp(){
        return setTimeStamp(System.currentTimeMillis());
    }

    public final AbstractNotificationBuilder<N> setSource(@Nonnull final Object value){
        source = value;
        return this;
    }

    protected final Object getSource(){
        return source;
    }

    public final AbstractNotificationBuilder<N> setUserData(final Object value){
        userData = value;
        return this;
    }

    protected final Object getUserData(){
        return userData;
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        sequenceNumber.set(0L);
        message = "";
        timeStamp = 0L;
        source = null;
        userData = null;
    }
}
