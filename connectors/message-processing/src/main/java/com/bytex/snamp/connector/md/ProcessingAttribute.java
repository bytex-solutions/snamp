package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;

/**
 * Represents attribute which value is derived from the value of another attribute in repository.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @implNote By default, this attribute doesn't respond to the received measurement notifications.
 */
public abstract class ProcessingAttribute<T> extends MessageDrivenAttribute<T> {
    private static final long serialVersionUID = 2475124771284618979L;

    protected ProcessingAttribute(final String name,
                                  final OpenType<T> type,
                                  final String description,
                                  final AttributeSpecifier specifier,
                                  final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    protected abstract T getValue(final AttributeSupport support) throws Exception;

    protected void setValue(final AttributeSupport support, final T value) throws Exception{
        throw cannotBeModified(this);
    }

    @SuppressWarnings("unchecked")
    final void setRawValue(final AttributeSupport support, final Object value) throws Exception{
        if(getOpenType().isValue(value))
            setValue(support, (T) value);
        else
            throw new InvalidAttributeValueException(String.format("Invalid attribute '%s' type. Expected type: '%s'. Actual value: '%s'", getName(), getOpenType(), value));
    }

    @Override
    protected boolean accept(final MeasurementNotification notification) {
        return false;
    }
}
