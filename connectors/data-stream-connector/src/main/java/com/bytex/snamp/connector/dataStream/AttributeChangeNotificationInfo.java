package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.AttributeChangeNotification;

/**
 * Represents descriptor of notification {@link AttributeChangeNotification}.
 */
final class AttributeChangeNotificationInfo extends SyntheticNotificationInfo {
    private static final long serialVersionUID = -3001907381843702060L;

    AttributeChangeNotificationInfo(final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        super(AttributeChangeNotification.ATTRIBUTE_CHANGE, AttributeChangeNotification.class, "Occurs when one of registered attribute will be changed", descriptor);
    }
}
