package com.bytex.snamp.connectors.notifications;

import javax.management.Notification;
import java.util.function.Predicate;

/**
 * Provides various implementations of {@link Mailbox}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public final class MailboxFactory {
    private MailboxFactory(){
        throw new InstantiationError();
    }

    /**
     * Creates a new fixed-size mailbox.
     * @param capacity The maximum amount of notifications in the mailbox.
     * @return A new instance of mailbox.
     */
    public static Mailbox newFixedSizeMailbox(final int capacity){
        return new FixedSizeMailbox(capacity);
    }

    /**
     * Creates a new mailbox with unbounded capacity.
     * @return A new mailbox.
     */
    public static Mailbox newMailbox(){
        return new LinkedMailbox();
    }

    /**
     * Creates a new mailbox with unbounded capacity and filter for notifications.
     * @param filter A filter used to select notifications to be placed into mailbox. Cannot be {@literal null}.
     * @return A new mailbox
     */
    public static Mailbox newMailbox(final Predicate<? super Notification> filter){
        return new LinkedMailbox(filter);
    }

    /**
     * Creates a new mailbox for the specified type of notifications.
     * @param notifType Notification type that should be matched to {@link Notification#getType()}. Cannot be {@literal null}.
     * @return A new mailbox.
     */
    public static Mailbox newMailbox(final String notifType) {
        return newMailbox(n -> notifType.equals(n.getType()));
    }
}
