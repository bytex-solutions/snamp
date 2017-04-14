package com.bytex.snamp.connector.notifications;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.management.Notification;

/**
 * Represents builder of {@link Notification} objects.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@NotThreadSafe
public class NotificationBuilder extends AbstractNotificationBuilder<Notification> {
    private String type;

    public NotificationBuilder(){
        type = "";
    }

    public NotificationBuilder(final Notification notification) {
        super(notification);
        type = notification.getType();
    }

    public final NotificationBuilder setType(@Nonnull final String value){
        type = value;
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

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        type = "";
        super.reset();
    }
}
