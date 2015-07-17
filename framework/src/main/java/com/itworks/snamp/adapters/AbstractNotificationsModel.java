package com.itworks.snamp.adapters;

import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.internal.RecordReader;

import java.util.EnumSet;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractNotificationsModel<TAccessor extends NotificationAccessor> extends ThreadSafeObject {
    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> AbstractNotificationsModel(final Class<G> resourceGroupDef) {
        super(resourceGroupDef);
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected AbstractNotificationsModel() {
        super();
    }


    /**
     * Iterates over all registered notifications.
     * @param notificationReader
     * @param <E>
     * @throws E
     */
    public abstract <E extends Exception> void forEachNotification(final RecordReader<String, ? super TAccessor, E> notificationReader) throws E;

}
