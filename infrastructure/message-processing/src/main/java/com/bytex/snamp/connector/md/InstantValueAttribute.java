package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.notifications.measurement.InstantValueChangedNotification;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.jmx.DefaultValues;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.concurrent.ConcurrentMap;

import static com.bytex.snamp.jmx.OpenMBean.cast;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class InstantValueAttribute extends MessageDrivenAttribute {
    private static final long serialVersionUID = 2840288523283603874L;

    private final Object defaultValue;

    InstantValueAttribute(final String name,
                          final SimpleType<?> type,
                          final AttributeDescriptor descriptor) {
        super(name, type, "Represents instant value supplied by connector", AttributeSpecifier.READ_WRITE, descriptor);
        defaultValue = DefaultValues.get(type);
        assert defaultValue != null;
    }


    private String getStorageKey(){
        return AttributeDescriptor.getName(this);
    }

    private boolean accept(final InstantValueChangedNotification<?> notification) {
        final boolean result;
        if (result = getOpenType().isValue(notification.getValue()))
            setValue((T) notification.getValue());
        return result;
    }

    @Override
    boolean accept(final MeasurementNotification notification) {
        return notification instanceof InstantValueChangedNotification<?> && accept((InstantValueChangedNotification<?>)notification);
    }

    protected T getValue(final ConcurrentMap<String, Object> storage) throws OpenDataException {
        return cast(attributeType, storage.getOrDefault(getStorageKey(), defaultValue));
    }

    protected void setValue(final T value, final ConcurrentMap<String, Object> storage) {
        storage.put(getStorageKey(), value);
    }
}
