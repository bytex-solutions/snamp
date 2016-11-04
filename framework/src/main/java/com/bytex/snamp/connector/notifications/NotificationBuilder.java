package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ThreadSafe;

import javax.management.Notification;
import java.util.Objects;

/**
 * Represents builder of {@link Notification} objects.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe(false)
public class NotificationBuilder extends AbstractNotificationBuilder<Notification> {
    private String type = "";

    public final NotificationBuilder setType(final String value){
        type = Objects.requireNonNull(value);
        return this;
    }

    @Override
    public final String getType(){
        return type;
    }

    /**
     * Retrieves an instance of the {@link Notification}.
     *
     * @return An instance of the {@link Notification}.
     */
    @Override
    public Notification get() {
        final Notification result = new Notification(type,
                getSource(),
                getSequenceNumber(true),
                getTimeStamp(),
                getMessage());
        result.setUserData(getUserData());
        return result;
    }
}
