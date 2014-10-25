package com.itworks.snamp.connectors.notifications;

/**
 * Represents exception occurred when consumer attempts to subscribe
 * to non-existent subscription list.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UnknownSubscriptionException extends Exception {
    /**
     * The identifier of subscription list.
     */
    public final String subscriptionListID;

    public UnknownSubscriptionException(final String listId){
        super(String.format("Unknown subscription %s", listId));
        this.subscriptionListID = listId;
    }
}
