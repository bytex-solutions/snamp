package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.attributes.OpenTypeAttributeInfo;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents attribute value storage.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MdaAttributeAccessor extends OpenTypeAttributeInfo {
    private final HttpAttributeManager storageManager;

    MdaAttributeAccessor(final String name,
                         final OpenType<?> type,
                         final AttributeDescriptor descriptor,
                         final HttpAttributeManager storage) {
        super(name, type, descriptor.getDescription(name), AttributeSpecifier.READ_WRITE, descriptor);
        this.storageManager = Objects.requireNonNull(storage);
    }
    /**
     * Returns the default value for this parameter, if it has one, or
     * <tt>null</tt> otherwise.
     *
     * @return the default value.
     */
    @Override
    public Object getDefaultValue() {
        return storageManager.getDefaultValue();
    }

    final Object setValue(final Object value, final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException {
        return storageManager.setValue(value, storage);
    }

    final Object getValue(final ConcurrentMap<String, ?> storage) {
        return storageManager.getValue(storage);
    }
}
