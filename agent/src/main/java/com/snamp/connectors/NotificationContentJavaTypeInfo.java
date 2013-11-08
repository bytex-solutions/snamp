package com.snamp.connectors;

/**
 * Represents JVM-compatible type of the notification content.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationContentJavaTypeInfo<T> extends NotificationContentTypeInfo, EntityJavaTypeInfo<T> {
}
