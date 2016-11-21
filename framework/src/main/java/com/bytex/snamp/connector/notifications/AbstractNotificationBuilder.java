package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.Stateful;

import javax.management.Notification;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Represents base class for notification builder.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractNotificationBuilder<N extends Notification> implements Supplier<N>, Stateful {
    private final AtomicLong sequenceNumber = new AtomicLong(0L);
    private String message = "";
    private long timeStamp = 0L;
    private Object source = null;
    private Object userData = null;

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

    public final AbstractNotificationBuilder<N> setMessage(final String value){
        message = Objects.requireNonNull(value);
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

    public final AbstractNotificationBuilder<N> setSource(final Object value){
        source = Objects.requireNonNull(value);
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
