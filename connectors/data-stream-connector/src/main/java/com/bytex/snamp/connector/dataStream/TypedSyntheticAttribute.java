package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;

import javax.management.Notification;
import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents typed message-driven attribute.
 * @param <N> Type of notification that can be handled by the attribute.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
abstract class TypedSyntheticAttribute<N extends Notification> extends SyntheticAttribute {
    private static final long serialVersionUID = 4061148981437115601L;
    private final Class<N> expectedType;

    TypedSyntheticAttribute(final Class<N> notificationType,
                            final String name,
                            final OpenType<?> type,
                            final String description,
                            final AttributeSpecifier specifier,
                            final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
        expectedType = Objects.requireNonNull(notificationType);
    }

    protected abstract Object changeAttributeValue(final N notification) throws Exception;

    protected boolean isNotificationEnabled(final N notification){
        return true;
    }

    @Override
    protected final NotificationProcessingResult handleNotification(final Notification notification) {
        if (expectedType.isInstance(notification)) {
            final N n = expectedType.cast(notification);
            if (isNotificationEnabled(n)) {
                final Object newValue;
                try {
                    newValue = changeAttributeValue(n);
                } catch (final Exception e) {
                    return processingFailed(e);
                }
                return notificationProcessed(newValue);
            }
        }
        return notificationIgnored();
    }
}
