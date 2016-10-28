package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.OpenMBeanAttributeInfoImpl;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.OpenType;
import java.io.Serializable;

/**
 * Represents abstract class for attributes that can be updated by third-party component
 * using message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class MessageDrivenAttribute<T> extends OpenMBeanAttributeInfoImpl implements AutoCloseable {
    private static final long serialVersionUID = -2361230399455752656L;

    protected MessageDrivenAttribute(final String name,
                           final OpenType<T> type,
                           final String description,
                           final AttributeSpecifier specifier,
                           final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    protected abstract T getValue();

    protected abstract Serializable takeSnapshot();

    protected abstract void loadFromSnapshot(final Serializable snapshot);

    protected abstract boolean accept(final MeasurementNotification notification);
}
