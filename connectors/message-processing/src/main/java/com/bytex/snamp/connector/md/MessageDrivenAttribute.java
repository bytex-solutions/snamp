package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.OpenType;

/**
 * Represents attribute which value depends on the measurement notification received from external component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see DistributedAttribute
 * @see ProcessingAttribute
 */
public abstract class MessageDrivenAttribute extends AbstractOpenAttributeInfo implements NotificationListener {
    private static final long serialVersionUID = -2361230399455752656L;

    MessageDrivenAttribute(final String name,
                           final OpenType<?> type,
                           final String description,
                           final AttributeSpecifier specifier,
                           final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    static MBeanException cannotBeModified(final MessageDrivenAttribute attribute){
        return new MBeanException(new UnsupportedOperationException(String.format("Attribute '%s' cannot be modified", attribute)));
    }
}
