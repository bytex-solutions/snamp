package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ThreadSafe;

import javax.management.Notification;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Represents builder of {@link Notification} objects.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe(false)
public class NotificationBuilder implements Supplier<Notification> {
    private final AtomicLong sequenceNumber = new AtomicLong(0L);
    private String message = "";
    private long timeStamp = System.currentTimeMillis();
    private String type = "";
    private Object source = null;
    private Object userData = null;

    public final NotificationBuilder setType(final String value){
        type = Objects.requireNonNull(value);
        return this;
    }

    public final NotificationBuilder setSequenceNumber(final long value){
        sequenceNumber.set(value);
        return this;
    }

    public final NotificationBuilder setMessage(final String value){
        message = Objects.requireNonNull(value);
        return this;
    }

    public final NotificationBuilder setTimeStamp(final long value){
        timeStamp = value;
        return this;
    }

    public final NotificationBuilder setTimeStamp(final Date value){
        return setTimeStamp(value.getTime());
    }

    public final NotificationBuilder setTimeStamp(){
        return setTimeStamp(System.currentTimeMillis());
    }

    public final NotificationBuilder setSource(final Object value){
        source = Objects.requireNonNull(value);
        return this;
    }

    public final NotificationBuilder setUserData(final Object value){
        userData = value;
        return this;
    }

    protected Notification create(final String type,
                                  final Object source,
                                  final long sequenceNumber,
                                  final long timeStamp,
                                  final String message,
                                  final Object userData){
        final Notification result = new Notification(type,
                source,
                sequenceNumber,
                timeStamp,
                message);
        result.setUserData(userData);
        return result;
    }

    /**
     * Retrieves an instance of the {@link Notification}.
     *
     * @return An instance of the {@link Notification}.
     */
    @Override
    public final Notification get() {
        return create(type, source, sequenceNumber.getAndIncrement(), timeStamp, message, userData);
    }
}
