package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.internal.EntryReader;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ModelOfNotifications<TAccessor extends NotificationAccessor> extends ThreadSafeObject implements NotificationSet<TAccessor> {
    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> ModelOfNotifications(final Class<G> resourceGroupDef) {
        super(resourceGroupDef);
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected ModelOfNotifications() {
        super();
    }


    /**
     * Iterates over all registered notifications.
     * @param notificationReader
     * @param <E>
     * @throws E
     */
    public abstract <E extends Exception> void forEachNotification(final EntryReader<String, ? super TAccessor, E> notificationReader) throws E;

}
