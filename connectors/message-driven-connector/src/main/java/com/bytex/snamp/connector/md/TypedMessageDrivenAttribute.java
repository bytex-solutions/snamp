package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents typed message-driven attribute.
 * @param <N> Type of notification that can be handled by the attribute.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
abstract class TypedMessageDrivenAttribute<N extends Notification> extends MessageDrivenAttribute {
    private static final long serialVersionUID = 4061148981437115601L;
    private final Class<N> expectedType;
    private final NotificationFilter filter;

    TypedMessageDrivenAttribute(final Class<N> notificationType,
                                final String name,
                                final OpenType<?> type,
                                final String description,
                                final AttributeSpecifier specifier,
                                final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(name, type, description, specifier, descriptor);
        expectedType = Objects.requireNonNull(notificationType);
        filter = MessageDrivenConnectorConfigurationDescriptionProvider.parseNotificationFilter(descriptor);
    }

    protected abstract Object changeAttributeValue(final N notification) throws Exception;

    protected boolean isNotificationEnabled(final N notification){
        return true;
    }

    @Override
    protected final NotificationProcessingResult handleNotification(final Notification notification) throws Exception {
        if (expectedType.isInstance(notification) && filter.isNotificationEnabled(notification)) {
            final N n = expectedType.cast(notification);
            if (isNotificationEnabled(n))
                return notificationProcessed(changeAttributeValue(n));
        }
        return notificationIgnored();
    }
}
