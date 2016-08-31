package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.advanced.InstantValueNotification;
import com.bytex.snamp.connector.notifications.advanced.MonitoringNotification;
import com.bytex.snamp.internal.MapKeyRef;
import com.bytex.snamp.jmx.DefaultValues;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static com.bytex.snamp.jmx.OpenMBean.cast;
import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class InstantValueAttribute<T extends Comparable<T>> extends MessageDrivenAttribute<T> {
    private static final long serialVersionUID = 2840288523283603874L;

    private final T defaultValue;
    private final SimpleType<T> attributeType;
    private final MapKeyRef<String, Object> keyRef;

    InstantValueAttribute(final SimpleType<T> attributeType,
                          final ConcurrentMap<String, Object> storage) {
        this.attributeType = Objects.requireNonNull(attributeType);
        defaultValue = DefaultValues.get(attributeType);
        assert defaultValue != null;
        keyRef = new MapKeyRef<>(storage, getStorageKey());
    }

    private String getStorageKey(){
        return AttributeDescriptor.getName(this);
    }

    private boolean accept(final InstantValueNotification<?> notification) {
        final boolean result;
        if (result = getOpenType().isValue(notification.getValue()))
            setValue((T) notification.getValue());
        return result;
    }

    @Override
    boolean accept(final MonitoringNotification notification) {
        return notification instanceof InstantValueNotification<?> && accept((InstantValueNotification<?>)notification);
    }

    protected T getValue(final ConcurrentMap<String, Object> storage) throws OpenDataException {
        return cast(attributeType, storage.getOrDefault(getStorageKey(), defaultValue));
    }

    protected void setValue(final T value, final ConcurrentMap<String, Object> storage) {
        storage.put(getStorageKey(), value);
    }
}
