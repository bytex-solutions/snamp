package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.OpenType;

/**
 * Represents attribute which value is derived from the value of another attribute in repository.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @implNote By default, this attribute doesn't respond to the received measurement notifications.
 */
public abstract class ProcessingAttribute extends MessageDrivenAttribute {
    private static final long serialVersionUID = 2475124771284618979L;

    protected ProcessingAttribute(final String name,
                                  final OpenType<?> type,
                                  final String description,
                                  final AttributeSpecifier specifier,
                                  final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    protected abstract Object getValue(final AttributeSupport support) throws Exception;

    @Override
    protected boolean accept(final MeasurementNotification notification) {
        return false;
    }
}
