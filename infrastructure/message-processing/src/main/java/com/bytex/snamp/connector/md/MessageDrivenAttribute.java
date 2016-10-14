package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.OpenMBeanAttributeInfoImpl;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.OpenType;

/**
 * Represents abstract class for attributes that can be updated by third-party component
 * using message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MessageDrivenAttribute extends OpenMBeanAttributeInfoImpl {
    private static final long serialVersionUID = -2361230399455752656L;

    MessageDrivenAttribute(final String name,
                           final OpenType<?> type,
                           final String description,
                           final AttributeSpecifier specifier,
                           final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    abstract boolean accept(final MeasurementNotification notification);


}
