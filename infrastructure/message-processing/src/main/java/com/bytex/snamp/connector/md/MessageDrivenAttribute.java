package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.OpenMBeanAttributeAccessor;
import com.bytex.snamp.connector.notifications.advanced.MonitoringNotification;

import javax.management.JMException;
import java.util.function.Consumer;

/**
 * Represents abstract class for attributes that can be updated by third-party component
 * using message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageDrivenAttribute<T> extends OpenMBeanAttributeAccessor<T> {
    private static final long serialVersionUID = -2361230399455752656L;

    abstract boolean accept(final MonitoringNotification notification);
}
