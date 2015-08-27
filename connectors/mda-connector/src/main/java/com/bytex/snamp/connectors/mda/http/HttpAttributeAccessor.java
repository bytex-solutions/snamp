package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MdaAttributeAccessor;

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
final class HttpAttributeAccessor extends MdaAttributeAccessor {
    private final HttpAttributeManager storageManager;

    HttpAttributeAccessor(final String name,
                          final OpenType<?> type,
                          final AttributeDescriptor descriptor,
                          final HttpAttributeManager storage) {
        super(name, type, AttributeSpecifier.READ_WRITE, descriptor);
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

    @Override
    public final Object setValue(final Object value, final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException {
        return storageManager.setValue(value, storage);
    }

    @Override
    public final Object getValue(final ConcurrentMap<String, ?> storage) {
        return storageManager.getValue(storage);
    }
}
